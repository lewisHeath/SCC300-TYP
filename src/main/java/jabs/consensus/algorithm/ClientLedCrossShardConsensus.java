package jabs.consensus.algorithm;

import jabs.network.message.CoordinationMessage;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.networks.sharded.ShardLoadTracker;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.simulator.event.AccountLockingEvent;
import jabs.simulator.event.AccountUnlockingEvent;
import jabs.simulator.event.MigrationEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import jabs.ledgerdata.Sharding.CrossShardTransaction;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.ledgerdata.pbft.PBFTBlock;

public class ClientLedCrossShardConsensus implements CrossShardConsensus {

    private int ShardLoad = 0;
    private PBFTShardedNode node;
    // data structure for tracking locked accounts
    private ArrayList<EthereumAccount> lockedAccounts = new ArrayList<EthereumAccount>();
    private ArrayList<EthereumTx> preparedTransactions = new ArrayList<EthereumTx>();
    private HashMap<EthereumTx, Node> preparedTransactionsFrom = new HashMap<EthereumTx, Node>();
    private HashMap<EthereumAccount, EthereumTx> lockedAccountsToTransactions = new HashMap<EthereumAccount, EthereumTx>();
    private ArrayList<EthereumTx> abortedTxs = new ArrayList<EthereumTx>();
    private int thisID;
    private int nodesInShard;
    private PBFTShardedNetwork network;
    //public int MigrationCount = 0; // counter for crosshard transactions occuring
    private EthereumAccount currentShard;
    private EthereumAccount ReceiverShard;
    private int newShard = 0;
    private Set<EthereumAccount> accountsInMigration = new HashSet<>(); //hashset to save the current account that is migrating
    private HashMap<String, Integer> crossShardTransactionCount = new HashMap<>();
    private int clientCrossShardTransactions;
    private ThresholdMigrationPolicy migrationPolicy;
    private ShardLoadTracker shardLoadTracker = new ShardLoadTracker();
    private MigrationOfExistingAccounts existingAccountsMigration;
    private int[] CrossShardVector; // this is  the alinment vector
    private int PolicyUse = 1;
    private boolean migrationApproved = false;
    

    public ClientLedCrossShardConsensus(PBFTShardedNode node) {
        this.node = node;
        this.nodesInShard = ((PBFTShardedNetwork) node.getNetwork()).getNodesPerShard();
        this.network = (PBFTShardedNetwork) node.getNetwork();
        this.clientCrossShardTransactions = 0;
        this.migrationPolicy = new ThresholdMigrationPolicy(2, this.network, accountsInMigration, this.node); // migration policy called and set
        this.existingAccountsMigration = new MigrationOfExistingAccounts(shardLoadTracker, this.network);
    }

    public void setID(int ID){
        this.thisID = ID;
    }

    public void processCoordinationMessage(CoordinationMessage message, Node from) {
        // System.out.println("Processing coordination message");
        // get the from node that was stored in the message
        Node messageFrom = message.getFrom();
        // get the transaction from the message
        EthereumTx tx = (EthereumTx) message.getData();
        // get all involved accounts in the transaction and store them in an ArrayList
        ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
        // this needs upgrading to handle smart contract transactions
        accounts.addAll(tx.getAllInvolvedAccounts());
        // get the accounts that are mapped to this shard
        ArrayList<EthereumAccount> shardAccounts = node.getShardAccounts();
        ArrayList<EthereumAccount> accountsInThisShard = new ArrayList<EthereumAccount>();
        // check which accounts are in this shard
        for (EthereumAccount account : accounts) {
            if (shardAccounts.contains(account)) {
                accountsInThisShard.add(account);
            }
        }

        switch (message.getType()) {
            case "pre-prepare":
                processPrePrepareMessage(accountsInThisShard, messageFrom, tx);
                break;
            case "commit":
                processCommitMessage(tx, from);
                break;
            case "rollback":
                processRollbackMessage(accountsInThisShard, tx);
                break;
            case "migration_request":
                processMigrationRequest(accountsInThisShard, message, tx, messageFrom);
                break;
            case "migration_approved":
                processMigrationApproval(message);
                break;
            case "migration_rejected":
                processMigrationRejection(message);
                break;
            default:
                System.out.println(message.getType());
                throw new RuntimeException("Unknown message type");
                
        }
    }

