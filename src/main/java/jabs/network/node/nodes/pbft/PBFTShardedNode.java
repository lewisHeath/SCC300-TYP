package jabs.network.node.nodes.pbft;

import jabs.consensus.blockchain.LocalBlockTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jabs.consensus.algorithm.ClientLedCrossShardConsensus;
import jabs.consensus.algorithm.CrossShardConsensus;
import jabs.consensus.algorithm.PBFT;
import jabs.consensus.algorithm.ShardLedCrossShardConsensus;
import jabs.ledgerdata.TransactionFactory;
import jabs.ledgerdata.Vote;
import jabs.ledgerdata.Sharding.Recipt;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.pbft.PBFTBlock;
import jabs.network.message.CoordinationMessage;
import jabs.network.message.DataMessage;
import jabs.network.message.Message;
import jabs.network.message.Packet;
import jabs.network.networks.Network;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.PeerBlockchainNode;
import jabs.network.node.nodes.Node;
import jabs.network.p2p.ShardedPBFTP2P;
import jabs.simulator.Simulator;

public class PBFTShardedNode extends PeerBlockchainNode<PBFTBlock, EthereumTx> {
    public static final PBFTBlock PBFT_GENESIS_BLOCK = new PBFTBlock(0, 0, 0, null, null);
    
    private int shardNumber;
    protected CrossShardConsensus crossShardConsensus;
    protected ArrayList<EthereumTx> mempool;
    protected HashMap<EthereumTx, Node> txToSender;
    protected ArrayList<Recipt> recipts;
    protected HashMap<EthereumAccount, Boolean> lockedAccounts;
    protected ArrayList<EthereumAccount> shardAccounts;

