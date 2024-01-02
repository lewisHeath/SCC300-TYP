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
import jabs.simulator.event.AccountLockingEvent;
import jabs.simulator.event.AccountUnlockingEvent;

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
    private ArrayList<EthereumTx> prePrepareSent = new ArrayList<EthereumTx>();
    private PHASE phase = PHASE.PRE_PREPARING;
    private Boolean sentPrePrepare = false;
    private int nodesInShard;
    private int thisID;

    private PBFTShardedNetwork network;

    private enum PHASE {
        PRE_PREPARING,
        PREPARING
    }

    public ShardLedCrossShardConsensus(PBFTShardedNode node){
        this.viewNumber = 0;
        this.node = node;
        this.network = (PBFTShardedNetwork)node.getNetwork();
        this.nodesInShard = ((PBFTShardedNetwork) node.getNetwork()).getNodesPerShard();
    }

    public void setID(int ID){
        this.thisID = ID;
    }

    public void processCoordinationMessage(CoordinationMessage message, Node from) {
        /*
         * 1. message types ("pre-prepare", "prepareOK", "prepareNOTOK", "commit", "abort")
         * 2. based on the message, pass to correct method
         * 3. get the accounts from the tx that are in this shard
         */

        EthereumTx tx = (EthereumTx) message.getData();
        Node clientFrom = message.getFrom();

        this.handleDataStructures(tx, message);

        switch (message.getType()) {
            case "pre-prepare":
                processPrePrepare(clientFrom, tx, from);
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

    private void processPrePrepare(Node clientFrom, EthereumTx tx, Node sentFrom) {

        // System.out.println(this.viewNumber % this.nodesInShard + " " + this.node.getNodeID());
        // System.out.println("Node: --" + node.getNodeID() + "-- received pre-prepare from Node: --" + sentFrom.getNodeID() + "-- in view: --" + this.viewNumber + "-- | " + this.viewNumber % this.nodesInShard + " | for tx: ----" + tx + "----");

        ArrayList<EthereumAccount> accounts = this.addTxToShardList(tx);

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


        Boolean sentFromClient = false;
        if(clientFrom == sentFrom){
            sentFromClient = true;
        }

        // if this node is the leader and it hasnt yet sent a pre-prepare to the rest of the shard, send it
        // if (thisID == 0 && sentFromClient) {
        //     // broadcast the tx to all nodes in this shard
        //     // System.out.println("Node " + node.getNodeID() + " is the leader and is sending a pre-prepare message for tx " + tx);
        //     CoordinationMessage message = new CoordinationMessage(tx, "pre-prepare", clientFrom);
        //     node.broadcastMessage(message);
        //     sentPrePrepare = true;
        //     this.phase = PHASE.PREPARING;
        //     // System.out.println("Sent pre-prepare in the function at view " + this.viewNumber + ", Node ID: " + node.getNodeID() + " for tx: " + tx);
        //     return;
        // }

        // if the message has not come from a client, it needs to be processed and then a prepareOK or prepareNOTOK sent
        // if (!sentFromClient && !prePrepareSent.contains(tx) && thisID == 0) {
        if (thisID == 0) {
            // System.out.println("Received pre-prepare at view " + this.viewNumber);
            prePrepareSent.add(tx);
            System.out.println("processing tx: " + tx + " in shard " + node.getShardNumber());
            // if this has already been aborted before even seeing it in a pre-prepare, do not process it
            if (txToAborts.get(tx).values().stream().anyMatch(x -> x > ((PBFTShardedNetwork) node.getNetwork()).getF())){
                System.out.println("already aborted: " + tx);
                return;
            }
            // check if the accounts are locked
            if (areAccountsLocked(accountsInThisShard)) {
                // send prepareNOTOK
                // System.out.println("Node: --" + node.getNodeID() + "-- sent prepareNOTOK in view: --" + this.viewNumber + "-- | " + this.viewNumber % this.nodesInShard + " | for tx: ----" + tx + "----");
                CoordinationMessage message = new CoordinationMessage(tx, "prepareNOTOK", clientFrom);
                // System.out.println("Sent prepareNOTOK at view " + this.viewNumber);
                // send to all nodes in this shard
                // FOR EVERY NODE
                ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
                // // GET CROSS SHARD CONSENSUS AND FORCE MESSAGE
                // for(PBFTShardedNode node : nodes) {
                //     // node.broadcastMessage(message);
                //     // send abort message to every shard involved
                //     this.sendCommitOrAbort("abort", tx);
                // }
                // System.out.println("Accounts are locked");

                // tell each shard that the account 

                this.sendCommitOrAbort("abort", tx);
                // print how many locked accounts there are 
                System.out.println("Locked accounts: " + lockedAccounts.size() + " in shard " + node.getShardNumber() + " for tx " + tx);

                return;
            }
            // lock the accounts
            System.out.println("Locked accounts before: " + lockedAccounts.size() + " in shard " + node.getShardNumber() + " for tx " + tx);
            for (EthereumAccount account : accountsInThisShard) {
                lockedAccounts.add(account);
                lockedAccountsToTransactions.put(account, tx);

                // little trick, tell the other shards that the account was locked for this tx




                // locking account event
                System.out.println("Locking account" + account);
                AccountLockingEvent event = new AccountLockingEvent(this.node.getSimulator().getSimulationTime(), account, this.node);
                this.node.getSimulator().putEvent(event, 0);
            }
            System.out.println("Locked accounts after: " + lockedAccounts.size() + " in shard " + node.getShardNumber() + " for tx " + tx);
            // send prepareOK
            // System.out.println("Node: --" + node.getNodeID() + "-- sent prepareOK in view: --" + this.viewNumber + "-- | " + this.viewNumber % this.nodesInShard + " | for tx: ----" + tx + "----");
            CoordinationMessage message = new CoordinationMessage(tx, "prepareOK", clientFrom);
            // System.out.println("Sent prepareOK at view " + this.viewNumber);
            // send to all nodes in this shard
            // FOR EVERY NODE
            ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
            // GET CROSS SHARD CONSENSUS AND FORCE MESSAGE
            for (PBFTShardedNode node : nodes) {
                node.broadcastMessage(message);
            }
            // System.out.println("Accounts are not locked");
            return;
        }
    }

    private void processPrepareOK(Node from, EthereumTx tx) {
        // System.out.println("Received prepareOK, this ID: " + node.getNodeID() + " from ID: " + from.getNodeID() + " for tx: " + tx);
        // if the list is null, then initialise the list
        if(this.txToAgreedNodes.get(tx) == null) {
            ArrayList<Node> newAgreedNodes = new ArrayList<Node>();
            this.txToAgreedNodes.put(tx, newAgreedNodes);
            if(!preparedTxs.contains(tx)){
                preparedTxs.add(tx);
            }
            this.addTxToShardList(tx);
        }
        // add the node to the agreed nodes
        ArrayList<Node> agreedNodes = txToAgreedNodes.get(tx);
        // check if the node is already in the list
        if (!agreedNodes.contains(from)) {
            agreedNodes.add(from);
        }
        // check if the no of agreed nodes is greater than 2f 
        if(agreedNodes.size() > ((PBFTShardedNetwork)node.getNetwork()).getF() && preparedTxs.contains(tx)){
            // send commit message to all shards in the tx
            this.sendCommitOrAbort("commit", tx);
            // add to phase two
            secondPhaseTxs.add(tx);
            // increment view number
            this.viewNumber++;
            // reset the sentPrePrepare flag
            this.sentPrePrepare = false;
            // System.out.println("Node: " + node.getNodeID() + " now at view: " + viewNumber);
            this.phase = PHASE.PRE_PREPARING;
            // if this node is the primary, then propose a new tx
            this.trySendPrePrepare();
        }
    }

    private void processPrepareNOTOK(Node from, EthereumTx tx) {
        // System.out.println("Received prepareNOTOK, this ID: " + node.getNodeID() + " from ID: " + from.getNodeID() + " for tx: " + tx);
        // if the list is null, then initialise the list
        if (this.txToDisagreedNodes.get(tx) == null) {
            ArrayList<Node> newDisagreedNodes = new ArrayList<Node>();
            this.txToDisagreedNodes.put(tx, newDisagreedNodes);
            if (!preparedTxs.contains(tx)) {
                preparedTxs.add(tx);
            }
            this.addTxToShardList(tx);
        }
        // add the node to the disagreed nodes
        ArrayList<Node> disagreedNodes = txToDisagreedNodes.get(tx);
        // check if the node is already in the list
        if (!disagreedNodes.contains(from)) {
            disagreedNodes.add(from);
        }
        // check if the no of disagreed nodes is greater than 2f
        if(disagreedNodes.size() > ((PBFTShardedNetwork)node.getNetwork()).getF() && preparedTxs.contains(tx)){
            // send abort message to all shards in the tx
            this.sendCommitOrAbort("abort", tx);
            // increment view number
            this.viewNumber++;
            // reset the sentPrePrepare flag
            this.sentPrePrepare = false;
            this.phase = PHASE.PRE_PREPARING;
            // if this node is the primary, then propose a new tx
            this.trySendPrePrepare();
        }
    }

    private void sendCommitOrAbort(String type, EthereumTx tx){
        if(thisID == 0){
            ArrayList<Integer> shards = txToShards.get(tx);
            for (Integer shard : shards) {
                CoordinationMessage message = new CoordinationMessage(tx, type, preparedTxsFrom.get(tx));
                // FORCE EVERY NODE
                ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
                // GET CROSS SHARD CONSENSUS AND FORCE MESSAGE
                for (PBFTShardedNode node : nodes) {
                    node.broadcastMessageToShard(message, shard);
                }
            }
        }
        // remove the tx from the prepared txs
        preparedTxs.remove(tx);
    }

    private void processAbort(Node from, EthereumTx tx) {
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
                // if the account is locked for this tx, then unlock it
                if(lockedAccountsToTransactions.get(account) != null && lockedAccountsToTransactions.get(account).equals(tx)){
                    // remove every duplicate account
                    while(lockedAccounts.contains(account)){
                        System.out.println("Account: " + account + " unlocked" + " in the abort phase");
                        lockedAccounts.remove(account);
                    }
                    lockedAccountsToTransactions.remove(account);
                    // System.out.println("Account: " + account + " unlocked" + " in the abort phase");
                }
                // lockedAccounts.remove(account);
                // lockedAccountsToTransactions.remove(account);
                // System.out.println("Account: " + account + " unlocked" + " in the abort phase");
            }
            // lockedAccounts.removeAll(accounts);
            // send aborted message to client
            CoordinationMessage message = new CoordinationMessage(tx, "aborted");
            if(thisID == 0){
                // FOR EVERY NODE
                ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
                // GET CROSS SHARD CONSENSUS AND FORCE MESSAGE
                for (PBFTShardedNode node : nodes) {
                    node.sendMessageToNode(message, preparedTxsFrom.get(tx));
                }
            }
            // remove tx from second phase list
            secondPhaseTxs.remove(tx);
        }
    }

    private void processCommit(Node from, EthereumTx tx) {
        // System.out.println("Received commit, this ID: " + node.getNodeID() + " from ID: " + from.getNodeID() + " for tx: " + tx);
        // get shard from the node
        int shard = ((PBFTShardedNode)from).getShardNumber();
        // get the commits for this tx and increment the vote by 1
        this.txToCommits.get(tx).put(shard, this.txToCommits.get(tx).get(shard) + 1);
        // check if any of the no of commits is greater than f + 1
        if(txToCommits.get(tx).values().stream().allMatch(x -> x > ((PBFTShardedNetwork)node.getNetwork()).getF()) && secondPhaseTxs.contains(tx)){
            // add tx to mempool
            node.processNewTx(tx, from);
            // remove tx from second phase list
            secondPhaseTxs.remove(tx);
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
                if(thisID == 0) System.out.println("size of locked accounts before confirming transaction: " + lockedAccounts.size());
                for (EthereumAccount account : accounts) {
                    lockedAccounts.remove(account);
                    while(lockedAccounts.contains(account)){
                        lockedAccounts.remove(account);
                        System.out.println("Unlocking account " + account);
                    }
                    // Unlocking account event
                    AccountUnlockingEvent event = new AccountUnlockingEvent(this.node.getSimulator().getSimulationTime(), account, this.node);
                    this.node.getSimulator().putEvent(event, 0);
                }
                lockedAccounts.removeAll(accounts);
                if(thisID == 0) System.out.println("size of locked accounts after confirming transaction: " + lockedAccounts.size());
                // send a committed message to the client node
                CoordinationMessage message = new CoordinationMessage(tx, "committed");
                // System.out.println("Sending committed message to client node " + preparedTxsFrom.get(tx).getNodeID() + " for tx " + tx + " from node " + node.getNodeID());
                if (thisID == 0) {
                    // FOR EVERY NODE
                    ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
                    // GET CROSS SHARD CONSENSUS AND FORCE MESSAGE
                    for (PBFTShardedNode node : nodes) {
                        node.sendMessageToNode(message, preparedTxsFrom.get(tx));
                    }
                }
            }
        }
    }

    public Boolean areAccountsLocked(ArrayList<EthereumAccount> accounts) {
        for (EthereumAccount account : accounts) {
            if (lockedAccounts.contains(account)) {
                System.out.println("Account " + account + " is locked");
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

    private void trySendPrePrepare() {
        // get the first tx from preparedTxs
        if(preparedTxs.size() == 0 || this.thisID != 0){
            return;
        }
        EthereumTx tx = preparedTxs.get(0);
        int thisID = ((PBFTShardedNetwork) node.getNetwork()).getIndexOfNode(node, node.getShardNumber());
        // if this is not null then send pre-prepare
        if (thisID == this.viewNumber % nodesInShard) {
            // broadcast the tx to all nodes in this shard
            Node from = preparedTxsFrom.get(tx);
            CoordinationMessage message = new CoordinationMessage(tx, "pre-prepare", from);
            node.broadcastMessage(message);
            sentPrePrepare = true;
            this.phase = PHASE.PREPARING;
            // System.out.println("Sent pre-prepare in try " + tx + " from node " + node.getNodeID() + " at view " + this.viewNumber);
            // System.out.println("Sent pre-prepare in the try at view " + this.viewNumber + ", Node ID: " + node.getNodeID() + " for tx: " + tx);
        }
    }

    private ArrayList<EthereumAccount> addTxToShardList(EthereumTx tx){
        ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
        accounts.addAll(tx.getAllInvolvedAccounts());
        // make a list of the shards involved in this tx
        ArrayList<Integer> shards = this.getShardsFromListOfAccounts(accounts);
        this.txToShards.put(tx, shards);
        return accounts;
    }

    private ArrayList<Integer> getShardsFromListOfAccounts(ArrayList<EthereumAccount> accounts){
        ArrayList<Integer> shards = new ArrayList<Integer>();
        for (EthereumAccount account : accounts) {
            if (!shards.contains(account.getShardNumber())) {
                shards.add(account.getShardNumber());
            }
        }
        return shards;
    }

    private void handleDataStructures(EthereumTx tx, CoordinationMessage message) {
        Node clientFrom = message.getFrom();
        ArrayList<EthereumAccount> accounts = this.addTxToShardList(tx);
        ArrayList<Integer> shards = this.getShardsFromListOfAccounts(accounts);
        // add to seen txs
        if(!seenTxs.contains(tx)) {
            seenTxs.add(tx);
        }
        // add to preparedTxs from
        if(!preparedTxs.contains(tx)){
            preparedTxsFrom.put(tx, clientFrom);
        }
        // add to txToAborts
        if(txToAborts.get(tx) == null){
            HashMap<Integer, Integer> aborts = new HashMap<Integer, Integer>();
            for (Integer shard : shards) {
                aborts.put(shard, 0);
            }
            txToAborts.put(tx, aborts);
        }
        // add to txToCommits
        if(txToCommits.get(tx) == null){
            HashMap<Integer, Integer> commits = new HashMap<Integer, Integer>();
            for (Integer shard : shards) {
                commits.put(shard, 0);
            }
            txToCommits.put(tx, commits);
        }
    }

    @Override
    public void migrateAccount(EthereumAccount accounts) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'migrateAccount'");
    }

}
