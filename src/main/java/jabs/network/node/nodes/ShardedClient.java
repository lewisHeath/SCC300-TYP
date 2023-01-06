package jabs.network.node.nodes;

import java.util.ArrayList;
import java.util.HashMap;

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

    public ShardedClient(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, new ShardedClientP2P());
        this.txs = new ArrayList<EthereumTx>();
        this.txToShards = new HashMap<EthereumTx, HashMap<Integer, Integer>>();
        this.fillTxPool(500);
    }

    @Override
    public void processIncomingPacket(Packet packet) {
        // TODO 
        // this is where the recipt from the debting of the sender account will arrive
        // send this recipt to the receivers shard
        Message message = packet.getMessage();
        if (message instanceof DataMessage) {
            Data data = ((DataMessage) message).getData();
            // if the data is an instance of a recipt
            if (data instanceof Recipt) {
                System.out.println("Client recieved recipt from node: " + packet.getFrom().getNodeID());
                Recipt recipt = (Recipt) data;
                // send the recipt to the shard which has the receiver account
                int shard = ((PBFTShardedNetwork)this.network).getAccountShard(recipt.getTx().getReceiver());
                // get a random node in that shard to send to (TODO: should this be all nodes?)
                Node node = ((PBFTShardedNetwork)this.network).getRandomNodeInShard(shard);
                // EthereumTx tx = recipt.getTx();
                // EthereumTx newTx = new EthereumTx(tx.getSize(), tx.getGas());
                // newTx.setReceiver(tx.getReceiver());
                // newTx.setSender(null);

                // broadcast recipt to all nodes in shard
                for (Node n : ((PBFTShardedNetwork)this.network).getAllNodesFromShard(shard)) {
                    this.networkInterface.addToUpLinkQueue(
                        new Packet(
                            this, n, new DataMessage(recipt)
                        )
                    );
                }
            }
        }
        else if (message instanceof CoordinationMessage) {
            // this is the prepareOK, prepareNOTOK and the committed message
            // the data will be the transaction
            Data data = ((CoordinationMessage) message).getData();
            String type = ((CoordinationMessage) message).getType();
            if (data instanceof EthereumTx) {
                EthereumTx tx = (EthereumTx) data;
                // if the transaction is in the txToShards map
                if (txToShards.containsKey(tx)) {
                    // get the shard the message came from
                    int shard = ((PBFTShardedNetwork)this.network).getAccountShard(tx.getSender());
                    // if the message is a prepareOK
                    if (type.equals("prepareOK")) {
                        // increment the number of prepareOKs for that shard
                        txToShards.get(tx).put(shard, txToShards.get(tx).get(shard) + 1);
                        // if the number of prepareOKs is greater than 2f for all of the shards
                        if (txToShards.get(tx).values().stream().allMatch(x -> x > 2 * ((PBFTShardedNetwork)this.network).getF())) {
                            // send a commit message to all nodes in the shard
                            for (Node n : ((PBFTShardedNetwork)this.network).getAllNodesFromShard(shard)) {
                                this.networkInterface.addToUpLinkQueue(
                                    new Packet(
                                        this, n, new CoordinationMessage(tx, "commit")
                                    )
                                );
                            }
                        }
                    }
                    // if the message is a prepareNOTOK
                    else if (type.equals("prepareNOTOK")) {
                        // remove the transaction from the map
                        txToShards.remove(tx);
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
                    }
                    // if the message is a committed
                    else if (type.equals("committed")) {
                        // remove the transaction from the map
                        txToShards.remove(tx);
                        System.out.println("Client recieved committed message from node: " + packet.getFrom().getNodeID());
                    }
                }
                // if the transaction is not in the map
                else {
                    // if the message is a prepareOK
                    if (type.equals("prepareOK")) {
                        // get the shard the message came from
                        int shard = ((PBFTShardedNetwork)this.network).getAccountShard(tx.getSender());
                        // add the transaction to the map with the shard and 1 prepareOK
                        HashMap<Integer, Integer> shardToPrepareOKs = new HashMap<Integer, Integer>();
                        shardToPrepareOKs.put(shard, 1);
                        txToShards.put(tx, shardToPrepareOKs);
                    }
                }
            }
        }
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
        }
        // System.out.println("Mempool size: " + this.mempool.size());
    }

    public void sendAllTransactions() {
        for (EthereumTx tx : txs) {
            // if it is cross shard
            if (((PBFTShardedNetwork)this.network).getAccountShard(tx.getSender()) != ((PBFTShardedNetwork)this.network).getAccountShard(tx.getReceiver())) {
                // increase the counter in the network
                ((PBFTShardedNetwork)this.network).clientCrossShardTransactions++;
            } else {
                // increase the counter in the network
                ((PBFTShardedNetwork)this.network).clientIntraShardTransactions++;
            }
            // create a pre-prepare message here and send it to all of the concerned shards in the transaction
            // get the shards the transaction is concerned with
            int senderShard = ((PBFTShardedNetwork)this.network).getAccountShard(tx.getSender());
            int receiverShard = ((PBFTShardedNetwork)this.network).getAccountShard(tx.getReceiver());
            // create a pre-prepare message
            CoordinationMessage prePrepare = new CoordinationMessage(tx, "pre-prepare");
            // if the sender and receiver are in different shards
            if (senderShard != receiverShard) {
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
                // TODO
            }
        }
        this.txs.clear();
    }
    
}
