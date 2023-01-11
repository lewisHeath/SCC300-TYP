package jabs.consensus.algorithm;

import java.util.ArrayList;
import java.util.HashMap;

import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.message.CoordinationMessage;
import jabs.network.message.DataMessage;
import jabs.network.message.Packet;
import jabs.network.networks.Network;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.ShardedClient;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.network.networks.sharded.PBFTShardedNetwork;

public class ShardLedEdgeNodeProtocol implements EdgeNodeProtocol {

    private ShardedClient node;
    private Network network;
    // data structures for the protocol
    private HashMap<EthereumTx, HashMap<Integer, Integer>> txToCommits;
    private HashMap<EthereumTx, HashMap<Integer, Integer>> txToAborts;
    private ArrayList<EthereumTx> preparedTxs;

    public ShardLedEdgeNodeProtocol(ShardedClient node, Network network) {
        this.node = node;
        this.network = network;
        this.txToCommits = new HashMap<EthereumTx, HashMap<Integer, Integer>>();
        this.txToAborts = new HashMap<EthereumTx, HashMap<Integer, Integer>>();
        this.preparedTxs = new ArrayList<EthereumTx>();
    }

    @Override
    public void processCoordinationMessage(EthereumTx tx, Integer shard, String type, PBFTShardedNode from) {
        /*
         * 1. this is either going to be aborted or committed
         * 2. if it is committed, add to the data structure and check if it is above 2f for all shards
         * 3. if it is aborted, add to the data structure and check if it is above 2f for all shards
         *  3.1. add to list of txs to be retried later, maybe
         */

        // check if the transaction is in the prepared list
        if(this.preparedTxs.contains(tx)){
            if(type.equals("committed")){
                // increment vote for commit
                this.txToCommits.get(tx).put(shard, this.txToCommits.get(tx).get(shard) + 1);
                // check if each shards vote is above 2f 
                if(this.txToCommits.get(tx).values().stream().allMatch(x -> x >= 2 * ((PBFTShardedNetwork) this.network).getF())){
                    // the transaction is committed
                    ((PBFTShardedNetwork) this.network).committedTransactions++;
                    // remove the tx from the data structures (and maybe to a list of committed txs)
                    this.preparedTxs.remove(tx);
                }
            } else if(type.equals("abort")) {
                // increment vote for abort
                this.txToAborts.get(tx).put(shard, this.txToAborts.get(tx).get(shard) + 1);
                // if ANY shards votes are above 2f
                if(this.txToAborts.get(tx).values().stream().anyMatch(x -> x >= 2 * ((PBFTShardedNetwork) this.network).getF())){
                    // the tx is aborted, add to queue of txs to try again maybe and remove from data structure
                    this.preparedTxs.remove(tx);
                }
            }
        }
    }

    @Override
    public void sendTransaction(EthereumTx tx) {
        /*
         * 1. get the shards the transaction is going to
         * 2. initialise the data structures here for that tx
         * 3. send the transaction in a pre-prepare message to the shards
         */
        
         this.preparedTxs.add(tx);
        // get the shards from the transaction
        int senderShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getSender());
        int receiverShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getReceiver());
        ArrayList<Integer> shards = new ArrayList<Integer>();
        // this will be modified to support more than 2 shards
        shards.add(senderShard);
        shards.add(receiverShard);
        if(senderShard != receiverShard) {
            // create pre-prepare message
            CoordinationMessage message = new CoordinationMessage(tx, "pre-prepare");
            // initialise the data structures
            this.txToCommits.put(tx, new HashMap<Integer, Integer>());
            this.txToAborts.put(tx, new HashMap<Integer, Integer>());
            // add the shards to the data structures
            for(Integer shard : shards) {
                this.txToCommits.get(tx).put(shard, 0);
                this.txToAborts.get(tx).put(shard, 0);
            }
            // send the message to the shards
            for(Integer shard : shards) {
                for (Node n : ((PBFTShardedNetwork) this.network).getAllNodesFromShard(shard)) {
                    node.getNodeNetworkInterface().addToUpLinkQueue(
                            new Packet(
                                    node, n, message));
                }
            }
        } else {
            // send the transaction normally to the shard
            DataMessage message = new DataMessage(tx);
            // send to all nodes in senderShard
            // for(Node n : ((PBFTShardedNetwork) this.network).getAllNodesFromShard(senderShard)) {
            //     node.getNodeNetworkInterface().addToUpLinkQueue(
            //             new Packet(
            //                     node, n, message));
            // }
        }
    }
    
}