    private void processPrePrepareMessage(ArrayList<EthereumAccount> accountsInThisShard, Node from, EthereumTx tx) {
        if(abortedTxs.contains(tx) || from instanceof PBFTShardedNode){
            return;
        }
        // add the transaction and the client node to the prepared transactions from
        preparedTransactionsFrom.put(tx, from);
        // check if the accounts for the transaction is locked
        for (EthereumAccount account : accountsInThisShard) {
            if (lockedAccounts.contains(account)) {
                // if the account is locked, send a prepareNOTOK message back to the client node
                CoordinationMessage message = new CoordinationMessage(tx, "prepareNOTOK");
                // IF THIS IS NODE 0, TELL ALL SHARD NODES TO SEND PREPARENOTOK
                // node.sendMessageToNode(message, from);
                if(thisID == 0){
                    node.broadcastMessage(new CoordinationMessage(tx, "pre-prepare", this.node));
                    ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
                    // FORCE MESSAGE
                    for (PBFTShardedNode node : nodes) {
                        node.sendMessageToNode(message, from);
                    }
                }
                return;
            }
        }
        // if the accounts are not locked, lock them
        for (EthereumAccount account : accountsInThisShard) {
            lockedAccounts.add(account);
            lockedAccountsToTransactions.put(account, tx);
            // account locking event
            AccountLockingEvent event = new AccountLockingEvent(this.node.getSimulator().getSimulationTime(), account, this.node);
            if(thisID == 0) this.node.getSimulator().putEvent(event, 0);
        }
        // send prepareOK message back to the client node
        CoordinationMessage message = new CoordinationMessage(tx, "prepareOK");
        // IF THIS IS NODE 0 TELL ALL SHARD NODES TO SEND PREPAREOK
        // node.sendMessageToNode(message, from);
        if(thisID == 0){
            node.broadcastMessage(new CoordinationMessage(tx, "pre-prepare", this.node));
            ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
            // FORCE MESSAGE
            for (PBFTShardedNode node : nodes) {
                node.sendMessageToNode(message, from);
            }
        }
        // System.out.println("Sending prepareOK message back to client node");
        // add the transaction to the prepared transactions list
        preparedTransactions.add(tx);
    }

    private void processCommitMessage(EthereumTx tx, Node from) {   ////////////////// 55555       here
        // add the transaction to the mempool
        if (preparedTransactionsFrom.get(tx) == from) {
            node.processNewTx(tx, from);
        }
    }

    private void processRollbackMessage(ArrayList<EthereumAccount> accountsInThisShard, EthereumTx tx) {
        // remove the transaction from the prepared transactions list
        preparedTransactions.remove(tx);
        // remove the transaction and the client node from the prepared transactions
        // from list
        preparedTransactionsFrom.remove(tx);
        abortedTxs.add(tx);
        // unlock the accounts only if the tx passed to this was the one which locked
        // the accounts
        for (EthereumAccount account : accountsInThisShard) {
            if (lockedAccountsToTransactions.get(account) == tx) {
                lockedAccounts.remove(account);
                // account unlocking event
                AccountUnlockingEvent event = new AccountUnlockingEvent(this.node.getSimulator().getSimulationTime(), account, this.node);
                if(thisID == 0) this.node.getSimulator().putEvent(event, 0);
                lockedAccountsToTransactions.remove(account);
            }
        }
    }

