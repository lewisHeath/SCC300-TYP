package jabs.network.node.nodes.pbft;

import jabs.consensus.blockchain.LocalBlockTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import jabs.consensus.algorithm.PBFT;
import jabs.ledgerdata.TransactionFactory;
import jabs.ledgerdata.Vote;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.pbft.PBFTBlock;
import jabs.ledgerdata.pbft.PBFTTx;
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
    protected HashSet<EthereumTx> mempool;

    public PBFTShardedNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth,
            int nodesPerShard, int shardNumber) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth,
                new ShardedPBFTP2P(),
                new PBFT<>(new LocalBlockTree<>(PBFT_GENESIS_BLOCK), nodesPerShard));
        this.consensusAlgorithm.setNode(this);
        this.shardNumber = shardNumber;
        this.mempool = new HashSet<>();
        // fill mempool with ethereum transactions
        fillMempool(100000);
    }

    @Override
    protected void processNewTx(EthereumTx tx, Node from) {
        // for now assuming this only happens when another shard sends the transaction to this shard
        // add it to the mempool
        this.mempool.add(tx);
        // broadcast to the other peers in this shard
        this.broadcastTransaction(tx, from);
    }

    // processNewCrossShardTransaction(EthCrossShardTx tx, Node from) {
    //     // add it to the mempool
    //     this.mempool.add(tx);
    //     // broadcast to the other peers in this shard
    //     this.broadcastTransaction(tx, from);
    // }

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
            this.mempool.add(TransactionFactory.sampleEthereumTransaction(network.getRandom()));
        }
        System.out.println("Mempool size: " + this.mempool.size());
    }

    public PBFTBlock createBlock() {
        // list of all the recipts ( for now send the other half of transactions to the other shards )
        HashMap<EthereumTx, EthereumTx> recipts = new HashMap<>();
        // System.out.println("Sharded PBFT node creating block");
        // gather maximum 1000000 gas worth of transactions from mempool
        HashSet<EthereumTx> txs = new HashSet<>();
        int gas = 0;
        for (EthereumTx tx : this.mempool) {
            // System.out.println("Gas: " + tx.getGas());
            if (gas + tx.getGas() <= 10000000) { 
                // get 2 random accounts from the network
                EthereumAccount sender = ((PBFTShardedNetwork)network).getRandomAccount();
                EthereumAccount receiver = ((PBFTShardedNetwork)network).getRandomAccount();
                tx.setSender(sender);
                tx.setReceiver(receiver);
                // check if it is a cross shard transaction
                if(sender.getShardNumber() == this.shardNumber && receiver.getShardNumber() != this.shardNumber) {
                    System.out.println("Cross shard transaction from account: " + sender.getAccountNumber() + " -> " + receiver.getAccountNumber() + " in shard: " + sender.getShardNumber() + " -> " + receiver.getShardNumber());
                    // PERFORM CROSS SHARD TRANSACTION
                    /*
                    * assuming shards pick transactions where the sender account is in their shard, we first need to debt the sender account and create a 'recipt'
                    * then we add this transaction of debting the senders account and waiting for the other shard to credit the receiver to the block
                    * then the cross shard coordination happens ( however this happens? ) and the receiver account in the other shard is credited
                    */

                    // create a receipt for the transaction and debt the account
                    // clone the tx and set the receiver to null
                    EthereumTx recipt = new EthereumTx(tx.getSize(), tx.getGas());
                    recipt.setSender(sender);
                    recipt.setReceiver(null);
                    // TODO add the amounts
                    
                    // add the recipt transaction to the block
                    txs.add(recipt);
                    // note down this transaction in the list of recipts to know which shards to notify of a cross shard transaction
                    recipts.put(tx, recipt);
                    gas += tx.getGas();

                    ((PBFTShardedNetwork)network).crossShardTransactions++;
                } else if (sender.getShardNumber() == this.shardNumber && receiver.getShardNumber() == this.shardNumber) {
                    System.out.println("Intra shard transaction from account: " + sender.getAccountNumber() + " -> " + receiver.getAccountNumber() + " in shard: " + sender.getShardNumber());
                    // add to the block
                    txs.add(tx);
                    ((PBFTShardedNetwork)network).intraShardTransactions++;
                    gas += tx.getGas();
                }
            }
        }
        // make block with these transactions
        PBFTBlock block = new PBFTBlock(gas, this.consensusAlgorithm.getCanonicalChainHead().getHeight() + 1, simulator.getSimulationTime(), this, this.consensusAlgorithm.getCanonicalChainHead());
        block.setTransactions(txs);
        // return block
        // System.out.println("Sharded PBFT node created block with " + txs.size() + " transactions from shard " + this.shardNumber + ", node ID " + this.nodeID);
        // print block height
        System.out.println("Block height: " + block.getHeight() + " in shard: " + this.shardNumber);
        // handle the cross shard transactions
        this.handleCrossShardTransactions(recipts);
        return block;
    }

    protected void handleCrossShardTransactions(HashMap<EthereumTx, EthereumTx> recipts) {
        // for each recipt
        for (Map.Entry<EthereumTx, EthereumTx> entry : recipts.entrySet()) {
            // get the transaction
            EthereumTx tx = entry.getKey();
            // get the recipt
            EthereumTx recipt = entry.getValue();
            // get the receiver account
            EthereumAccount receiver = tx.getReceiver();
            // get the shard number of the receiver
            int shardNumber = receiver.getShardNumber();
            // get the node in that shard
            PBFTShardedNode node = ((PBFTShardedNetwork)network).getRandomNodeInShard(shardNumber);
            System.out.println("Sending cross shard transaction to shard: " + shardNumber + " from shard: " + this.shardNumber);
            /* 
             * TODO: this is where the cross shard coordination happens, for now we just send the transaction to the other shard
             * nodes need a function to reveive an array of a transaction and its corresponding recipt from the other shard
             */
            // send the transaction and recipt to the shard
        }
    }

    public void removeTransactionsFromMempool(PBFTBlock block) {
        this.mempool.removeAll(block.getTransactions());
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

    protected void broadcastTransactionToShard(EthereumTx tx, int shardNumber) {
        // System.out.println("Sharded PBFT node broadcasting transaction");
        // broadcast transaction to all nodes in the shard
        broadcastMessageToShard(new DataMessage(tx), shardNumber);
    }

    protected void broadcastTransactionToNode(EthereumTx tx, PBFTShardedNode node) {
        // System.out.println("Sharded PBFT node broadcasting transaction");
        // broadcast transaction to all nodes in the shard
        this.networkInterface.addToUpLinkQueue(
            new Packet(this, node, new DataMessage(tx))
        );
    }

    protected void broadcastTransaction(EthereumTx tx, Node excludeNeighbor) {
        for (Node neighbor : this.p2pConnections.getNeighbors()) {
            if (neighbor != excludeNeighbor) {
                this.networkInterface.addToUpLinkQueue(
                        new Packet(this, neighbor,
                                new DataMessage(tx)
                        )
                );
            }
        }
    }
}