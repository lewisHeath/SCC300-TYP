package jabs.network.node.nodes.pbft;

import jabs.consensus.blockchain.LocalBlockTree;
import jabs.consensus.algorithm.PBFT;
import jabs.ledgerdata.Vote;
import jabs.ledgerdata.pbft.PBFTBlock;
import jabs.ledgerdata.pbft.PBFTTx;
import jabs.network.networks.Network;
import jabs.network.node.nodes.PeerBlockchainNode;
import jabs.network.node.nodes.Node;
import jabs.network.p2p.ShardedPBFTP2P;
import jabs.simulator.Simulator;

public class PBFTShardedNode extends PeerBlockchainNode<PBFTBlock, PBFTTx> implements IPBFTNode{
    public static final PBFTBlock PBFT_GENESIS_BLOCK = new PBFTBlock(0, 0, 0, null, null);
    
    private int shardNumber;

    public PBFTShardedNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth,
            int nodesPerShard, int shardNumber) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth,
                new ShardedPBFTP2P(),
                new PBFT<>(new LocalBlockTree<>(PBFT_GENESIS_BLOCK), nodesPerShard));
        this.consensusAlgorithm.setNode(this);
        this.shardNumber = shardNumber;
    }

    @Override
    protected void processNewTx(PBFTTx tx, Node from) {
        // nothing for now
    }

    @Override
    protected void processNewBlock(PBFTBlock block) {
        // nothing for now
    }

    @Override
    protected void processNewVote(Vote vote) {
        // System.out.println("Sharded PBFT node processing vote");
        ((PBFT<PBFTBlock, PBFTTx>) this.consensusAlgorithm).newIncomingVote(vote);
    }

    @Override
    public void generateNewTransaction() {
        // nothing for now
    }

    public int getShardNumber() {
        return this.shardNumber;
    }
}