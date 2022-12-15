package jabs.network.p2p;

import jabs.network.networks.Network;
import jabs.network.node.nodes.Node;

public class ShardedClientP2P extends AbstractP2PConnections {
    @Override
    public void connectToNetwork(Network network) {
        // add all of the nodes in the network to the neighbors list
        this.neighbors.addAll(network.getAllNodes());
        node.getNodeNetworkInterface().connectNetwork(network, network.getRandom());
    }
    @Override
    public boolean requestConnection(Node node) {
        // TODO
        return false;
    }
}

