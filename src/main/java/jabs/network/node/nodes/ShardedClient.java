package jabs.network.node.nodes;

import java.util.ArrayList;
import java.util.HashMap;

import jabs.consensus.algorithm.ClientLedEdgeNodeProtocol;
import jabs.consensus.algorithm.EdgeNodeProtocol;
import jabs.consensus.algorithm.ShardLedEdgeNodeProtocol;
import jabs.ledgerdata.Data;
import jabs.ledgerdata.TransactionFactory;
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
import jabs.simulator.event.TransactionCommittedEvent;
import jabs.simulator.event.TxGenerationProcessSingleNode;

public class ShardedClient extends Node{

    private ArrayList<EthereumTx> txs;
    private EdgeNodeProtocol protocol;
    protected Simulator.ScheduledEvent txGenerationProcess;
    private int timeBetweenTxs;
    private HashMap<EthereumTx, Integer> intraShardTxCommitCount;

    public ShardedClient(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth, int timeBetweenTxs) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, new ShardedClientP2P());
        this.txs = new ArrayList<EthereumTx>();
        // this needs to be modified for allowing either client led or shard led to be used
        this.protocol = new ClientLedEdgeNodeProtocol(this, network);
        this.timeBetweenTxs = timeBetweenTxs;
        this.intraShardTxCommitCount = new HashMap<EthereumTx, Integer>();
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
            if(type.equals("intra-shard-committed")){
                // process intra shard committed tx
                this.processIntraShardCommittedTx((EthereumTx) data, shard);
            }
            // System.out.println("Client recieved " + type + " from node: " + packet.getFrom().getNodeID());
            if (data instanceof EthereumTx) {
                EthereumTx tx = (EthereumTx) data;
                this.protocol.processCoordinationMessage(tx, shard, type, (PBFTShardedNode) packet.getFrom());
            }
        }
    }

    private void processIntraShardCommittedTx(EthereumTx data, int shard) {
        // check if the tx is in the list of txs
        if(this.txs.contains(data)){
            // check if the tx is in the list of intra shard txs
            if(this.intraShardTxCommitCount.containsKey(data)){
                // increment the commit count
                int commitCount = this.intraShardTxCommitCount.get(data);
                commitCount++;
                this.intraShardTxCommitCount.put(data, commitCount);
                // check if the commit count is equal to the number of shards
                if(commitCount >= ((PBFTShardedNetwork) this.network).getF() + 1){
                    // remove the tx from the list of txs
                    this.txs.remove(data);
                    // remove the tx from the list of intra shard txs
                    this.intraShardTxCommitCount.remove(data);
                    // increment the number of committed txs
                    ((PBFTShardedNetwork) this.network).committedTransactions++;
                    // generate transaction committed event
                    TransactionCommittedEvent txCommittedEvent = new TransactionCommittedEvent(this.simulator.getSimulationTime(), data);
                    this.simulator.putEvent(txCommittedEvent, 0);
                    // System.out.println("Committed");
                }
            }
        }
    }

    @Override
    public void generateNewTransaction() {

        // generate transaction creation event TODO


        EthereumTx tx = TransactionFactory.sampleEthereumTransaction(network.getRandom());
        tx.setCreationTime(this.simulator.getSimulationTime());
        // somehow decide how many shards/accounts should be involved in the transaction TODO
        // for now, i will randomly select between 2 and 5 accounts
        int numAccounts = network.getRandom().nextInt(3) + 2;


        ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
        for (int i = 0; i < numAccounts; i++) {
            accounts.add(((PBFTShardedNetwork) network).getRandomAccount());
        }
        // set sender and receiver(s)
        tx.setSender(accounts.get(0));
        accounts.remove(0);
        tx.setReceiver(accounts.get(0));
        tx.setReceivers(accounts);

        txs.add(tx);
        int senderShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getSender());
        // check if the sender shard is not the same as ANY of the receiver shards
        boolean crossShard = false;
        for (EthereumAccount account : tx.getReceivers()) {
            int receiverShard = ((PBFTShardedNetwork) this.network).getAccountShard(account);
            if (senderShard != receiverShard) {
                crossShard = true;
                break;
            }
        }

        if (crossShard) {
            ((PBFTShardedNetwork) this.network).clientCrossShardTransactions++;
            tx.setCrossShard(true);
            this.sendCrossShardTransaction(tx);
        } else {
            ((PBFTShardedNetwork) this.network).clientIntraShardTransactions++;
            this.intraShardTxCommitCount.put(tx, 0);
            tx.setCrossShard(false);
            this.sendTransaction(tx, senderShard);
        }
    }

    public void startTxGenerationProcess() {
        TxGenerationProcessSingleNode txGenerationProcess = new TxGenerationProcessSingleNode(this.simulator, this.network.getRandom(), this, timeBetweenTxs);
        this.txGenerationProcess = this.simulator.putEvent(txGenerationProcess, txGenerationProcess.timeToNextGeneration());
    }

    public void stopTxGenerationProcess() {
        // this.simulator.removeEvent(this.txGenerationProcess);
    }

    private void sendTransaction(EthereumTx tx, int shard) {
        // send to at least f + 1 nodes in the shard
        // int f = ((PBFTShardedNetwork) this.network).getF();
        tx.setCreationTime(this.simulator.getSimulationTime());

        ArrayList<PBFTShardedNode> nodes = ((PBFTShardedNetwork) this.network).getAllNodesFromShard(shard);
        for(Node node : nodes){
            this.networkInterface.addToUpLinkQueue(
                new Packet(this, node, 
                new DataMessage(tx))
            );
        }
    }

    private void sendCrossShardTransaction(EthereumTx tx) {
        this.protocol.sendTransaction(tx);
    }
}