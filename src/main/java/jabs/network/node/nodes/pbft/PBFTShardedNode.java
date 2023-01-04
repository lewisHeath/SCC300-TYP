package jabs.network.node.nodes.pbft;

import jabs.consensus.blockchain.LocalBlockTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jabs.consensus.algorithm.PBFT;
import jabs.ledgerdata.TransactionFactory;
import jabs.ledgerdata.Vote;
import jabs.ledgerdata.Sharding.Recipt;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.pbft.PBFTBlock;
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
    protected ArrayList<EthereumTx> mempool;
    protected HashMap<EthereumTx, Node> txToSender;
    protected ArrayList<Recipt> recipts;

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
        System.out.println("Node " + this.nodeID + " in shard: " + shardNumber + " has been created");
    }

    @Override
    protected void processNewTx(EthereumTx tx, Node from) {
        // for now assuming this only happens when another shard sends the transaction to this shard
        // add it to the mempool
        if(from instanceof PBFTShardedNode) {
            if (((PBFTShardedNode)from).getShardNumber() != this.shardNumber) {
                System.out.println("CRITICAL ERROR: transaction from another shard sent to this shard");
            }
        }
        System.out.println("Node: " + this.nodeID + " received tx " + " from Node: " + from.getNodeID() + " in shard: " + shardNumber);
        this.mempool.add(0, tx);
        // broadcast to the other peers in this shard
        this.broadcastTransaction(tx, from);
        // add this transaction along with the client it was sent from to a list
        this.txToSender.put(tx, from);
        System.out.println("Mempool size: " + this.mempool.size() + " shard: " + shardNumber);
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

    @Override
    protected void processNewBlock(PBFTBlock block) {
        // nothing for now
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
        // list of all the recipts ( for now send the other half of transactions to the other shards )
        ArrayList<Recipt> recipts = new ArrayList<>();
        // System.out.println("Sharded PBFT node creating block");
        // gather maximum 1000000 gas worth of transactions from mempool
        ArrayList<EthereumTx> txs = new ArrayList<>();
        int gas = 0;
        // System.out.println("Mempool size when creating block = " + this.mempool.size() + " shard: " + shardNumber);
        System.out.println("Node: " + this.nodeID + " creating block in shard: " + shardNumber);
        for (int i = 0; i < this.recipts.size(); i++) {
            Recipt recipt = this.recipts.get(i);
            // get the transaction from the recipt
            EthereumTx tx = recipt.getTx();
            // make a new transaction from null to the receiver
            EthereumTx newTx = new EthereumTx(tx.getSize(), tx.getGas());
            newTx.setSender(null);
            newTx.setReceiver(tx.getReceiver());
            // add this tx to the list of txs
            txs.add(newTx);
            gas += newTx.getGas();
            // add THIS recipt to the list of recipts to be sent to the client to confirm the commit
            // TODO
        }
        for (int i = 0; i < this.mempool.size(); i++) {
            EthereumTx tx = this.mempool.get(i);
            // System.out.println("Gas: " + tx.getGas());
            if (gas + tx.getGas() <= 10000000) { 
                // check if it is a cross shard transaction
                // txs.add(tx);
                EthereumAccount sender = tx.getSender();
                EthereumAccount receiver = tx.getReceiver();
                if(sender == null && receiver.getShardNumber() == this.shardNumber) {
                    // this is the credit for the cross shard transaction
                    System.out.println("Node: " + this.nodeID + " adding cross shard transaction to block in shard: " + shardNumber);
                    /*
                     * if the account is locked then leave this transaction in the mempool
                     * if not then add it to the block
                     */
                    txs.add(tx);
                    /*
                     * use cross shard transaction object which the nodes can read from the latest blocks and then tell the clients that 
                     * the transaction has been committed and then the client will send the unlock message to both shards
                     */
                }
                else if(sender.getShardNumber() == this.shardNumber && receiver.getShardNumber() != this.shardNumber) {
                    // System.out.println("CROSS-SHARD transaction from account: " + sender.getAccountNumber() + " -> " + receiver.getAccountNumber() + " in shard: " + sender.getShardNumber() + " -> " + receiver.getShardNumber());
                    // PERFORM CROSS SHARD TRANSACTION
                    /*
                    * assuming shards pick transactions where the sender account is in their shard, we first need to debt the sender account and create a 'recipt'
                    * then we add this transaction of debting the senders account and waiting for the other shard to credit the receiver to the block
                    * then the cross shard coordination happens ( however this happens? ) and the receiver account in the other shard is credited
                    */

                    // create a receipt for the transaction and debt the account
                    // clone the tx and set the receiver to null
                    EthereumTx proof = new EthereumTx(tx.getSize(), tx.getGas());
                    proof.setSender(sender);
                    proof.setReceiver(null);
                    Recipt recipt = new Recipt(tx.getSize() * 2, tx, proof);
                    // TODO add the amounts
                    
                    // add the recipt transaction to the block
                    txs.add(proof);
                    // note down this transaction in the list of recipts to know which shards to notify of a cross shard transaction
                    recipts.add(recipt);
                    // System.out.println("Created recipt for cross shard transaction: " + recipt.getTx().getSender().getAccountNumber() + " -> " + recipt.getTx().getReceiver().getAccountNumber() + " in shard: " + recipt.getTx().getSender().getShardNumber() + " -> " + recipt.getTx().getReceiver().getShardNumber());
                    gas += tx.getGas();
                    // txs.add(tx);
                    this.mempool.remove(i);

                    // lock the senders account
                    sender.lock();
                    // System.out.println("Locked account " + sender.getAccountNumber() + " in shard: " + sender.getShardNumber());

                    ((PBFTShardedNetwork)network).crossShardTransactions++;
                } else if (sender.getShardNumber() == this.shardNumber && receiver.getShardNumber() == this.shardNumber) {
                    // System.out.println("INTRA-SHARD transaction from account: " + sender.getAccountNumber() + " -> " + receiver.getAccountNumber() + " in shard: " + sender.getShardNumber());
                    // add to the block
                    txs.add(tx);
                    // this.mempool.remove(i);
                    ((PBFTShardedNetwork)network).intraShardTransactions++;
                    gas += tx.getGas();
                } else if (sender.getShardNumber() != this.shardNumber && receiver.getShardNumber() == this.shardNumber) {
                    // System.out.println("THIS SHOULD NOT HAPPPEN");
                    // ((PBFTShardedNetwork) network).failures++;
                } else {
                    System.out.println("THIS SHOULD NOT HAPPPEN 2");
                    ((PBFTShardedNetwork) network).failures++;
                }
            }
        }
        // make block with these transactions
        PBFTBlock block = new PBFTBlock(gas, this.consensusAlgorithm.getCanonicalChainHead().getHeight() + 1, simulator.getSimulationTime(), this, this.consensusAlgorithm.getCanonicalChainHead());
        block.setTransactions(txs);
        block.setRecipts(recipts);
        // return block
        // System.out.println("Sharded PBFT node created block with " + txs.size() + " transactions from shard " + this.shardNumber + ", node ID " + this.nodeID);
        // print block height
        // System.out.println("Block height: " + block.getHeight() + " in shard: " + this.shardNumber);
        // System.out.println("Mempool size: " + this.mempool.size());
        removeTransactionsFromMempool(block);
        // handle the cross shard transactions
        // this.handleCrossShardTransactions(recipts);
        System.out.println("Block transactions: " + block.getTransactions().size());
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
            this.broadcastMessageToNode(new DataMessage(recipt), client);
        }
        // clear the list of recipts
        this.recipts.clear();
    }

    public void setMempool(ArrayList<EthereumTx> mempool) {
        this.mempool = mempool;
    }

    public void removeTransactionsFromMempool(PBFTBlock block) {
        // TODO: modify to use the length of transactions in block instead of looking at individual transactions
        // System.out.println("Removing transactions from mempool");
        // ArrayList<EthereumTx> txs = block.getTransactions();

        
        int sizeOfTransactions = block.getTransactions().size();
        // remove this many transactions from the front of the mempool
        for (int i = 0; i < sizeOfTransactions; i++) {
            this.mempool.remove(0);
        }


        // for(int i = 0; i < txs.size(); i++) {
        //     EthereumTx tx = txs.get(i);
        //     if(this.mempool.contains(tx)) {
        //         this.mempool.remove(tx);
        //         // System.out.println("Removed transaction from mempool");
        //     }
        // }
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

    protected void broadcastTransactionToShard(EthereumTx tx, int shardNumber) {
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

    protected void broadcastMessage(Message message, Node excludeNeighbor) {
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
}