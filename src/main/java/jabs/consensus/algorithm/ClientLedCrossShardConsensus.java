package jabs.consensus.algorithm;

import jabs.network.message.CoordinationMessage;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.simulator.event.AccountLockingEvent;
import jabs.simulator.event.AccountUnlockingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.pbft.PBFTBlock;

public class ClientLedCrossShardConsensus implements CrossShardConsensus {

    private PBFTShardedNode node;
    // data structure for tracking locked accounts
    private ArrayList<EthereumAccount> lockedAccounts = new ArrayList<EthereumAccount>();
    private ArrayList<EthereumTx> preparedTransactions = new ArrayList<EthereumTx>();
    private HashMap<EthereumTx, Node> preparedTransactionsFrom = new HashMap<EthereumTx, Node>();
    private HashMap<EthereumAccount, EthereumTx> lockedAccountsToTransactions = new HashMap<EthereumAccount, EthereumTx>();
    private ArrayList<EthereumTx> abortedTxs = new ArrayList<EthereumTx>();
    private int thisID;
    private int nodesInShard;
    private PBFTShardedNetwork network;

    public ClientLedCrossShardConsensus(PBFTShardedNode node) {
        this.node = node;
        this.nodesInShard = ((PBFTShardedNetwork) node.getNetwork()).getNodesPerShard();
        this.network = (PBFTShardedNetwork) node.getNetwork();
    }

    public void setID(int ID){
        this.thisID = ID;
    }

    public void processCoordinationMessage(CoordinationMessage message, Node from) {
        // System.out.println("Processing coordination message");
        // get the from node that was stored in the message
        Node messageFrom = message.getFrom();
        // get the transaction from the message
        EthereumTx tx = (EthereumTx) message.getData();
        // get all involved accounts in the transaction and store them in an ArrayList
        ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
        // this needs upgrading to handle smart contract transactions
        accounts.addAll(tx.getAllInvolvedAccounts());
        // get the accounts that are mapped to this shard
        ArrayList<EthereumAccount> shardAccounts = node.getShardAccounts();
        ArrayList<EthereumAccount> accountsInThisShard = new ArrayList<EthereumAccount>();
        // check which accounts are in this shard
        for (EthereumAccount account : accounts) {
            if (shardAccounts.contains(account)) {
                accountsInThisShard.add(account);
            }
        }

        switch (message.getType()) {
            case "pre-prepare":
                processPrePrepareMessage(accountsInThisShard, messageFrom, tx);
                break;
            case "commit":
                processCommitMessage(tx, from);
                break;
            case "rollback":
                processRollbackMessage(accountsInThisShard, tx);
                break;
            default:
                throw new RuntimeException("Unknown message type");
        }
    }

    private void processPrePrepareMessage(ArrayList<EthereumAccount> accountsInThisShard, Node from, EthereumTx tx) {
        if(abortedTxs.contains(tx) || from instanceof PBFTShardedNode){
            return;
        }
        // add the transaction and the client node to the prepared transactions from
        preparedTransactionsFrom.put(tx, from);
        // check if the accounts for the transaction is locked
        for (EthereumAccount account : accountsInThisShard) {
            if (lockedAccounts.contains(account)) {
                // if the account is locked, send a prepareNOTOK message back to the client node
                CoordinationMessage message = new CoordinationMessage(tx, "prepareNOTOK");
                // IF THIS IS NODE 0, TELL ALL SHARD NODES TO SEND PREPARENOTOK
                // node.sendMessageToNode(message, from);
                if(thisID == 0){
                    node.broadcastMessage(new CoordinationMessage(tx, "pre-prepare", this.node));
                    ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
                    // FORCE MESSAGE
                    for (PBFTShardedNode node : nodes) {
                        node.sendMessageToNode(message, from);
                    }
                }
                return;
            }
        }
        // if the accounts are not locked, lock them
        for (EthereumAccount account : accountsInThisShard) {
            lockedAccounts.add(account);
            lockedAccountsToTransactions.put(account, tx);
            // account locking event
            AccountLockingEvent event = new AccountLockingEvent(this.node.getSimulator().getSimulationTime(), account, this.node);
            if(thisID == 0) this.node.getSimulator().putEvent(event, 0);
        }
        // send prepareOK message back to the client node
        CoordinationMessage message = new CoordinationMessage(tx, "prepareOK");
        // IF THIS IS NODE 0 TELL ALL SHARD NODES TO SEND PREPAREOK
        // node.sendMessageToNode(message, from);
        if(thisID == 0){
            node.broadcastMessage(new CoordinationMessage(tx, "pre-prepare", this.node));
            ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
            // FORCE MESSAGE
            for (PBFTShardedNode node : nodes) {
                node.sendMessageToNode(message, from);
            }
        }
        // System.out.println("Sending prepareOK message back to client node");
        // add the transaction to the prepared transactions list
        preparedTransactions.add(tx);
    }

