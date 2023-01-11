package jabs.consensus.algorithm;

import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.message.CoordinationMessage;
import jabs.network.message.DataMessage;
import jabs.network.message.Packet;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.ShardedClient;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.network.networks.Network;
import jabs.network.networks.sharded.PBFTShardedNetwork;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientLedEdgeNodeProtocol implements EdgeNodeProtocol {

    private ShardedClient node;
    private Network network;
    // data structures for the protocol
    private HashMap<EthereumTx, HashMap<Integer, Integer>> txToPrepareOKs;
    private HashMap<EthereumTx, HashMap<Integer, Integer>> txToCommitOKs;
    private HashMap<EthereumTx, HashMap<Integer, Integer>> txToPrepareNOTOKs;
    private ArrayList<EthereumTx> abortedTransactions;
    private ArrayList<EthereumTx> txsWithCommitMessagesSent;
    private HashMap<EthereumTx, Double> txToTime;

    public ClientLedEdgeNodeProtocol(ShardedClient node, Network network) {
        this.node = node;
        this.network = network;
        this.txToPrepareOKs = new HashMap<EthereumTx, HashMap<Integer, Integer>>();
        this.txToCommitOKs = new HashMap<EthereumTx, HashMap<Integer, Integer>>();
        this.txToPrepareNOTOKs = new HashMap<EthereumTx, HashMap<Integer, Integer>>();
        this.abortedTransactions = new ArrayList<EthereumTx>();
        this.txsWithCommitMessagesSent = new ArrayList<EthereumTx>();
        this.txToTime = new HashMap<EthereumTx, Double>();
    }

    @Override
    public void processCoordinationMessage(EthereumTx tx, Integer shard, String type, PBFTShardedNode from) {
        if (txToPrepareOKs.containsKey(tx)) {
            // if the message is a prepareOK
            if (type.equals("prepareOK")) {
                // increment vote for this tx from this shard
                txToPrepareOKs.get(tx).put(shard, txToPrepareOKs.get(tx).get(shard) + 1);
                if (txToPrepareOKs.get(tx).values().stream()
                        .allMatch(x -> x >= 2 * ((PBFTShardedNetwork) this.network).getF())
                        && !txsWithCommitMessagesSent.contains(tx)) {
                    // send a commit message to all nodes in BOTH shards
                    for (int shardToSendTo : txToPrepareOKs.get(tx).keySet()) {
                        for (Node n : ((PBFTShardedNetwork) this.network).getAllNodesFromShard(shardToSendTo)) {
                            node.getNodeNetworkInterface().addToUpLinkQueue(
                                    new Packet(
                                            node, n, new CoordinationMessage(tx, "commit")));
                        }
                    }
                    // add to a list of transactions that have had the commit message sent
                    txsWithCommitMessagesSent.add(tx);
                }
            }
            // if the message is a prepareNOTOK
            else if (type.equals("prepareNOTOK")) {
                // the rollback is only sent if 2f nodes in a shard say prepareNOTOK, to account
                // for malicious nodes
                // increment vote for this tx from this shard
                txToPrepareNOTOKs.get(tx).put(shard, txToPrepareNOTOKs.get(tx).get(shard) + 1);
                // if any of the shards votes are above 2f
                if (txToPrepareNOTOKs.get(tx).values().stream()
                        .anyMatch(x -> x >= 2 * ((PBFTShardedNetwork) this.network).getF())) {
                    // send a rollback message to all nodes in all concerned shards in the
                    // transaction
                    for (int s : txToPrepareOKs.get(tx).keySet()) {
                        for (Node n : ((PBFTShardedNetwork) this.network).getAllNodesFromShard(s)) {
                            node.getNodeNetworkInterface().addToUpLinkQueue(
                                    new Packet(
                                            node, n, new CoordinationMessage(tx, "rollback")));
                        }
                    }
                    // remove the transaction from the map
                    txToPrepareOKs.remove(tx);
                    // add the transaction to the aborted transactions list
                    abortedTransactions.add(tx);
                    // add the tx to a map of transactions to be sent, with the time being 1 minute
                    // from now
                    if (txToTime.get(tx) == null) {
                        // System.out.println("Transaction is aborted, adding to queue to be sent
                        // again");
                        txToTime.put(tx, node.getSimulator().getSimulationTime() + 60);
                    }
                }
            }
            // if the message is a committed
            else if (type.equals("committed")) {
                txToCommitOKs.get(tx).put(shard, txToCommitOKs.get(tx).get(shard) + 1);
                // if the number of commitOKs is greater than 2f for all of the shards
                if (txToCommitOKs.get(tx).values().stream()
                        .allMatch(x -> x >= 2 * ((PBFTShardedNetwork) this.network).getF())) {
                    // the tx is now committed
                    // System.out.println("Transaction is committed!");
                    ((PBFTShardedNetwork) this.network).committedTransactions++;
                    // remove the transaction from the map
                    txToCommitOKs.remove(tx);
                    txToPrepareOKs.remove(tx);
                }
            }
        }
    }

    @Override
    public void sendTransaction(EthereumTx tx) {
        int senderShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getSender());
        int receiverShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getReceiver());
        // create a pre-prepare message here and send it to all of the concerned shards
        // in the transaction
        CoordinationMessage prePrepare = new CoordinationMessage(tx, "pre-prepare");
        // if the sender and receiver are in different shards
        if (senderShard != receiverShard) {
            // init the prepareOKs map
            HashMap<Integer, Integer> shardToPrepareOKs = new HashMap<Integer, Integer>();
            txToPrepareOKs.put(tx, shardToPrepareOKs);
            // init the commit OKs map
            HashMap<Integer, Integer> shardToCommitOKs = new HashMap<Integer, Integer>();
            txToCommitOKs.put(tx, shardToCommitOKs);
            // init the prepareNOTOKs map
            HashMap<Integer, Integer> shardToPrepareNOTOKs = new HashMap<Integer, Integer>();
            txToPrepareNOTOKs.put(tx, shardToPrepareNOTOKs);
            // get a list of accounts involved in the transaction and get the shards they
            // are in
            // this will be modified for smart contract transactions
            ArrayList<Integer> shards = new ArrayList<Integer>();
            shards.add(senderShard);
            shards.add(receiverShard);
            txToPrepareOKs.get(tx).put(senderShard, 0);
            txToPrepareOKs.get(tx).put(receiverShard, 0);
            txToCommitOKs.get(tx).put(senderShard, 0);
            txToCommitOKs.get(tx).put(receiverShard, 0);
            txToPrepareNOTOKs.get(tx).put(senderShard, 0);
            txToPrepareNOTOKs.get(tx).put(receiverShard, 0);
            this.txToTime.remove(tx);
            // System.out.println("Client sending cross-shard transaction");
            // ((PBFTShardedNetwork) this.network).clientCrossShardTransactions++;
            // send the message to all of the nodes in the sender shard
            for (Node n : ((PBFTShardedNetwork) this.network).getAllNodesFromShard(senderShard)) {
                node.getNodeNetworkInterface().addToUpLinkQueue(
                        new Packet(
                                node, n, prePrepare));
            }
            // send the message to all of the nodes in the receiver shard
            for (Node n : ((PBFTShardedNetwork) this.network).getAllNodesFromShard(receiverShard)) {
                node.getNodeNetworkInterface().addToUpLinkQueue(
                        new Packet(
                                node, n, prePrepare));
            }
        } else {
            // just simply send the transaction to all or one of the nodes in the shard the
            // transaction is in
            DataMessage newTx = new DataMessage(tx);
            // send a data message with the tx in to all the nodes in the shard
            // for (Node n : ((PBFTShardedNetwork)this.network).getAllNodesFromShard(senderShard)) {
            //     node.getNodeNetworkInterface().addToUpLinkQueue(
            //         new Packet(
            //             node, n, newTx
            //         )
            //     );
            // }
        }
    }

    private void reSendTxs() {
        // create a local copy of the txToTime map
        HashMap<EthereumTx, Double> txsToBeSent = new HashMap<EthereumTx, Double>(txToTime);
        // for each tx in the txToTime map
        for (EthereumTx tx : txsToBeSent.keySet()) {
            // if the current simulation time is greater than the time from the map
            // System.out.println("Current time: " + this.simulator.getSimulationTime() + "
            // txToTime: " + txToTime.get(tx));
            if (node.getSimulator().getSimulationTime() > txToTime.get(tx)) {
                // create a new version of the transaction
                EthereumTx newTx = new EthereumTx(tx.getSize(), tx.getGas());
                newTx.setSender(tx.getSender());
                newTx.setReceiver(tx.getReceiver());
                // send the transaction again
                this.sendTransaction(newTx);
                // remove the transaction from the map
                txToTime.remove(tx);
                // System.out.println("Resending transaction");
            }
        }
    }
}
