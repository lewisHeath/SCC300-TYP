package jabs.ledgerdata.pbft;

import jabs.ledgerdata.Block;
import jabs.network.node.nodes.Node;

public class PBFTMigration_Approved <B extends Block<B>> extends PBFTBlockVote<B> {
    protected PBFTMigration_Approved(Node voter, B block) {
        super(block.getSize() + PBFT_VOTE_SIZE_OVERHEAD, voter, block, VoteType.MIGRATION_APPROVED);
        //TODO Auto-generated constructor stub
    }

}
