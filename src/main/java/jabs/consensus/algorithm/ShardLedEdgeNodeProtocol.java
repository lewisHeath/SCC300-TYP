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
import jabs.simulator.event.TransactionCommittedEvent;
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
                System.out.println("Node " + this.node.getNodeID() + " received a commit message for tx " + tx + " from node " + from.getNodeID());
                // increment vote for commit
                this.txToCommits.get(tx).put(shard, this.txToCommits.get(tx).get(shard) + 1);
                // check if each shards vote is above f + 1
                if(this.txToCommits.get(tx).values().stream().allMatch(x -> x > 2 * ((PBFTShardedNetwork) this.network).getF())){
                    // the transaction is committed
                    ((PBFTShardedNetwork) this.network).committedTransactions++;
                    // remove the tx from the data structures (and maybe to a list of committed txs)
                    this.preparedTxs.remove(tx);
                    TransactionCommittedEvent txCommittedEvent = new TransactionCommittedEvent(this.node.getSimulator().getSimulationTime(), tx);
                    this.node.getSimulator().putEvent(txCommittedEvent, 0);
                }
            } else if(type.equals("aborted")) {
                // increment vote for abort
                this.txToAborts.get(tx).put(shard, this.txToAborts.get(tx).get(shard) + 1);
                // if ANY shards votes are above 2f
                if(this.txToAborts.get(tx).values().stream().anyMatch(x -> x > 2 * ((PBFTShardedNetwork) this.network).getF())){
                    // the tx is aborted, add to queue of txs to try again maybe and remove from data structure
                    this.preparedTxs.remove(tx);
                    // System.out.println("Transaction " + tx + " was aborted");
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
        ArrayList<Integer> shards = new ArrayList<Integer>();
        shards.addAll(tx.getAllInvolvedShards());
        Boolean crossShard = false;
        if(shards.size() > 1) {
            crossShard = true;
        }
        // this will be modified to support more than 2 shards
        if(crossShard) {
            // create pre-prepare message
            CoordinationMessage message = new CoordinationMessage(tx, "pre-prepare", this.node);
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
                // send to one node in each shard
                Node nodeToSendTo = ((PBFTShardedNetwork)this.network).getNodeInShard(shard, 0);
                this.node.getNodeNetworkInterface().addToUpLinkQueue(
                    new Packet(this.node, nodeToSendTo, message)
                );
                // for(Node nodeToSendTo : ((PBFTShardedNetwork)network).getAllNodesFromShard(shard)){
                //     this.node.getNodeNetworkInterface().addToUpLinkQueue(
                //     new Packet(this.node, nodeToSendTo, message)
                //     );
                // }
            }
            // System.out.println("Node " + this.node.getNodeID() + " sent a pre-prepare message for tx " + tx);
        } else {
            // send the transaction normally to the shard
            DataMessage message = new DataMessage(tx);
            // send to all nodes in senderShard
            for(Node n : ((PBFTShardedNetwork) this.network).getAllNodesFromShard(shards.get(0))) {
                node.getNodeNetworkInterface().addToUpLinkQueue(
                        new Packet(
                                node, n, message));
            }
        }
    }
    
}
