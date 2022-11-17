package jabs.network.node.nodes.pbft;

import jabs.consensus.blockchain.LocalBlockTree;

import java.util.HashSet;

import jabs.consensus.algorithm.PBFT;
import jabs.ledgerdata.TransactionFactory;
import jabs.ledgerdata.Vote;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.pbft.PBFTBlock;
import jabs.ledgerdata.pbft.PBFTTx;
import jabs.network.networks.Network;
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
        // nothing for now
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
            this.mempool.add(TransactionFactory.sampleEthereumTransaction(network.getRandom()));
        }
        System.out.println("Mempool size: " + this.mempool.size());
    }

    public PBFTBlock createBlock() {
        // System.out.println("Sharded PBFT node creating block");
        // gather maximum 1000000 gas worth of transactions from mempool
        HashSet<EthereumTx> txs = new HashSet<>();
        int gas = 0;
        for (EthereumTx tx : this.mempool) {
            // System.out.println("Gas: " + tx.getGas());
            if (gas + tx.getGas() <= 10000000) {
                txs.add(tx);
                gas += tx.getGas();
            }
        }
        // make block with these transactions
        PBFTBlock block = new PBFTBlock(gas, this.consensusAlgorithm.getCanonicalChainHead().getHeight() + 1, simulator.getSimulationTime(), this, this.consensusAlgorithm.getCanonicalChainHead());
        block.setTransactions(txs);
        // return block
        System.out.println("Sharded PBFT node created block with " + txs.size() + " transactions from shard " + this.shardNumber + ", node ID " + this.nodeID);
        return block;
    }

    public void removeTransactionsFromMempool(PBFTBlock block) {
        this.mempool.removeAll(block.getTransactions());
    }
}