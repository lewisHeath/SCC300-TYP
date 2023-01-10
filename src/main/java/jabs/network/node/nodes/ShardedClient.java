package jabs.network.node.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import jabs.ledgerdata.Data;
import jabs.ledgerdata.TransactionFactory;
import jabs.ledgerdata.Sharding.Recipt;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.message.CoordinationMessage;
import jabs.network.message.DataMessage;
import jabs.network.message.Message;
import jabs.network.message.Packet;
import jabs.network.networks.Network;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.network.p2p.ShardedClientP2P;
import jabs.simulator.Simulator;

public class ShardedClient extends Node{

    private ArrayList<EthereumTx> txs;
    // transaction -> <shard, no of prepareOKs>
    private HashMap<EthereumTx, HashMap<Integer, Integer>> txToShards;
    private HashMap<EthereumTx, HashMap<Integer, Integer>> txToCommitOKs;
    private ArrayList<EthereumTx> abortedTransactions;
    private ArrayList<EthereumTx> txsWithCommitMessagesSent;
    private HashMap<EthereumTx, Double> txToTime;

    public ShardedClient(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, new ShardedClientP2P());
        this.txs = new ArrayList<EthereumTx>();
        this.txToShards = new HashMap<EthereumTx, HashMap<Integer, Integer>>();
        this.txToCommitOKs = new HashMap<EthereumTx, HashMap<Integer, Integer>>();
        this.abortedTransactions = new ArrayList<EthereumTx>();
        this.txsWithCommitMessagesSent = new ArrayList<EthereumTx>();
        this.txToTime = new HashMap<EthereumTx, Double>();
        this.fillTxPool(100);
    }

    @Override
    public void processIncomingPacket(Packet packet) {
        this.reSendTxs();
        Message message = packet.getMessage();
        if (message instanceof CoordinationMessage) {
            // this is the prepareOK, prepareNOTOK and the committed message
            // the data will be the transaction
            Data data = ((CoordinationMessage) message).getData();
            String type = ((CoordinationMessage) message).getType();
            // get the shard the message came from
            int shard = ((PBFTShardedNode) packet.getFrom()).getShardNumber();
            // System.out.println("Client recieved " + type + " from node: " + packet.getFrom().getNodeID());
            if (data instanceof EthereumTx) {
                EthereumTx tx = (EthereumTx) data;
                // if the transaction is in the txToShards map
                if (txToShards.containsKey(tx)) {
                    // if the message is a prepareOK
                    if (type.equals("prepareOK")) {
                        // increment vote for this tx from this shard
                        txToShards.get(tx).put(shard, txToShards.get(tx).get(shard) + 1);
                        if (txToShards.get(tx).values().stream().allMatch(x -> x >= 2 * ((PBFTShardedNetwork)this.network).getF()) && !txsWithCommitMessagesSent.contains(tx)) {
                            // send a commit message to all nodes in BOTH shards
                            for (int shardToSendTo : txToShards.get(tx).keySet()) {
                                for (Node n : ((PBFTShardedNetwork)this.network).getAllNodesFromShard(shardToSendTo)) {
                                    this.networkInterface.addToUpLinkQueue(
                                        new Packet(
                                            this, n, new CoordinationMessage(tx, "commit")
                                        )
                                    );
                                }
                            }
                            // add to a list of transactions that have had the commit message sent
                            txsWithCommitMessagesSent.add(tx);
                        }
                    }
                    // if the message is a prepareNOTOK
                    else if (type.equals("prepareNOTOK")) {
                        // send a rollback message to all nodes in all concerned shards in the transaction
                        for (int s : txToShards.get(tx).keySet()) {
                            for (Node n : ((PBFTShardedNetwork)this.network).getAllNodesFromShard(s)) {
                                this.networkInterface.addToUpLinkQueue(
                                    new Packet(
                                        this, n, new CoordinationMessage(tx, "rollback")
                                    )
                                );
                            }
                        }
                        // remove the transaction from the map
                        txToShards.remove(tx);
                        // add the transaction to the aborted transactions list
                        abortedTransactions.add(tx);
                        // add the tx to a map of transactions to be sent, with the time being 1 minute from now
                        if(txToTime.get(tx) == null){
                            // System.out.println("Transaction is aborted, adding to queue to be sent again");
                            txToTime.put(tx, this.simulator.getSimulationTime() + 10);
                        }
                    }
                    // if the message is a committed
                    else if (type.equals("committed")) {
                        txToCommitOKs.get(tx).put(shard, txToCommitOKs.get(tx).get(shard) + 1);
                        // if the number of commitOKs is greater than 2f for all of the shards
                        if (txToCommitOKs.get(tx).values().stream().allMatch(x -> x >= 2 * ((PBFTShardedNetwork)this.network).getF())) {
                            // the tx is now committed
                            // System.out.println("Transaction is committed!");
                            ((PBFTShardedNetwork)this.network).committedTransactions++;
                            // remove the transaction from the map
                            txToCommitOKs.remove(tx);
                            txToShards.remove(tx);
                            // remove the transaction from the txs list
                            this.txs.remove(tx);
                        }
                    }
                }
            }
        }
        // this.reSendTxs();
    }

    @Override
    public void generateNewTransaction() {
        // TODO
        // get the next transaction from the transaction pool
        EthereumTx tx = this.txs.get(0);
        // send it to the shard the account is in
        int shard = ((PBFTShardedNetwork)this.network).getAccountShard(tx.getSender());
        // get a random node in that shard to send to (TODO: should this be all nodes?)
        Node node = ((PBFTShardedNetwork)this.network).getRandomNodeInShard(shard);
        // send the transaction to the node
        this.networkInterface.addToUpLinkQueue(
            new Packet(
                this, node, new DataMessage(tx)
            )
        );
        // remove the transaction from the pool
        this.txs.remove(0);
    }

    public void addTx(EthereumTx tx) {
        txs.add(tx);
    }

    protected void fillTxPool(int numTxs) {
        for (int i = 0; i < numTxs; i++) {
            EthereumTx tx = TransactionFactory.sampleEthereumTransaction(network.getRandom());
            // get 2 random accounts from the network
            EthereumAccount sender = ((PBFTShardedNetwork) network).getRandomAccount();
            EthereumAccount receiver = ((PBFTShardedNetwork) network).getRandomAccount();
            tx.setSender(sender);
            tx.setReceiver(receiver);
            txs.add(tx);
            int senderShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getSender());
            int receiverShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getReceiver());
            if(senderShard != receiverShard) {
                ((PBFTShardedNetwork) this.network).clientCrossShardTransactions++;
            }
        }
        // System.out.println("Mempool size: " + this.txs.size());
    }

    public void sendAllTransactions() {
        for (EthereumTx tx : txs) {
            this.sendTransaction(tx);
        }
        this.txs.clear();
    }

    private void sendTransaction(EthereumTx tx) {
        int senderShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getSender());
        int receiverShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getReceiver());
        // create a pre-prepare message here and send it to all of the concerned shards in the transaction
        CoordinationMessage prePrepare = new CoordinationMessage(tx, "pre-prepare");
        // if the sender and receiver are in different shards
        if (senderShard != receiverShard) {
            // init the prepareOKs map
            HashMap<Integer, Integer> shardToPrepareOKs = new HashMap<Integer, Integer>();
            txToShards.put(tx, shardToPrepareOKs);
            // init the commit OKs map
            HashMap<Integer, Integer> shardToCommitOKs = new HashMap<Integer, Integer>();
            txToCommitOKs.put(tx, shardToCommitOKs);
            // get a list of accounts involved in the transaction and get the shards they
            // are in
            // this will be modified for smart contract transactions
            ArrayList<Integer> shards = new ArrayList<Integer>();
            shards.add(senderShard);
            shards.add(receiverShard);
            txToShards.get(tx).put(senderShard, 0);
            txToShards.get(tx).put(receiverShard, 0);
            txToCommitOKs.get(tx).put(senderShard, 0);
            txToCommitOKs.get(tx).put(receiverShard, 0);
            this.txToTime.remove(tx);
            // System.out.println("Client sending cross-shard transaction");
            // ((PBFTShardedNetwork) this.network).clientCrossShardTransactions++;
            // send the message to all of the nodes in the sender shard
            for (Node n : ((PBFTShardedNetwork)this.network).getAllNodesFromShard(senderShard)) {
                this.networkInterface.addToUpLinkQueue(
                    new Packet(
                        this, n, prePrepare
                    )
                );
            }
            // send the message to all of the nodes in the receiver shard
            for (Node n : ((PBFTShardedNetwork)this.network).getAllNodesFromShard(receiverShard)) {
                this.networkInterface.addToUpLinkQueue(
                    new Packet(
                        this, n, prePrepare
                    )
                );
            }
        } else {
            // just simply send the transaction to all or one of the nodes in the shard the transaction is in
            ((PBFTShardedNetwork) this.network).clientIntraShardTransactions++;
            DataMessage newTx = new DataMessage(tx);
            //send a data message with the tx in to all the nodes in the shard
            // for (Node n : ((PBFTShardedNetwork)this.network).getAllNodesFromShard(senderShard)) {
            //     this.networkInterface.addToUpLinkQueue(
            //         new Packet(
            //             this, n, newTx
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
            // System.out.println("Current time: " + this.simulator.getSimulationTime() + " txToTime: " + txToTime.get(tx));
            if (this.simulator.getSimulationTime() > txToTime.get(tx)) {
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