    public PBFTShardedNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth,
            int nodesPerShard, int shardNumber) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth,
                new ShardedPBFTP2P(),
                new PBFT<>(new LocalBlockTree<>(PBFT_GENESIS_BLOCK), nodesPerShard));
        this.consensusAlgorithm.setNode(this);
        this.shardNumber = shardNumber;
        this.mempool = new ArrayList<>();
        this.txToSender = new HashMap<>();
        this.recipts = new ArrayList<>();
        this.lockedAccounts = new HashMap<>();
        this.shardAccounts = new ArrayList<>();
        // this needs to be mofified to support shard led
        this.crossShardConsensus = new ShardLedCrossShardConsensus(this);
        // System.out.println("Node " + this.nodeID + " in shard: " + shardNumber + " has been created");
    }

    @Override
    public void processNewTx(EthereumTx tx, Node from) {
        // System.out.println("Node: " + this.nodeID + " received tx " + " from Node: " + from.getNodeID() + " in shard: " + shardNumber);
        // for now assuming this only happens when another shard sends the transaction to this shard
        // add it to the mempool
        if(from instanceof PBFTShardedNode) {
            if (((PBFTShardedNode)from).getShardNumber() != this.shardNumber) {
                System.out.println("CRITICAL ERROR: transaction from another shard sent to this shard");
            }
        }  
        // System.out.println("Node: " + this.nodeID + " received tx " + " from Node: " + from.getNodeID() + " in shard: " + shardNumber);
        this.mempool.add(tx);
        // broadcast to the other peers in this shard
        // this.broadcastTransactionToShard(tx, shardNumber);
        // add this transaction along with the client it was sent from to a list
        this.txToSender.put(tx, from);
        // System.out.println("Mempool size: " + this.mempool.size() + " shard: " + shardNumber);
    }

    public void processNewRecipt(Recipt recipt, Node from) {
        // if this node has not seen the recipt before
        if (!this.recipts.contains(recipt)) {
            // for now assuming this only happens when another shard sends the transaction to this shard
            System.out.println("Node: " + this.nodeID + " received recipt " + " from Node: " + from.getNodeID() + " in shard: " + shardNumber);
            // add it to the list of proofs for cross shard transactions
            this.recipts.add(recipt);
            // create a transaction from null to the receiver to credit the account
            EthereumTx tx = recipt.getTx();
            EthereumTx newTx = new EthereumTx(tx.getSize(), tx.getGas());
            newTx.setSender(null);
            newTx.setReceiver(tx.getReceiver());
            // add this tx to the top of the mempool
            this.mempool.add(0, newTx);
            // broadcast to the other peers in this shard
            // this.broadcastRecipt(recipt);
        }
    }

    public void processCoordinationMessage(CoordinationMessage message, Node from) {
        // pass this down to the cross shard consensus algorithm
        this.crossShardConsensus.processCoordinationMessage(message, from);
    }

    @Override
    protected void processNewBlock(PBFTBlock block) {
        // nothing for now
    }

    public void processConfirmedBlock(PBFTBlock block) {
        // pass it to the cross shard consensus algorithm to handle the committed messages
        this.crossShardConsensus.processConfirmedBlock(block);
        // pass it to the method to remove the transactions from the mempool
        this.removeTransactionsFromMempool(block);
        // process the intra shard transactions and tell the client they are confirmed
        this.processIntraShardTxsFromBlock(block);
    }

    private void processIntraShardTxsFromBlock(PBFTBlock block) {
        // for each tx in the block, work out if it is intra shard or not
        for(EthereumTx tx : block.getTransactions()){
            // this will change for smart contract txs...
            ArrayList<EthereumAccount> accounts = new ArrayList<>();
            accounts.addAll(tx.getAllInvolvedAccounts());
            // check if they are in the same shard
            ArrayList<Integer> shards = new ArrayList<>();
            for(EthereumAccount account : accounts) {
                if(!shards.contains(account.getShardNumber())){
                    shards.add(account.getShardNumber());
                }
            }
            // if the size of the shards list is 1 then they are in the same shard
            if(shards.size() == 1){
                // process tx
                Node from = this.txToSender.get(tx);
                // send a committed intra-shard message to this node
                CoordinationMessage message = new CoordinationMessage(tx, "intra-shard-committed");
                // send the message
                if(from != null){
                    // System.out.println("Sending intra shard confirm to Node ID: " + from.getNodeID());
                    this.broadcastMessageToNode(message, from);
                } else {
                    // System.out.println("CRITICAL ERROR: from node is null");
                }
            }
        }
    }

    @Override
    protected void processNewVote(Vote vote) {
        // System.out.println("Sharded PBFT node processing vote");
        ((PBFT<PBFTBlock, EthereumTx>) this.consensusAlgorithm).newIncomingVote(vote);
    }

    @Override
    public void generateNewTransaction() {
        // nothing for now
    }

    public int getShardNumber() {
        return this.shardNumber;
    }

    protected void fillMempool(int numTxs) {
        for (int i = 0; i < numTxs; i++) {
            EthereumTx tx = TransactionFactory.sampleEthereumTransaction(network.getRandom());
            // get 2 random accounts from the network
            // make sure sender account is this shard
            EthereumAccount sender = ((PBFTShardedNetwork) this.network).getRandomAccountFromShard(this.shardNumber);
            EthereumAccount receiver = ((PBFTShardedNetwork) network).getRandomAccount();
            tx.setSender(sender);
            tx.setReceiver(receiver);
            // this.txToSender.put(tx, ((PBFTShardedNetwork) network).getRandomClient());
            mempool.add(tx);
        }
        // System.out.println("Mempool size: " + this.mempool.size());
    }

    public PBFTBlock createBlock() {
        // System.out.println("Node " + this.nodeID + " in shard: " + shardNumber + " creating block");
        ArrayList<EthereumTx> txs = new ArrayList<>();
        int gas = 0;
        int size = 0;
        if(this.mempool.size() ==0){
            // System.out.println("Mempool is empty");
        }
        // fill list of txs with txs from the mempool
        while (gas < 10000000 && this.mempool.size() > 0) {
            // add top tx from mempool to transaction list
            EthereumTx tx = this.mempool.get(0);
            this.mempool.remove(0);
            txs.add(tx);
            gas += tx.getGas();
            size += tx.getSize();
        }
        if(size == 0) size = 1000;
        // System.out.println("Node " + this.nodeID + " in shard: " + shardNumber + " creating block with size: " + size);
        // create a new block
        PBFTBlock block = new PBFTBlock(size * 100, this.consensusAlgorithm.getCanonicalChainHead().getHeight() + 1, simulator.getSimulationTime(), this, this.consensusAlgorithm.getCanonicalChainHead());
        // add the transactions to the block
        block.setTransactions(txs);
        removeTransactionsFromMempool(block);
        return block;
    }

    public void handleCrossShardTransactions(ArrayList<Recipt> recipts) {
        // for each recipt
        for (Recipt recipt : recipts) {
            // get the transaction
            EthereumTx tx = recipt.getTx();
            // get the recipt
            EthereumTx proof = recipt.getProof();
            // get the receiver account
            EthereumAccount receiver = tx.getReceiver();
            // get the shard number of the receiver
            int shardNumber = receiver.getShardNumber();
            // get the node in that shard
            // PBFTShardedNode node = ((PBFTShardedNetwork)network).getRandomNodeInShard(shardNumber);
            // System.out.println("Sending cross shard transaction to shard: " + shardNumber + " from shard: " + this.shardNumber);
            /* 
             * TODO: this is where the cross shard coordination happens, for now we just send the transaction to the other shard
             * nodes need a function to reveive an array of a transaction and its corresponding recipt from the other shard
             */
            // send the recipt to the shard
            // this.broadcastMessageToNode(new DataMessage(recipt), node);
            // Node client = txToSender.get(tx);
            // for now get a random client
            Node client = ((PBFTShardedNetwork)network).getRandomClient();
            // send the recipt back to the client
            // this.broadcastMessageToNode(new DataMessage(recipt), client);
        }
        // clear the list of recipts
        this.recipts.clear();
    }

    public void setMempool(ArrayList<EthereumTx> mempool) {
        this.mempool = mempool;
        // System.out.println("Mempool size: " + this.mempool.size());
    }

    public void removeTransactionsFromMempool(PBFTBlock block) {
        for (EthereumTx tx : block.getTransactions()) {
            this.mempool.remove(tx);
            // System.out.println("Mempool size: " + this.mempool.size());
        }
        // here we need to tell the cross shard consensus algorith to unlock the accounts or do it in another function called form PBFT consensus TODO
    }

    public ArrayList<EthereumAccount> getShardAccounts() {
        return this.shardAccounts;
    }

    public void setShardAccounts(ArrayList<EthereumAccount> shardAccounts) {
        this.shardAccounts = shardAccounts;
    }

    /**
     * method to send a message to every node in a different shard
     * @param message
     * @param shardNumber
     */
    public void broadcastMessageToShard(Message message, int shardNumber) {
        // get the list of nodes from that shard
        ArrayList<PBFTShardedNode> nodes = ((PBFTShardedNetwork)network).getShard(shardNumber);
        for (PBFTShardedNode node : nodes) {
            // send message to each node in the shard
            this.networkInterface.addToUpLinkQueue(
                new Packet(this, node, message)
            );
        }
    }

    protected void broadcastRecipt(Recipt recipt) {
        // broadcast the recipt to the shard
        this.broadcastMessage(new DataMessage(recipt), this);
    }

    public void broadcastTransactionToShard(EthereumTx tx, int shardNumber) {
        // System.out.println("Sharded PBFT node broadcasting transaction");
        // broadcast transaction to all nodes in the specified shard
        broadcastMessageToShard(new DataMessage(tx), shardNumber);
    }

    protected void broadcastTransactionToNode(EthereumTx tx, Node node) {
        // System.out.println("Sharded PBFT node broadcasting transaction");
        this.broadcastMessageToNode(new DataMessage(tx), node);
    }

    protected void broadcastMessageToNode(Message message, Node node) {
        // send the message to the node
        this.networkInterface.addToUpLinkQueue(
            new Packet(this, node, message)
        );
    }

    protected void broadcastTransaction(EthereumTx tx, Node excludeNeighbor) {
        // System.out.println("Sharded PBFT node broadcasting transaction");
        // broadcast transaction to all nodes in the shard
        broadcastMessage(new DataMessage(tx), excludeNeighbor);
    }

    public void broadcastMessage(Message message, Node excludeNeighbor) {
        for (Node neighbor : this.p2pConnections.getNeighbors()) {
            if (neighbor != excludeNeighbor) {
                this.networkInterface.addToUpLinkQueue(
                        new Packet(this, neighbor,
                                message
                        )
                );
            }
        }
    }

    public void sendMessageToNode(Message message, Node node) {
        this.networkInterface.addToUpLinkQueue(
            new Packet(this, node, message)
        );
    }

    public CrossShardConsensus getCrossShardConsensus() {
        return this.crossShardConsensus;
    }
}