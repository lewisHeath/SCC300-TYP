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
    protected int numberOfClients;
    protected int timeBetweenTxs;
    protected String protocol;
    protected boolean migration;
    protected boolean newAccountMigration;

    public ShardedPBFTScenario(String name, long seed, int numberOfShards, int nodesPerShard, int numberOfClients, int timeBetweenTxs,
            double simulationStopTime, String protocol, boolean migration, boolean newAccountMigration) {
        super(name, seed);
        this.numberOfShards = numberOfShards;
        this.nodesPerShard = nodesPerShard;
        this.simulationStopTime = (int) simulationStopTime;
        this.numberOfClients = numberOfClients;
        this.timeBetweenTxs = timeBetweenTxs;
        this.protocol = protocol;
        this.migration = migration;
        this.newAccountMigration = newAccountMigration;
    }

    @Override
    public void createNetwork() {
        if(this.protocol.equals("client")) {
            network = new PBFTShardedNetwork(randomnessEngine, numberOfShards, nodesPerShard, numberOfClients, timeBetweenTxs, true, migration, newAccountMigration);
        } else if(this.protocol.equals("shard")) {
            network = new PBFTShardedNetwork(randomnessEngine, numberOfShards, nodesPerShard, numberOfClients, timeBetweenTxs, false, migration, newAccountMigration);
        } else {
            System.out.println("Invalid protocol");
            System.exit(1);
        }
        network.populateNetwork(simulator, new PBFTConsensusConfig());
    }

    @Override
    protected void insertInitialEvents() {
        // tell all of the clients to send the transactions to the correct shards
        ((PBFTShardedNetwork)this.network).startClientTxGenerationProcesses();
        // get the first node in each shard and broadcast genesis block
        for (int i = 0; i < numberOfShards; i++) {
            ArrayList<PBFTShardedNode> shard = ((PBFTShardedNetwork) network).getShard(i);
            PBFTShardedNode node = shard.get(0);
            // broadcast genesis block
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
