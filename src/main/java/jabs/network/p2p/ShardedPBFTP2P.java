package jabs.network.p2p;

import jabs.network.networks.Network;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.pbft.PBFTShardedNode;

public class ShardedPBFTP2P extends AbstractP2PConnections {

    @Override
    public void connectToNetwork(Network network) {
        int shardNumber = ((PBFTShardedNode)node).getShardNumber();
        // System.out.println("Shard number from node: " + shardNumber);
        connectToNetwork((PBFTShardedNetwork)network, shardNumber);
    }

    public void connectToNetwork(PBFTShardedNetwork network, int shardNumber){
        // System.out.println("Calling from p2p");
        // add all of the shard members to the p2p connections
        this.neighbors.addAll(network.getShard(shardNumber));
        node.getNodeNetworkInterface().connectNetwork(network, network.getRandom());
    }

    @Override
    public boolean requestConnection(Node node) {
        return false;
    }
}