    public void processConfirmedBlock(PBFTBlock block) {
        // get the transactions from the block
        ArrayList<EthereumTx> transactions = block.getTransactions();
        existingAccountsMigration.updateTransactionHistory(transactions); // updates the history of transactions, for the last 100 blocks
        for (EthereumTx tx : transactions) {
            // Update cross-shard transaction count for involved accounts
            EthereumAccount sender = tx.getSender();
             ArrayList<EthereumAccount> receivers = tx.getReceivers();

            for (EthereumAccount receiver : receivers) {
                System.out.println("INVOLVE ACCOUNTS IN PROCESS: " + sender.getShardNumber() + " -> " + receiver.getShardNumber());

                // Create an identifier for the transaction involving both sender and receiver
                String transactionKey = sender.getShardNumber() + "-" + receiver.getShardNumber();

                // Getting the current count for the unique identifier
                int transactionCount = crossShardTransactionCount.getOrDefault(transactionKey, 0);

                // Incrementing the count
                crossShardTransactionCount.put(transactionKey, transactionCount + 1);
            }
        
            // if the transaction is in the prepared transactions list, unlock the accounts
            if (preparedTransactionsFrom.containsKey(tx)) {
                ArrayList<EthereumAccount> accounts = new ArrayList<>(tx.getAllInvolvedAccounts());
                // unlock the accounts
                System.out.println("size of locked accounts before confirming: " + lockedAccounts.size());
                for (EthereumAccount account : accounts) {
                    lockedAccounts.remove(account);
                    // account unlocking event
                    AccountUnlockingEvent event = new AccountUnlockingEvent(this.node.getSimulator().getSimulationTime(), account, this.node);
                    if (thisID == 0) this.node.getSimulator().putEvent(event, 0);
                }
                lockedAccounts.removeAll(accounts);
                System.out.println("size of locked accounts after confirming: " + lockedAccounts.size());
                // send a committed message to the client node
                CoordinationMessage message = new CoordinationMessage(tx, "committed");
                // NODE 0 TELL ALL NODES WHAT TO DO
                if (thisID == 0) {
                    ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
                    // FORCE MESSAGE
                    for (PBFTShardedNode node : nodes) {
                        node.sendMessageToNode(message, preparedTransactionsFrom.get(tx));
                    }
                }
                initiateMigration(tx);
               /*   // declare the vector array with the size of the number of shards
                int[] CrossShardVector = new int[((PBFTShardedNetwork)this.network).getNumberOfShards()];
                  // Check for migration
                int senderShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getSender());
                for (EthereumAccount account : tx.getReceivers()) {

                    int receiverShard = ((PBFTShardedNetwork) this.network).getAccountShard(account);
                    //this is just for detection whether we on crossshard or not
                    // if crossshard, migrate
                    if (senderShard != receiverShard && ((PBFTShardedNetwork)this.network).migrate() == true) {
                        // Add a load to the used shard
                        int senderShardLoad = shardLoadTracker.getLoad(senderShard);
                        shardLoadTracker.addShard(senderShardLoad, ShardLoad + 1);
                        // Add a load to the used shard // this keeps track of how many times the shards are interacting
                        int receiverShardLoad = shardLoadTracker.getLoad(receiverShard);
                        shardLoadTracker.addShard(receiverShardLoad, ShardLoad + 1);
                        migrationPolicy.migrateIfNecessary(account, tx.getReceiver(),tx.getSender(), crossShardTransactionCount, true); // policy 1 ( data structure)
                        boolean migrate_mainshard = existingAccountsMigration.shouldMigrate(account, CrossShardVector, senderShard, true); // policy 2, if mainshard has less load balance, return true
                        if(migrate_mainshard == true) { // migrate to main shard                   // main shard accounts                       // sender account to be migrated     
                            migrationPolicy.migrateAccount(account, ((PBFTShardedNetwork)this.network).getAccountByShardNumber(shardLoadTracker.getLeastLoadedShard()), account);
        
                        }
                    } else {
                        System.out.println("Intra shard DETECTED, No need to migrate");
                        } 
                        
                   }*/ 
            }
        }
    }

    private void executeMigration(EthereumTx tx, int receiverShard) {
        // Get the sender shard and initialize CrossShardVector
        int senderShard = ((PBFTShardedNetwork) this.network).getAccountShard(tx.getSender());
        int[] CrossShardVector = new int[((PBFTShardedNetwork) this.network).getNumberOfShards()];
        // Increment sender shard load
        int senderShardLoad = shardLoadTracker.getLoad(senderShard);
        shardLoadTracker.addShard(senderShardLoad, 1);
        // Increment receiver shard load
        int receiverShardLoad = shardLoadTracker.getLoad(receiverShard);
        shardLoadTracker.addShard(receiverShardLoad, 1);
        // Execute migration for each receiver account
        for (EthereumAccount account : tx.getReceivers()) {
            // Perform migration if necessary
            migrationPolicy.migrateIfNecessary(account, tx.getReceiver(), tx.getSender(), crossShardTransactionCount, false);
            // Check if migration to main shard is needed
            boolean migrateMainShard = existingAccountsMigration.shouldMigrate(account, CrossShardVector, senderShard, true);
            if (migrateMainShard) {
                // Get the least loaded shard and migrate the account
                int leastLoadedShard = shardLoadTracker.getLeastLoadedShard();
                EthereumAccount leastLoadedAccount = ((PBFTShardedNetwork) this.network).getAccountByShardNumber(leastLoadedShard);
                migrationPolicy.migrateAccount(account, leastLoadedAccount, account);
            }
        }
    }
    



