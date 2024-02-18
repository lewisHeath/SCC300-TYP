package jabs.ledgerdata.pbft;
import jabs.ledgerdata.Block;
import jabs.network.node.nodes.Node;

public class PBFTMigration_Request <B extends Block<B>> extends PBFTBlockVote<B> {
    protected PBFTMigration_Request(Node voter, B block) {
        super(block.getSize() + PBFT_VOTE_SIZE_OVERHEAD, voter, block, VoteType.MIGRATION_REQUEST);
        //TODO Auto-generated constructor stub
    }

}