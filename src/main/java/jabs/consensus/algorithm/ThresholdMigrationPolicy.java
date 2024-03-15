package jabs.consensus.algorithm;
import java.util.Map;
import java.util.Set;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.network.networks.Network;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.simulator.event.MigrationEvent;
import jabs.simulator.event.ShardloadEvent;

// ThresholdMigrationPolicy implementation
public class ThresholdMigrationPolicy implements MigrationPolicy {
     private final int migrationThreshold;
    private PBFTShardedNetwork network;
    private Set<EthereumAccount> accountsInMigration;
    private Node node;
    private Boolean Policy = false;
   // public int MigrationCount = 0;

    public ThresholdMigrationPolicy(int migrationThreshold, Network network, Set<EthereumAccount> accountsInMigration, Node node) {
        this.migrationThreshold = migrationThreshold;
        this.accountsInMigration = accountsInMigration;
        this.node = node;
        this.network = (PBFTShardedNetwork) node.getNetwork();
       // this.MigrationCount = 0;
    }
    

    @Override
public void migrateIfNecessary(EthereumAccount receiver, EthereumAccount sender, Map<String, Integer> crossShardTransactionCount, boolean activate) {
    if(activate){
    // Get the unique identifier for the transaction involving both sender and receiver
    String transactionKey = sender.getShardNumber() + "-" + receiver.getShardNumber();

    // Get the current count for the unique identifier
    int accountCrossShardCount = crossShardTransactionCount.getOrDefault(transactionKey, 0);

    // debug information
    System.out.println("receiver : " + receiver);
    System.out.println("sender :" + sender);
    System.out.println("Current cross-shard count: " + accountCrossShardCount);
    
    // Check if the migration threshold is reached for the current account
    if (accountCrossShardCount >= migrationThreshold) {
        ((PBFTShardedNetwork)this.network).Policy = "Data Structure Policy";
        System.out.println("5555555555555555555555555555");
        System.out.println("Sender: " + sender.getShardNumber() + " Receiver :" + receiver.getShardNumber());
        migrateAccount(receiver.getShardNumber(), sender);
        // Reset the count after migration
        crossShardTransactionCount.put(transactionKey, 0);
        }
    }
}




    @Override
    public void migrateAccount (int receiverShard, EthereumAccount currentAccount) {
       // MigrationCount++;
        ((PBFTShardedNetwork)network).MigrationCounts++;
      // newShard = network.getRandomAccount(true).getShardNumber(); // random shard to send the account to for now, soon need to send to only the shards that are in for cross-shard transactions
       // network.accountToShard.put(currentAccount, receiverShard.getShardNumber()); // store the account in the new shard
       // network.accountToShard.remove(currentAccount);
       ((PBFTShardedNetwork)network).clientCrossShardTransactions++;
       
       ((PBFTShardedNetwork)this.network).addAccount(currentAccount, receiverShard);
        currentAccount.SetShard(receiverShard);
        System.out.println(((PBFTShardedNetwork)network).shardToAccounts.get(receiverShard));
        ((PBFTShardedNetwork)this.network).shardToAccounts.get(receiverShard).add(currentAccount);
        System.out.println("Account :" + currentAccount + "  Now in shard N* :" + network.getAccountShard(currentAccount));
        // Log or notify about the account migration
        System.out.println("Account " + currentAccount + " migrated from Shard " +  currentAccount.getShardNumber() + " to Shard " + receiverShard + " SUCCESSFULLY ");
        accountsInMigration.add(currentAccount);
        ((PBFTShardedNetwork)this.network).shardLoadTracker.updateLoad(receiverShard, 1);
        // Create a migration event
        MigrationEvent migrationEvent = new MigrationEvent(node.getSimulator().getSimulationTime(), currentAccount, currentAccount.getShardNumber(), receiverShard,migrationThreshold,((PBFTShardedNetwork)this.network).clientCrossShardTransactions, ((PBFTShardedNetwork)this.network).clientIntraShardTransactions, ((PBFTShardedNetwork)this.network).committedTransactions,((PBFTShardedNetwork)network).MigrationCounts,((PBFTShardedNetwork)network).committedMigrations);
        // Put the migration event into the simulator's event queue
        node.getSimulator().putEvent(migrationEvent, 0);
        currentAccount.MigrateStatus(true);
        ((PBFTShardedNetwork) this.network).committedMigrations++;
        ShardloadEvent event = new ShardloadEvent(node.getSimulator().getSimulationTime(), currentAccount.getShardNumber(), ((PBFTShardedNetwork)this.network).shardLoadTracker.getLoad(receiverShard),1); 
        node.getSimulator().putEvent(event, 0); // log the receiver shard updated load
    }


  

}
