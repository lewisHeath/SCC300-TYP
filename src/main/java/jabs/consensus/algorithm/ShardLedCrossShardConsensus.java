package jabs.consensus.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.pbft.PBFTBlock;
import jabs.network.message.CoordinationMessage;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.pbft.PBFTShardedNode;

public class ShardLedCrossShardConsensus implements CrossShardConsensus{

    private int viewNumber;
    private PBFTShardedNode node;
    private Queue<EthereumTx> txQueue = new LinkedList<EthereumTx>();
    private ArrayList<EthereumAccount> lockedAccounts = new ArrayList<EthereumAccount>();
    private HashMap<EthereumAccount, EthereumTx> lockedAccountsToTransactions = new HashMap<EthereumAccount, EthereumTx>();
    private HashMap<EthereumTx, ArrayList<Integer>> txToShards = new HashMap<EthereumTx, ArrayList<Integer>>();
    private HashSet<EthereumTx> seenTxs = new HashSet<EthereumTx>();
    private ArrayList<EthereumTx> preparedTxs = new ArrayList<EthereumTx>();
    private ArrayList<EthereumTx> secondPhaseTxs = new ArrayList<EthereumTx>();
    private HashMap<EthereumTx, Node> preparedTxsFrom = new HashMap<EthereumTx, Node>();
    private HashMap<EthereumTx, ArrayList<Node>> txToAgreedNodes = new HashMap<EthereumTx, ArrayList<Node>>();
    private HashMap<EthereumTx, ArrayList<Node>> txToDisagreedNodes = new HashMap<EthereumTx, ArrayList<Node>>();
    private HashMap<EthereumTx, HashMap<Integer, Integer>> txToAborts = new HashMap<EthereumTx, HashMap<Integer, Integer>>();
    private HashMap<EthereumTx, HashMap<Integer, Integer>> txToCommits = new HashMap<EthereumTx, HashMap<Integer, Integer>>();

    public ShardLedCrossShardConsensus(PBFTShardedNode node){
        this.viewNumber = 0;
        this.node = node;
    }

    public void processCoordinationMessage(CoordinationMessage message, Node from) {
        /*
         * 1. message types ("pre-prepare", "prepareOK", "prepareNOTOK", "commit", "abort")
         * 2. based on the message, pass to correct method
         * 3. get the accounts from the tx that are in this shard
         */

        EthereumTx tx = (EthereumTx) message.getData();
        Node messageFrom = message.getFrom();

        switch (message.getType()) {
            case "pre-prepare":
                processPrePrepare(messageFrom, tx, from);
                break;
            case "prepareOK":
                processPrepareOK(from , tx);
                break;
            case "prepareNOTOK":
                processPrepareNOTOK(from, tx);
                break;
            case "commit":
                processCommit(from, tx);
                break;
            case "abort":
                processAbort(from, tx);
                break;
        }
    }

    private void processPrePrepare(Node from, EthereumTx tx, Node sentFrom) {

        /*
         * only actually process if this node is the leader and it has come from a client OR
         * if this has come from another node and they are the leader TODO
         */

        ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
        accounts.addAll(tx.getAllInvolvedAccounts());
        // make a list of the shards involved in this tx
        ArrayList<Integer> shards = new ArrayList<Integer>();
        for (EthereumAccount account : accounts) {
            if (!shards.contains(account.getShardNumber())) {
                shards.add(account.getShardNumber());
            }
        }
        this.txToShards.put(tx, shards);

        // find the accounts that are in this shard
        ArrayList<EthereumAccount> shardAccounts = node.getShardAccounts();
        ArrayList<EthereumAccount> accountsInThisShard = new ArrayList<EthereumAccount>();
        for (EthereumAccount account : accounts) {
            if (shardAccounts.contains(account)) {
                accountsInThisShard.add(account);
            }
        }

        /*
         * if the message is from the client, add to the seenTxs then
         * if this node is the leader it will broadcast the new tx to the nodes in the shard
         * if the message is from another node, and they are the leader, perform the checks for the tx...
         */

        // add the tx to the prepared txs
        preparedTxs.add(tx);
        preparedTxsFrom.put(tx, from);
        // add the node to the agreed nodes
        ArrayList<Node> agreedNodes = new ArrayList<Node>();
        agreedNodes.add(node);
        txToAgreedNodes.put(tx, agreedNodes);
        // add the tx to the disagreed nodes
        ArrayList<Node> disagreedNodes = new ArrayList<Node>();
        txToDisagreedNodes.put(tx, disagreedNodes);
        // add the tx to the aborts
        HashMap<Integer, Integer> aborts = new HashMap<Integer, Integer>();
        // add the tx to the commits
        HashMap<Integer, Integer> commits = new HashMap<Integer, Integer>();
        // init the hashmaps with the shards with a vote of 0
        for (Integer shard : shards) {
            aborts.put(shard, 0);
            commits.put(shard, 0);
        }
        txToAborts.put(tx, aborts);
        txToCommits.put(tx, commits);

        /*
         * Only do this if the message has come from the leader or you are the leader
         */

        int ID = node.getNodeID() % ((PBFTShardedNetwork)this.node.getNetwork()).getNumberOfShards();

        if(ID == this.viewNumber || sentFrom.getNodeID() == this.viewNumber){
            // check if the accounts are locked
            if (areAccountsLocked(accountsInThisShard)) {
                // send prepareNOTOK
                CoordinationMessage message = new CoordinationMessage(tx, "prepareNOTOK");
                // send to all nodes in this shard
                node.broadcastMessage(message);
                // System.out.println("Accounts are locked");
                return;
            }
            // lock the accounts
            for (EthereumAccount account : accountsInThisShard) {
                lockedAccounts.add(account);
                lockedAccountsToTransactions.put(account, tx);
            }
            // send prepareOK
            CoordinationMessage message = new CoordinationMessage(tx, "prepareOK");
            // send to all nodes in this shard
            node.broadcastMessage(message);
        }
    }

