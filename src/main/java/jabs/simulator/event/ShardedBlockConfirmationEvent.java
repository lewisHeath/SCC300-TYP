package jabs.simulator.event;

import jabs.ledgerdata.Block;
import jabs.ledgerdata.pbft.PBFTBlock;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.pbft.PBFTShardedNode;

public class ShardedBlockConfirmationEvent extends BlockConfirmationEvent {

    private int shard;
    private int transactionsInBlock;

    public ShardedBlockConfirmationEvent(double time, Node node, Block block) {
        super(time, node, block);
        this.shard = ((PBFTShardedNode) node).getShardNumber();
        this.transactionsInBlock = ((PBFTBlock) block).getTransactions().size();
    }

    public int getShard() {
        return shard;
    }

    public int getTransactionsInBlock() {
        return transactionsInBlock;
    }
    
}
