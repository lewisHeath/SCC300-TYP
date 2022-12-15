package jabs.scenario;

import jabs.consensus.config.PBFTConsensusConfig;
import jabs.ledgerdata.pbft.PBFTPrePrepareVote;
import jabs.network.message.VoteMessage;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.ShardedClient;
import jabs.network.node.nodes.pbft.PBFTShardedNode;


import java.util.ArrayList;

public class ShardedPBFTScenario extends AbstractScenario {
    protected int nodesPerShard;
    protected int simulationStopTime;
    protected int numberOfShards;

    public ShardedPBFTScenario(String name, long seed, int numberOfShards, int nodesPerShard,
            double simulationStopTime) {
        super(name, seed);
        this.numberOfShards = numberOfShards;
        this.nodesPerShard = nodesPerShard;
        this.simulationStopTime = (int) simulationStopTime;
    }

    @Override
    public void createNetwork() {
        network = new PBFTShardedNetwork(randomnessEngine, numberOfShards, nodesPerShard);
        network.populateNetwork(simulator, new PBFTConsensusConfig());
    }

    @Override
    protected void insertInitialEvents() {
        // tell all of the clients to send the transactions to the correct shards
        ArrayList<ShardedClient> clients = ((PBFTShardedNetwork) network).getClients();
        for (ShardedClient client : clients) {
            // client.sendAllTransactions();
        }
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

    @Override
    public boolean simulationStopCondition() {
        return (simulator.getSimulationTime() > this.simulationStopTime);
    }
}
