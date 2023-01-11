package jabs.network.node.nodes;

import java.util.ArrayList;

import jabs.consensus.algorithm.ClientLedEdgeNodeProtocol;
import jabs.consensus.algorithm.EdgeNodeProtocol;
import jabs.ledgerdata.Data;
import jabs.ledgerdata.TransactionFactory;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.message.CoordinationMessage;
import jabs.network.message.Message;
import jabs.network.message.Packet;
import jabs.network.networks.Network;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.network.p2p.ShardedClientP2P;
import jabs.simulator.Simulator;

public class ShardedClient extends Node{

    private ArrayList<EthereumTx> txs;
    private EdgeNodeProtocol protocol;

    public ShardedClient(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, new ShardedClientP2P());
        this.txs = new ArrayList<EthereumTx>();
        // this needs to be modified for allowing either client led or shard led to be used
        this.protocol = new ClientLedEdgeNodeProtocol(this, network);
        this.fillTxPool(100);
    }

    @Override
    public void processIncomingPacket(Packet packet) {
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
                this.protocol.processCoordinationMessage(tx, shard, type, (PBFTShardedNode) packet.getFrom());
            }
        }
    }

    @Override
    public void generateNewTransaction() {
        
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
            } else {
                ((PBFTShardedNetwork) this.network).clientIntraShardTransactions++;
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
        this.protocol.sendTransaction(tx);
    }
}