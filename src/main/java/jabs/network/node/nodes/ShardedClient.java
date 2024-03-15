package jabs.network.node.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import jabs.consensus.algorithm.ClientLedCrossShardConsensus;
import jabs.consensus.algorithm.ClientLedEdgeNodeProtocol;
import jabs.consensus.algorithm.EdgeNodeProtocol;
import jabs.consensus.algorithm.MigrationOfExistingAccounts;
import jabs.consensus.algorithm.MigrationPolicy;
import jabs.consensus.algorithm.PBFT;
import jabs.consensus.algorithm.ShardLedEdgeNodeProtocol;
import jabs.consensus.algorithm.ThresholdMigrationPolicy;
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
import jabs.network.networks.sharded.ShardLoadTracker;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.network.p2p.ShardedClientP2P;
import jabs.simulator.Simulator;
import jabs.simulator.event.MigrationEvent;
import jabs.simulator.event.ShardloadEvent;
import jabs.simulator.event.TransactionCommittedEvent;
import jabs.simulator.event.TransactionCreationEvent;
import jabs.simulator.event.TxGenerationProcessSingleNode;

public class ShardedClient extends Node{
    private ArrayList<EthereumTx> txs;
    private EdgeNodeProtocol protocol;
    protected Simulator.ScheduledEvent txGenerationProcess;
    private int timeBetweenTxs;
    private HashMap<EthereumTx, Integer> intraShardTxCommitCount;
    private ThresholdMigrationPolicy migrationPolicy;
    private Set<EthereumAccount> accountsInMigration = new HashSet<>(); //hashset to save the current account that is migrating
    private PBFTShardedNode node;
    private HashMap<EthereumAccount, Integer> crossShardTransactionCount = new HashMap<>();
   // private int shardNumber = ((PBFTShardedNode) packet.getFrom()).getShardNumber(); 
  //  public ShardLoadTracker shardLoadTracker = new ShardLoadTracker(); // keeps track of the load of the shards.
    private int ShardLoad = 0;
   

