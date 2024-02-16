package jabs.consensus.algorithm;

import java.util.Map;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.network.networks.Network;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.networks.sharded.ShardLoadTracker;

public class MigrationOfExistingAccounts implements MigrationPolicy {

    private ShardLoadTracker shardLoadTracker;
    private boolean Policy = false;
    public Network network;

    // Constructor to initialize with ShardLoadTracker
    public MigrationOfExistingAccounts(ShardLoadTracker shardLoadTracker, Network network) {
        this.shardLoadTracker = shardLoadTracker;
        this.network = network;
    }

  
    

   
    public boolean shouldMigrate(EthereumAccount account, int[] crossShardVector, int mainShard) {
        // Calculate the alignment vector based on shard load information
        int[] alignmentVector = calculateAlignmentVector();

        // Get the current shard of the account
        int currentShard = getCurrentShard(account);

        // Get the alignment of the account towards its current shard and the main shard
        int alignmentToCurrentShard = crossShardVector[currentShard];
        int alignmentToMainShard = crossShardVector[mainShard];

        // Calculate the sum of alignments towards other shards (excluding the current shard)
        int sumOfOtherAlignments = getSumOfOtherAlignments(alignmentVector, currentShard);

        // Determine if the account should be migrated based on the decision logic
        if (alignmentToCurrentShard < (sumOfOtherAlignments - alignmentToCurrentShard)) {
            ((PBFTShardedNetwork)this.network).Policy = "Main Shard";
            System.out.println("MAIN SHARD EXIST 9999999999999999999999999999999999999999999999999999999999");
            return true; // Migrate to the main shard
        } else {
           
            return false; // Remain in the current shard
        }
    }

    // Method to get the current shard of the account
    private int getCurrentShard(EthereumAccount account) {
        return account.getShardNumber();
    }

    // Method to calculate the sum of alignments towards other shards
    private int getSumOfOtherAlignments(int[] alignmentVector, int currentShard) {
        int sum = 0;
        for (int i = 0; i < alignmentVector.length; i++) {
            if (i != currentShard) {
                sum += alignmentVector[i];
            }
        }
        return sum;
    }

    // Method to calculate the alignment vector based on shard load information
    private int[] calculateAlignmentVector() {
        int[] alignmentVector = new int[shardLoadTracker.getShardLoads().size()];

        // Calculate alignment for each shard based on their loads
        for (Map.Entry<Integer, Integer> entry : shardLoadTracker.getShardLoads().entrySet()) {
            int shardId = entry.getKey();
            int shardLoad = entry.getValue();

            // Here you can define your own logic for alignment calculation based on load
            // For example, you can set alignment inversely proportional to load
            // This is just a placeholder, replace it with your actual logic
            alignmentVector[shardId] = 1 / shardLoad; // Placeholder logic
        }

        return alignmentVector;
    }


    @Override
    public void migrateIfNecessary(EthereumAccount account, EthereumAccount receiver, EthereumAccount sender,
            Map<String, Integer> crossShardTransactionCount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'migrateIfNecessary'");
    }

    @Override
    public void migrateAccount(EthereumAccount accounts, EthereumAccount receiverAccount,
            EthereumAccount currentAccount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'migrateAccount'");
    }

    public boolean isMainShardPolicyApplied() {
        return Policy;
    }


   
}
