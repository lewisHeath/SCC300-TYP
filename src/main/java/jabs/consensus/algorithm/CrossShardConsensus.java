package jabs.consensus.algorithm;

import jabs.network.message.CoordinationMessage;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.pbft.PBFTShardedNode;

import java.util.ArrayList;
import java.util.HashMap;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.pbft.PBFTBlock;

public class CrossShardConsensus {

    private PBFTShardedNode node;
    // data structure for tracking locked accounts
    private ArrayList<EthereumAccount> lockedAccounts = new ArrayList<EthereumAccount>();
    private ArrayList<EthereumTx> preparedTransactions = new ArrayList<EthereumTx>();
    private HashMap<EthereumTx, Node> preparedTransactionsFrom = new HashMap<EthereumTx, Node>();

    public CrossShardConsensus(PBFTShardedNode node) {
        this.node = node;
    }

    public void processCoordinationMessage(CoordinationMessage message, Node from) {

        // get the transaction from the message
        EthereumTx tx = (EthereumTx) message.getData();
        // get all involved accounts in the transaction and store them in an ArrayList
        ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
        // this needs upgrading to handle smart contract transactions
        accounts.add(tx.getSender());
        accounts.add(tx.getReceiver());
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
                processPrePrepareMessage(accountsInThisShard, from, tx);
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
        // check if the accounts for the transaction is locked
        for (EthereumAccount account : accountsInThisShard) {
            if (lockedAccounts.contains(account)) {
                // if the account is locked, send a prepareNOTOK message back to the client node
                CoordinationMessage message = new CoordinationMessage(tx, "prepareNOTOK");
                node.sendMessageToNode(message, from);
                return;
            } 
        }
        // if the accounts are not locked, lock them
        for (EthereumAccount account : accountsInThisShard) {
            lockedAccounts.add(account);
        }
        // send prepareOK message back to the client node
        CoordinationMessage message = new CoordinationMessage(tx, "prepareOK");
        node.sendMessageToNode(message, from);
        // add the transaction to the prepared transactions list
        preparedTransactions.add(tx);
        // add the transaction and the client node to the prepared transactions from list
        preparedTransactionsFrom.put(tx, from);
    }

    private void processCommitMessage(EthereumTx tx, Node from) {
        // this will only be called if the client sends commit message, which means we now need to add the transaction to the mempool
        // and the accounts will be locked at this point
        // add the transaction to the mempool
        node.processNewTx(tx, from);
    }

    private void processRollbackMessage(ArrayList<EthereumAccount> accountsInThisShard, EthereumTx tx) {
        // remove the transaction from the prepared transactions list
        preparedTransactions.remove(tx);
        // remove the transaction and the client node from the prepared transactions from list
        preparedTransactionsFrom.remove(tx);
        // unlock the accounts
        for (EthereumAccount account : accountsInThisShard) {
            lockedAccounts.remove(account);
        }
    }

    public void processConfirmedBlock(PBFTBlock block) {
        // get the transactions from the block
        ArrayList<EthereumTx> transactions = block.getTransactions();
        // check if the transactions are in the prepared transactions list
        for (EthereumTx tx : transactions) {
            if (preparedTransactions.contains(tx)) {
                // if the transaction is in the prepared transactions list, unlock the accounts
                ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
                // this needs upgrading to handle smart contract transactions
                accounts.add(tx.getSender());
                accounts.add(tx.getReceiver());
                // unlock the accounts
                for (EthereumAccount account : accounts) {
                    lockedAccounts.remove(account);
                }
                // send a committed message to the client node
                CoordinationMessage message = new CoordinationMessage(tx, "committed");
                node.sendMessageToNode(message, preparedTransactionsFrom.get(tx));
            }
        }
    }
}
