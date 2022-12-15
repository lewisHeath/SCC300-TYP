package jabs.network.node.nodes;

import java.util.ArrayList;

import jabs.ledgerdata.Data;
import jabs.ledgerdata.TransactionFactory;
import jabs.ledgerdata.Sharding.Recipt;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
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

    public ShardedClient(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, new ShardedClientP2P());
        this.txs = new ArrayList<EthereumTx>();
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
            // send it to the shard the account is in
            int shard = ((PBFTShardedNetwork)this.network).getAccountShard(tx.getSender());
            // get a random node in that shard to send to (TODO: should this be all nodes?)
            PBFTShardedNode node = ((PBFTShardedNetwork)this.network).getRandomNodeInShard(shard);
            // send the transaction to the node
            System.out.println("Sending tx from client to shard " + shard);
            System.out.println("sender shard: " + ((PBFTShardedNetwork)this.network).getAccountShard(tx.getSender()));
            // if it is cross shard
            if (((PBFTShardedNetwork)this.network).getAccountShard(tx.getSender()) != ((PBFTShardedNetwork)this.network).getAccountShard(tx.getReceiver())) {
                // increase the counter in the network
                ((PBFTShardedNetwork)this.network).clientCrossShardTransactions++;
            } else {
                // increase the counter in the network
                ((PBFTShardedNetwork)this.network).clientIntraShardTransactions++;
            }
            System.out.println("Node shard: " + node.getShardNumber() + " Sender shard: " + ((PBFTShardedNetwork)this.network).getAccountShard(tx.getSender()));
            this.networkInterface.addToUpLinkQueue(
                new Packet(
                    this, node, new DataMessage(tx)
                )
            );
        }
        this.txs.clear();
    }
    
}