    private void processPrepareOK(Node from, EthereumTx tx) {
        if(preparedTxs.contains(tx)){
            // add the node to the agreed nodes
            ArrayList<Node> agreedNodes = txToAgreedNodes.get(tx);
            // check if the node is already in the list
            if (!agreedNodes.contains(from)) {
                agreedNodes.add(from);
            }
            // check if the no of agreed nodes is greater than 2f 
            if(agreedNodes.size() > ((PBFTShardedNetwork)node.getNetwork()).getF()){
                // send commit message to all shards in the tx
                this.sendCommitOrAbort("commit", tx);
                // add to phase two
                secondPhaseTxs.add(tx);
                // increment view number
                this.viewNumber++;
                // if this node is the primary, then propose a new tx
                // TODO
            }
        }
    }

    private void processPrepareNOTOK(Node from, EthereumTx tx) {
        if(preparedTxs.contains(tx)){
            // add the node to the disagreed nodes
            ArrayList<Node> disagreedNodes = txToDisagreedNodes.get(tx);
            // check if the node is already in the list
            if (!disagreedNodes.contains(from)) {
                disagreedNodes.add(from);
            }
            // check if the no of disagreed nodes is greater than 2f
            if(disagreedNodes.size() > ((PBFTShardedNetwork)node.getNetwork()).getF()){
                // send abort message to all shards in the tx
                this.sendCommitOrAbort("abort", tx);
                // increment view number
                this.viewNumber++;
                // if this node is the primary, then propose a new tx
                // TODO
            }
        }
    }

    private void sendCommitOrAbort(String type, EthereumTx tx){
        ArrayList<Integer> shards = txToShards.get(tx);
        for (Integer shard : shards) {
            CoordinationMessage message = new CoordinationMessage(tx, type);
            node.broadcastMessageToShard(message, shard);
        }
        // remove the tx from the prepared txs
        preparedTxs.remove(tx);
    }

    private void processAbort(Node from, EthereumTx tx) {
        if(secondPhaseTxs.contains(tx)){
            // get shard from the node
            int shard = ((PBFTShardedNode)from).getShardNumber();
            // get the aborts for this tx and increment the vote by 1
            this.txToAborts.get(tx).put(shard, this.txToAborts.get(tx).get(shard) + 1);
            // check if any of the no of aborts is greater than f + 1
            if(txToAborts.get(tx).values().stream().anyMatch(x -> x > ((PBFTShardedNetwork)node.getNetwork()).getF())){
                // unlock the accounts
                ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
                accounts.addAll(tx.getAllInvolvedAccounts());
                for (EthereumAccount account : accounts) {
                    if (lockedAccountsToTransactions.get(account) == tx) {
                        lockedAccounts.remove(account);
                        lockedAccountsToTransactions.remove(account);
                    }
                }
                // send aborted message to client
                CoordinationMessage message = new CoordinationMessage(tx, "aborted");
                node.sendMessageToNode(message, preparedTxsFrom.get(tx));
                // remove tx from second phase list
                secondPhaseTxs.remove(tx);
            }
        }
    }

    private void processCommit(Node from, EthereumTx tx) {
        if(secondPhaseTxs.contains(tx)){
            // get shard from the node
            int shard = ((PBFTShardedNode)from).getShardNumber();
            // get the commits for this tx and increment the vote by 1
            this.txToCommits.get(tx).put(shard, this.txToCommits.get(tx).get(shard) + 1);
            // check if any of the no of commits is greater than f + 1
            if(txToCommits.get(tx).values().stream().anyMatch(x -> x > ((PBFTShardedNetwork)node.getNetwork()).getF())){
                // add tx to mempool
                node.broadcastTransactionToShard(tx, node.getShardNumber());
                // remove tx from second phase list
                secondPhaseTxs.remove(tx);
            }
        }
    }

    public void processConfirmedBlock(PBFTBlock block) {
        // get the transactions from the block
        ArrayList<EthereumTx> transactions = block.getTransactions();
        for (EthereumTx tx : transactions) {
            if(preparedTxsFrom.containsKey(tx)){
                ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
                // this needs upgrading to handle smart contract transactions
                accounts.addAll(tx.getAllInvolvedAccounts());
                // unlock the accounts
                for (EthereumAccount account : accounts) {
                    lockedAccounts.remove(account);
                    // System.out.println("Unlocking account " + account);
                }
                // send a committed message to the client node
                CoordinationMessage message = new CoordinationMessage(tx, "committed");
                node.sendMessageToNode(message, preparedTxsFrom.get(tx));
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
