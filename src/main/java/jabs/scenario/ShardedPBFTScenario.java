package jabs.scenario;

import jabs.consensus.config.PBFTConsensusConfig;
import jabs.ledgerdata.BlockFactory;
import jabs.ledgerdata.pbft.PBFTPrePrepareVote;
import jabs.network.message.VoteMessage;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.pbft.PBFTShardedNode;

import static jabs.network.node.nodes.pbft.PBFTShardedNode.PBFT_GENESIS_BLOCK;

import java.util.ArrayList;

public class ShardedPBFTScenario extends PBFTLANScenario {
    protected int numberOfShards;

    public ShardedPBFTScenario(String name, long seed, int numberOfShards, int nodesPerShard,
            double simulationStopTime) {
        super(name, seed, nodesPerShard, simulationStopTime);
        this.numberOfShards = numberOfShards;
    }

    @Override
    public void createNetwork() {
        network = new PBFTShardedNetwork(randomnessEngine, numberOfShards, numNodes);
        network.populateNetwork(simulator, new PBFTConsensusConfig());
    }

    @Override
    protected void insertInitialEvents() {
        // get the first node in each shard and broadcast genesis block
        for (int i = 0; i < numberOfShards; i++) {
            // PBFTShardedNode node = ((PBFTShardedNetwork) network).getShard(i).get(0);
            ArrayList<PBFTShardedNode> shard = ((PBFTShardedNetwork) network).getShard(i);
            // System.out.println("size of shard in scenario: " + shard.size());
            PBFTShardedNode node = shard.get(0);
            // broadcast genesis block
            // System.out.println("broadcasting genesis block from node ID: " + node.nodeID);
            node.broadcastMessage(
                    new VoteMessage(
                            new PBFTPrePrepareVote<>(node, node.createBlock()
                                )
                        )
                    );
        }
    }
}