    public ShardedClient(Simulator simulator,Network network, int nodeID, long downloadBandwidth, long uploadBandwidth, int timeBetweenTxs, boolean clientLed) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, new ShardedClientP2P());
        this.txs = new ArrayList<EthereumTx>();
        if(clientLed){
            this.protocol = new ClientLedEdgeNodeProtocol(this, network);
          //  this.migrationPolicy = new ThresholdMigrationPolicy(0, network, accountsInMigration, network.getNode(nodeID)); // migration policy called and set
        } else {
            this.protocol = new ShardLedEdgeNodeProtocol(this, network);
        }
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
                System.out.println("Sharded client class: processing and intrashard tx.... : shard N* :  " + shard);
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
        // generate a new transaction
        EthereumTx tx = TransactionFactory.sampleEthereumTransaction(network.getRandom());
        tx.setCreationTime(this.simulator.getSimulationTime());

        // System.out.println("Client generated new transaction: " + tx);
        // System.out.println("Size of transaction: " + tx.getSize());
        // for now, i will randomly select between 2 and 5 accounts
        // int numAccounts = network.getRandom().nextInt(3) + 2;
        int numAccounts = 2;


        ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
        for (int i = 0; i < numAccounts; i++) {
            EthereumAccount account = ((PBFTShardedNetwork) network).getRandomAccount(true);
            accounts.add(account);
            System.out.println("ACCOUNT received ********:" + account + " with Shard :" + ((PBFTShardedNetwork)this.network).getAccountShard(account));    

        }
         
          // set sender and receiver(s)
          tx.setSender(accounts.get(0));
          accounts.remove(0);
          tx.setReceiver(accounts.get(0));
          tx.setReceivers(accounts);
         // tx.setReceiver1(accounts.get(0)); // this is for migration
        //accounts.clear();
        txs.add(tx);
        System.out.println("siiiiiiiize : "+((PBFTShardedNetwork) this.network).accountToShard.size());
        System.out.println("SENDER :"  + tx.getSender());
        int senderShard = ((PBFTShardedNetwork)this.network).getAccountShard(tx.getSender()); // get sendershard N*
        EthereumAccount Sender = tx.getSender();
        // check if the sender shard is not the same as ANY of the receiver shards
        boolean crossShard = false;
        for (EthereumAccount account : tx.getReceivers()) {
            int receiverShard = ((PBFTShardedNetwork)this.network).getAccountShard(account);
            System.out.println("RECEIVER :"  + account + "With Shard "+ receiverShard);
            // increase the load of the receive shard,
            // this is supposed to prevent the next accounts to be in the same shard
            ((PBFTShardedNetwork)this.network).shardLoadTracker.updateLoad(receiverShard, 1);
            ShardloadEvent event = new ShardloadEvent(this.simulator.getSimulationTime(), receiverShard, ((PBFTShardedNetwork)this.network).shardLoadTracker.getLoad(receiverShard),0); 
            this.simulator.putEvent(event, 0); // log the receiver shard updated load
            if (senderShard != receiverShard) {
                if(((PBFTShardedNetwork)this.network).newAccountMigration2 == true){ // if new account migration is set to true
                    if(((PBFTShardedNetwork)this.network).shardLoadTracker.getLoad(receiverShard) < ((PBFTShardedNetwork)this.network).shardLoadTracker.getLoad(senderShard)){ // if the receiver shard has less load than the sender shard, migrate the sender account to the receiver shard
                        ((PBFTShardedNetwork)this.network).addAccount(Sender, receiverShard); // migrate the sender acc to the receiver shard
                        Sender.SetShard(receiverShard);
                        ((PBFTShardedNetwork)this.network).shardToAccounts.get(receiverShard).add(Sender); // adding sender account to the receiver shard
                        ShardloadEvent event2 = new ShardloadEvent(this.simulator.getSimulationTime(), receiverShard, ((PBFTShardedNetwork)this.network).shardLoadTracker.getLoad(receiverShard),1); 
                        this.simulator.putEvent(event2, 0); // log the receiver shard updated load
                        break;  
                    }
                    else{
                        crossShard = true;
                        break;
                    }
                }
                System.out.println("sedner "+ senderShard + " vs " + receiverShard);
                crossShard = true;
                break;
            }
            else{
                System.out.println("IntraShard Sender shard : " + senderShard + " Receiver shard : " + receiverShard);
            }
        }

        if (crossShard){      
          //  migrationPolicy.migrateIfNecessary(tx.getReceiver(), tx.getReceiver(),tx.getSender(), crossShardTransactionCount);
            // print the shards involved in the transaction
            ((PBFTShardedNetwork)this.network).shardLoadTracker.updateLoad(senderShard, 1);
            ((PBFTShardedNetwork)this.network).shardLoadTracker.updateLoad(tx.getReceiver().getShardNumber(), 1);
            ShardloadEvent event1 = new ShardloadEvent(this.simulator.getSimulationTime(), senderShard, ((PBFTShardedNetwork)this.network).shardLoadTracker.getLoad(senderShard),0); 
            this.simulator.putEvent(event1, 0); // log the receiver shard updated load
            ShardloadEvent event2 = new ShardloadEvent(this.simulator.getSimulationTime(), tx.getReceiver().getShardNumber(), ((PBFTShardedNetwork)this.network).shardLoadTracker.getLoad(tx.getReceiver().getShardNumber()),0); 
            this.simulator.putEvent(event2, 1); // log the receiver shard updated load
            System.out.println("CrossShard Transaction occuring....");
            System.out.println("Sender shard: " + senderShard);
            for (int i = 0; i < tx.getReceivers().size(); i++) {
                System.out.println("Receiver Account: " + ((PBFTShardedNetwork) this.network).getAccountShard(tx.getReceivers().get(i)));
            }
            // print the involved accounts
            System.out.println("Involved accounts: ");
            for (EthereumAccount tempAccount : tx.getAllInvolvedAccounts()){   
                System.out.println("INVOLVE ACCOUNTS IN PROCESS: " + tempAccount.getShardNumber());
                int accountCrossShardCount = crossShardTransactionCount.getOrDefault(tempAccount, 0);
                crossShardTransactionCount.put(tempAccount, accountCrossShardCount + 1);
                System.out.println(tempAccount);
            }
           
            ((PBFTShardedNetwork) this.network).clientCrossShardTransactions++;
            tx.setCrossShard(true);
            // create transaction creation event
            TransactionCreationEvent event = new TransactionCreationEvent(this.simulator.getSimulationTime(), tx,((PBFTShardedNetwork)this.network).clientIntraShardTransactions,  ((PBFTShardedNetwork) this.network).clientCrossShardTransactions++);
            this.simulator.putEvent(event, 0);
            this.sendCrossShardTransaction(tx);
            MigrationEvent event3 = new MigrationEvent(this.simulator.getSimulationTime(), null, senderShard, senderShard, 0, ((PBFTShardedNetwork) this.network).clientCrossShardTransactions , ((PBFTShardedNetwork) this.network).clientIntraShardTransactions,((PBFTShardedNetwork) this.network).committedTransactions, 0,    ((PBFTShardedNetwork) this.network).committedMigrations);
            this.simulator.putEvent(event3, 0);
            } else {
            // intra-Shard
           // ((PBFTShardedNetwork)this.network).shardLoadTracker.updateLoad(senderShard, 1);
            System.out.println("IntraShard Transaction occuring....");
            ((PBFTShardedNetwork) this.network).clientIntraShardTransactions++;
            this.intraShardTxCommitCount.put(tx, 0);
            tx.setCrossShard(false);
            // create transaction creation event
            TransactionCreationEvent event = new TransactionCreationEvent(this.simulator.getSimulationTime(), tx ,  ((PBFTShardedNetwork) this.network).clientIntraShardTransactions++,  ((PBFTShardedNetwork) this.network).clientCrossShardTransactions);
            this.simulator.putEvent(event, 0);
            this.sendTransaction(tx, senderShard);
            MigrationEvent event2 = new MigrationEvent(this.simulator.getSimulationTime(), null, senderShard, senderShard, 0, ((PBFTShardedNetwork) this.network).clientCrossShardTransactions , ((PBFTShardedNetwork) this.network).clientIntraShardTransactions,((PBFTShardedNetwork) this.network).committedTransactions, 0,((PBFTShardedNetwork) this.network).committedMigrations);
            this.simulator.putEvent(event2, 0);
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
     //   ((PBFTShardedNetwork) this.network).shardLoadTracker.updateLoad(shard, 1); // increase shard load
     
        ArrayList<PBFTShardedNode> nodes = ((PBFTShardedNetwork) this.network).getAllNodesFromShard(shard);
        for(Node node : nodes){
            this.networkInterface.addToUpLinkQueue(
                new Packet(this, node, 
                new DataMessage(tx))
            );
        }
    }

    private void sendCrossShardTransaction(EthereumTx tx) {
        this.protocol.sendTransaction(tx); // send tx
        
    }
}