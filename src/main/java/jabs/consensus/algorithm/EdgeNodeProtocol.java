package jabs.consensus.algorithm;

import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.message.CoordinationMessage;
import jabs.network.node.nodes.pbft.PBFTShardedNode;

public interface EdgeNodeProtocol {
    public void processCoordinationMessage(EthereumTx tx, Integer shard, String type, PBFTShardedNode from);
    public void sendTransaction(EthereumTx tx);
}