    private void processCommitMessage(EthereumTx tx, Node from) {
        // add the transaction to the mempool
        if (preparedTransactionsFrom.get(tx) == from) {
            node.processNewTx(tx, from);
        }
    }

    private void processRollbackMessage(ArrayList<EthereumAccount> accountsInThisShard, EthereumTx tx) {
        // remove the transaction from the prepared transactions list
        preparedTransactions.remove(tx);
        // remove the transaction and the client node from the prepared transactions
        // from list
        preparedTransactionsFrom.remove(tx);
        abortedTxs.add(tx);
        // unlock the accounts only if the tx passed to this was the one which locked
        // the accounts
        for (EthereumAccount account : accountsInThisShard) {
            if (lockedAccountsToTransactions.get(account) == tx) {
                lockedAccounts.remove(account);
                // account unlocking event
                AccountUnlockingEvent event = new AccountUnlockingEvent(this.node.getSimulator().getSimulationTime(), account, this.node);
                if(thisID == 0) this.node.getSimulator().putEvent(event, 0);
                lockedAccountsToTransactions.remove(account);
            }
        }
    }

    public void processConfirmedBlock(PBFTBlock block) {
        // get the transactions from the block
        ArrayList<EthereumTx> transactions = block.getTransactions();
        for (EthereumTx tx : transactions) {
            // if the transaction is in the prepared transactions list, unlock the accounts
            if(preparedTransactionsFrom.containsKey(tx)){
                ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
                // this needs upgrading to handle smart contract transactions
                accounts.addAll(tx.getAllInvolvedAccounts());
                // unlock the accounts
                System.out.println("size of locked accounts before confirming: " + lockedAccounts.size());
                for (EthereumAccount account : accounts) {
                    lockedAccounts.remove(account);
                    // account unlocking event
                    AccountUnlockingEvent event = new AccountUnlockingEvent(this.node.getSimulator().getSimulationTime(), account, this.node);
                    if(thisID == 0) this.node.getSimulator().putEvent(event, 0);
                    // System.out.println("Unlocking account " + account);
                }
                lockedAccounts.removeAll(accounts);
                System.out.println("size of locked accounts after confirming: " + lockedAccounts.size());
                // send a committed message to the client node
                CoordinationMessage message = new CoordinationMessage(tx, "committed");
                // NODE 0 TELL ALL NODES WHAT TO DO
                if(thisID == 0){
                    ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
                    // FORCE MESSAGE
                    for (PBFTShardedNode node : nodes) {
                        node.sendMessageToNode(message, preparedTransactionsFrom.get(tx));
                    }
                }
            }
        }
    }


    public Boolean areAccountsLocked(ArrayList<EthereumAccount> accounts) {
        for (EthereumAccount account : accounts) {
            if (lockedAccounts.contains(account)) {
                return true;
            }
        }
        return false;
    }

    public Boolean isLocked(EthereumAccount account) {
        return lockedAccounts.contains(account);
    }

    public Boolean areAllAccountsInThisShard(ArrayList<EthereumAccount> accounts) {
        // get the accounts that are mapped to this shard
        ArrayList<EthereumAccount> shardAccounts = node.getShardAccounts();
        // check which accounts are in this shard
        int i = 0;
        for (EthereumAccount account : accounts) {
            if (shardAccounts.contains(account)) {
                i++;
            }
        }
        return i == accounts.size();
    }
}