        // Method to initiate migration
    public void initiateMigration(EthereumTx tx) {
        // Broadcast migration request message to other nodes
        broadcastMigrationRequest(tx);
    }

    // Method to broadcast migration request message to other nodes
    private void broadcastMigrationRequest(EthereumTx tx) {
        CoordinationMessage message = new CoordinationMessage(tx, "migration_request", this.node);
        node.broadcastMessage(message);
    }

    private void processMigrationRequest(ArrayList<EthereumAccount> accountsInThisShard ,CoordinationMessage message, EthereumTx tx, Node from) {
        // Initiate consensus for migration approval
        // Depending on your consensus algorithm, you may need to send messages, collect votes, and wait for a decision
        // Once consensus is reached, invoke processMigrationApproval or processMigrationRejection based on the outcome
        // For example:
              // if the accounts are not locked, lock them
          // if the accounts are not locked, lock them


          for (EthereumAccount account : accountsInThisShard) {
            if (lockedAccounts.contains(account)) {
                // if the account is locked, send a prepareNOTOK message back to the client node
                message = new CoordinationMessage(tx, "migration_rejected");
                // IF THIS IS NODE 0, TELL ALL SHARD NODES TO SEND PREPARENOTOK
                // node.sendMessageToNode(message, from);
                if(thisID == 0){
                    node.broadcastMessage(new CoordinationMessage(tx, "migration_request", this.node));
                    ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
                    // FORCE MESSAGE
                    for (PBFTShardedNode node : nodes) {
                        node.sendMessageToNode(message, from);
                    }
                }
                return;
            }
        }
          for (EthereumAccount account : accountsInThisShard) {
            lockedAccounts.add(account);
            lockedAccountsToTransactions.put(account, tx);
            // account locking event
            AccountLockingEvent event = new AccountLockingEvent(this.node.getSimulator().getSimulationTime(), account, this.node);
            if(thisID == 0) this.node.getSimulator().putEvent(event, 0);
        }
        // send prepareOK message back to the client node
        message = new CoordinationMessage(tx, "migration_approved");
        // IF THIS IS NODE 0 TELL ALL SHARD NODES TO SEND PREPAREOK
        // node.sendMessageToNode(message, from);
        if(thisID == 0){
            node.broadcastMessage(new CoordinationMessage(tx, "migration_request", this.node));
            ArrayList<PBFTShardedNode> nodes = this.network.getShard(node.getShardNumber());
            // FORCE MESSAGE
            for (PBFTShardedNode node : nodes) {
                node.sendMessageToNode(message, from);
            }
        }
        // System.out.println("Sending prepareOK message back to client node");
        // add the transaction to the prepared transactions list
        executeMigration(tx, newShard);
    }


    private void processMigrationApproval(CoordinationMessage message) {
        EthereumTx tx = (EthereumTx) message.getData();
        int receiverShard = tx.getReceiver().getShardNumber();
        executeMigration(tx, receiverShard);
    }
    
    private void processMigrationRejection(CoordinationMessage message) {
        EthereumTx tx = (EthereumTx) message.getData();
        int receiverShard = tx.getReceiver().getShardNumber();
        
    }
    
    

    private boolean isAccountInMigration(EthereumAccount account) {
    // return true if the account is migrating, or migrated   
        return accountsInMigration.contains(account);
    }


    public Boolean areAccountsLocked(ArrayList<EthereumAccount> accounts) {
        for (EthereumAccount account : accounts) {
            if (lockedAccounts.contains(account)) {
                return true;
            }
        }
        return false;
    }

    public Boolean isLocked(EthereumAccount account) {
        return lockedAccounts.contains(account);
    }

    public Boolean areAllAccountsInThisShard(ArrayList<EthereumAccount> accounts) {
        // get the accounts that are mapped to this shard
        ArrayList<EthereumAccount> shardAccounts = node.getShardAccounts();
        // check which accounts are in this shard
        int i = 0;
        for (EthereumAccount account : accounts) {
            if (shardAccounts.contains(account)) {
                i++;
            }
        }
        return i == accounts.size();
    }


    private int getReceiverShard( EthereumTx tx) {
        for (int receiverShard : tx.getAllInvolvedShards() ) {
            int receiverShards = receiverShard;
            System.out.println("GETRECEIVEDSHARD FUNCTION" + receiverShards );
            return receiverShard;
        }
        return 0;  
    }
    

}
