package jabs.network.networks.sharded;

import java.util.ArrayList;

import jabs.consensus.config.ConsensusAlgorithmConfig;
import jabs.network.networks.Network;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.network.stats.lan.LAN100MNetworkStats;
import jabs.network.stats.lan.SingleNodeType;
import jabs.simulator.Simulator;
import jabs.simulator.randengine.RandomnessEngine;

public class PBFTShardedNetwork extends Network<PBFTShardedNode, SingleNodeType> {

    private int numberOfShards;
    private int nodesPerShard;
    private final ArrayList<ArrayList<PBFTShardedNode>> shards = new ArrayList<ArrayList<PBFTShardedNode>>();

    public PBFTShardedNetwork(RandomnessEngine randomnessEngine, int numberOfShards, int nodesPerShard) {
        super(randomnessEngine, new LAN100MNetworkStats(randomnessEngine));
        this.numberOfShards = numberOfShards;
        this.nodesPerShard = nodesPerShard;
    }

    public PBFTShardedNode createNewPBFTShardedNode(Simulator simulator, int nodeID, int numNodesInShard, int shardNumber) {
        return new PBFTShardedNode(simulator, this, nodeID,
                this.sampleDownloadBandwidth(SingleNodeType.LAN_NODE),
                this.sampleUploadBandwidth(SingleNodeType.LAN_NODE),
                numNodesInShard, shardNumber);
    }

    @Override
    public void populateNetwork(Simulator simulator, int numNodes, ConsensusAlgorithmConfig consensusAlgorithmConfig) {
        populateNetwork(simulator, consensusAlgorithmConfig);
    }

    @Override
    public void populateNetwork(Simulator simulator, ConsensusAlgorithmConfig pbfConsensusAlgorithmConfig) {
        // add the nodes to each shard
        for (int i = 0; i < numberOfShards; i++){
            // initialise shard
            shards.add(i, new ArrayList<PBFTShardedNode>());
            // add j nodes to shard i
            for (int j = nodesPerShard * i; j < nodesPerShard * (i + 1); j++){
                // add the node to the network
                this.addNode(createNewPBFTShardedNode(simulator, j, nodesPerShard, i));
                // adding that node to the shard
                shards.get(i).add((PBFTShardedNode)this.getNode(j));
            }
        }
        // connect each node to its sharded p2p connections
        for (PBFTShardedNode node : this.getAllNodes()) {
            node.getP2pConnections().connectToNetwork(this);
        }
        // so now each node is in this network but its neighbors are their shard
        // System.out.println("number of shards: " + shards.size());
        // System.out.println("number of nodes in shards: " + shards.get(0).size());
    }

    /**
     * @param node A PBFT node to add to the network
     */
    @Override
    public void addNode(PBFTShardedNode node) {
        this.addNode(node, SingleNodeType.LAN_NODE);
    }

    public ArrayList<PBFTShardedNode> getShard(int shardNumber){
        // System.out.println("size of shard(" + shardNumber + ") in getShard() method: " + shards.get(shardNumber).size());
        return this.shards.get(shardNumber);
    }
}
