package jabs.consensus.algorithm;
import java.util.Map;
import java.util.Set;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.simulator.event.MigrationEvent;

// ThresholdMigrationPolicy implementation
public class ThresholdMigrationPolicy implements MigrationPolicy {
     private final int migrationThreshold;
    private PBFTShardedNetwork network;
    private Set<EthereumAccount> accountsInMigration;
    private PBFTShardedNode node;
   // public int MigrationCount = 0;

    public ThresholdMigrationPolicy(int migrationThreshold, PBFTShardedNetwork network, Set<EthereumAccount> accountsInMigration, PBFTShardedNode node) {
        this.migrationThreshold = migrationThreshold;
        this.accountsInMigration = accountsInMigration;
        this.node = node;
        this.network = (PBFTShardedNetwork) node.getNetwork();
       // this.MigrationCount = 0;
    }
    

    // Migration logic
    @Override
    public void migrateIfNecessary(EthereumAccount account, EthereumAccount receiver, EthereumAccount sender,Map<EthereumAccount, Integer> crossShardTransactionCount ) {
      //  int migrationThreshold = 3; // Set threshold value manually for now
        // Increment the cross-shard transaction count for the current account
        int accountCrossShardCount = crossShardTransactionCount.get(account);
      //  crossShardTransactionCount.put(account, accountCrossShardCount + 1);
        // Print debug information
        System.out.println("Account: " + account);
        System.out.println("Current cross-shard count: " + crossShardTransactionCount.get(account));
        // Check if the migration threshold is reached for the current account
        if (accountCrossShardCount >= migrationThreshold) {
            System.out.println("5555555555555555555555555555");
            System.out.println("Sender: " + sender.getShardNumber() + " Receiver :" + receiver.getShardNumber());
            migrateAccount(account, receiver, sender);
            // Reset the count after migration
            crossShardTransactionCount.put(account, 0);
            //crossShardTransactionCount.put(sender, 0);
        }
    }



    @Override
    public void migrateAccount(EthereumAccount accounts, EthereumAccount receiverAccount, EthereumAccount currentAccount) {
       // MigrationCount++;
       // ((PBFTShardedNetwork)network).MigrationCounts++;  
      // newShard = network.getRandomAccount(true).getShardNumber(); // random shard to send the account to for now, soon need to send to only the shards that are in for cross-shard transactions
       // network.accountToShard.put(currentAccount, receiverAccount.getShardNumber()); // store the account in the new shard
       // network.accountToShard.remove(currentAccount);
        network.addAccount(currentAccount, receiverAccount.getShardNumber());
        System.out.println("Account :" + currentAccount + "  Now in shard N* :" + network.getAccountShard(currentAccount));
        // Log or notify about the account migration
        System.out.println("Account " + currentAccount + " migrated from Shard " +  currentAccount.getShardNumber() + " to Shard " + receiverAccount.getShardNumber());
        accountsInMigration.add(currentAccount);
        // Create a migration event
        MigrationEvent migrationEvent = new MigrationEvent(node.getSimulator().getSimulationTime(), currentAccount, currentAccount.getShardNumber(), receiverAccount.getShardNumber(),migrationThreshold,((PBFTShardedNetwork)this.network).clientCrossShardTransactions, ((PBFTShardedNetwork)this.network).clientIntraShardTransactions, ((PBFTShardedNetwork)this.network).committedTransactions,((PBFTShardedNetwork)network).MigrationCounts++);
        // Put the migration event into the simulator's event queue
        node.getSimulator().putEvent(migrationEvent, 0);
    }

}
