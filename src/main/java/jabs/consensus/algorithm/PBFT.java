package jabs.consensus.algorithm;

import jabs.consensus.blockchain.LocalBlockTree;
import jabs.ledgerdata.*;
import jabs.ledgerdata.Sharding.Recipt;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.pbft.*;
import jabs.network.message.VoteMessage;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.pbft.PBFTNode;
import jabs.network.node.nodes.pbft.PBFTShardedNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

// based on: https://sawtooth.hyperledger.org/docs/pbft/nightly/master/architecture.html
// another good source: http://ug93tad.github.io/pbft/

public class PBFT<B extends SingleParentBlock<B>, T extends Tx<T>> extends AbstractChainBasedConsensus<B, T>
        implements VotingBasedConsensus<B, T>, DeterministicFinalityConsensus<B, T> {
    private final int numAllParticipants;
    private final HashMap<B, HashMap<Node, Vote>> prepareVotes = new HashMap<>();
    private final HashMap<B, HashMap<Node, Vote>> commitVotes = new HashMap<>();
    private final HashSet<B> preparedBlocks = new HashSet<>();
    private final HashSet<B> committedBlocks = new HashSet<>();
    private int currentViewNumber = 0;

    // TODO: View change should be implemented

    private PBFTMode pbftMode = PBFTMode.NORMAL_MODE;
    private PBFTPhase pbftPhase = PBFTPhase.PRE_PREPARING;

    @Override
    public boolean isBlockFinalized(B block) {
        return false;
    }

    @Override
    public boolean isTxFinalized(T tx) {
        return false;
    }

    @Override
    public int getNumOfFinalizedBlocks() {
        return 0;
    }

    @Override
    public int getNumOfFinalizedTxs() {
        return 0;
    }

    public enum PBFTMode {
        NORMAL_MODE,
        VIEW_CHANGE_MODE
    }

    public enum PBFTPhase {
        PRE_PREPARING,
        PREPARING,
        COMMITTING
    }

    public PBFT(LocalBlockTree<B> localBlockTree, int numAllParticipants) {
        super(localBlockTree);
        this.numAllParticipants = numAllParticipants;
        this.currentMainChainHead = localBlockTree.getGenesisBlock();
    }

    public void newIncomingVote(Vote vote) {
        if (vote instanceof PBFTBlockVote) { // for the time being, the view change votes are not supported
            PBFTBlockVote<B> blockVote = (PBFTBlockVote<B>) vote;
            B block = blockVote.getBlock();
            switch (blockVote.getVoteType()) {
                case PRE_PREPARE :
                    if (!this.localBlockTree.contains(block)) {
                        this.localBlockTree.add(block);
                    }
                    if (this.localBlockTree.getLocalBlock(block).isConnectedToGenesis) {
                        // System.out.println("block connected to genesis");
                        this.pbftPhase = PBFTPhase.PREPARING;
                        this.peerBlockchainNode.broadcastMessage(
                                new VoteMessage(
                                        new PBFTPrepareVote<>(this.peerBlockchainNode, blockVote.getBlock())
                                )
                        );
                    }
                    // else request the missing blocks? (this would happen after shard shuffle)
                    break;
                case PREPARE:
                    checkVotes(blockVote, block, prepareVotes, preparedBlocks, PBFTPhase.COMMITTING);
                    break;
                case COMMIT:
                    checkVotes(blockVote, block, commitVotes, committedBlocks, PBFTPhase.PRE_PREPARING);
                    break;
            }
        }
    }

    private void checkVotes(PBFTBlockVote<B> vote, B block, HashMap<B, HashMap<Node, Vote>> votes, HashSet<B> blocks, PBFTPhase nextStep) {
        if (!blocks.contains(block)) {
            if (!votes.containsKey(block)) { // this the first vote received for this block
                votes.put(block, new HashMap<>());
            }
            votes.get(block).put(vote.getVoter(), vote);
            if (votes.get(block).size() > (((numAllParticipants / 3) * 2) + 1)) { // if over 2 thirds voted in favour
                blocks.add(block);
                this.pbftPhase = nextStep;
                switch (nextStep) { // depending on what the next step is do different things
                    case PRE_PREPARING: // if THIS stage is commit
                        this.currentViewNumber += 1;
                        this.currentMainChainHead = block;
                        updateChain();
                        // TODO: handle the cross shard transactions in the newest block in the chain
                        handleCrossShardTransactions();
                        // System.out.println("checking if i can make a new block");
                        // get the shard that this node is in
                        int ID = this.peerBlockchainNode.nodeID;
                        if(this.peerBlockchainNode instanceof PBFTShardedNode) {
                            PBFTShardedNode pbftShardedNode = (PBFTShardedNode) this.peerBlockchainNode;
                            int shardNumber = pbftShardedNode.getShardNumber();
                            ID = ((PBFTShardedNetwork) pbftShardedNode.getNetwork()).getIndexOfNode(pbftShardedNode, shardNumber);
                            // remove the transactions from that block from the mempool
                            pbftShardedNode.removeTransactionsFromMempool((PBFTBlock) block);
                        }
                        if (ID == this.getCurrentPrimaryNumber()){ // IF IT IS THIS NODES TIME TO MAKE A BLOCK, MAKE ONE
                            // System.out.println("Node ID: " + this.peerBlockchainNode.nodeID + " making a block");
                            this.peerBlockchainNode.broadcastMessage(
                                    new VoteMessage(
                                            new PBFTPrePrepareVote<>(this.peerBlockchainNode, ((PBFTShardedNode)this.peerBlockchainNode).createBlock()
                                            )
                                    )
                            );
                        }
                        break;
                    case COMMITTING: // if THIS stage is prepare
                        this.peerBlockchainNode.broadcastMessage(
                                new VoteMessage(
                                        new PBFTCommitVote<>(this.peerBlockchainNode, block)
                                )
                        );
                        break;
                }
            }
        }
    }

    @Override
    public void newIncomingBlock(B block) {
    }

    /**
     * @param block
     * @return
     */
    @Override
    public boolean isBlockConfirmed(B block) {
        return false;
    }

    /**
     * @param block
     * @return
     */
    @Override
    public boolean isBlockValid(B block) {
        return false;
    }

    public int getCurrentViewNumber() {
        return this.currentViewNumber;
    }

    public int getCurrentPrimaryNumber() {
        return (this.currentViewNumber % this.numAllParticipants);
    }

    public int getNumAllParticipants() {
        return this.numAllParticipants;
    }

    public PBFTPhase getPbftPhase() {
        return this.pbftPhase;
    }

    @Override
    protected void updateChain() {
        this.confirmedBlocks.add(this.currentMainChainHead);
    }

    private void handleCrossShardTransactions() {
        // get the latest block in the chain as a PBFTBlock
        PBFTBlock block = (PBFTBlock) this.currentMainChainHead;
        // get the transactions from that block
        ArrayList<Recipt> transactions = block.getRecipts();
        // pass this to the handle cross shard transactions method in node
        ((PBFTShardedNode)this.peerBlockchainNode).handleCrossShardTransactions(transactions);
    }
}
