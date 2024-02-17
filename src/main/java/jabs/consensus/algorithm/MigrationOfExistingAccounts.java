package jabs.consensus.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.networks.Network;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.networks.sharded.ShardLoadTracker;
import java.util.*;


public class MigrationOfExistingAccounts implements MigrationPolicy {
    private ShardLoadTracker shardLoadTracker;
    private boolean mainShardPolicyApplied = false;
    private List<Integer> transactionHistory = new ArrayList<>();
    Network network;
    private static final int K = 100; // Number of blocks to consider for transaction history

    // Constructor to initialize with ShardLoadTracker
    public MigrationOfExistingAccounts(ShardLoadTracker shardLoadTracker, Network network) {
        this.shardLoadTracker = shardLoadTracker;
        this.network = network;
    }

    public boolean shouldMigrate(EthereumAccount account, int[] crossShardVector, int mainShard, boolean activate) {
        // Calculate the alignment vector based on shard load information and transaction history
        if(activate == true)  {
            int[] alignmentVector = calculateAlignmentVector(crossShardVector);

            // Get the current shard of the account
            int currentShard = getCurrentShard(account);

            // Get the alignment of the account towards its current shard and the main shard
            int alignmentToCurrentShard = crossShardVector[currentShard];
            int alignmentToMainShard = crossShardVector[mainShard];

            // Calculate the sum of alignments towards other shards (excluding the current shard)
            int sumOfOtherAlignments = getSumOfOtherAlignments(alignmentVector, currentShard);

            // Determine if the account should be migrated
            if (alignmentToCurrentShard < (sumOfOtherAlignments - alignmentToCurrentShard)) {
                mainShardPolicyApplied = true;
                ((PBFTShardedNetwork)this.network).Policy = "Main Shard ";
                return true; // Migrate to the main shard
            } else {
                return false; // Remain in the current shard
            }
        }
        else{
            return false;
        }
    }

    // Method to calculate the alignment vector
    // iterates through each shard, calculates alignment inversely proportional to load, and adds the recent transaction count to adjust the alignment.
    // the shard with the least alignment vector is what we want
    private int[] calculateAlignmentVector(int[] crossShardVector) {
        int[] alignmentVector = new int[shardLoadTracker.getShardLoads().size()];

        // Update alignment for each shard based on their loads and transaction history
        for (Map.Entry<Integer, Integer> entry : shardLoadTracker.getShardLoads().entrySet()) {
            int shardId = entry.getKey();
            int shardLoad = entry.getValue();

            // Calculate alignment based on shard load
            int alignment = 1 / shardLoad;

            // Adjust alignment based on transaction history
            int transactionsInShard = countTransactionsInShard(shardId);
            alignment += transactionsInShard;

            alignmentVector[shardId] = alignment;
        }

        return alignmentVector;
    }

    // Method to count transactions in a shard from the transaction history
    private int countTransactionsInShard(int shardId) {
        int count = 0;
        for (int transaction : transactionHistory) {
            if (transaction == shardId) {
                count++;
            }
        }
        return count;
    }

    // Method to update transaction history with transactions from the last k blocks
    public void updateTransactionHistory(ArrayList<EthereumTx> transactions) {
    // Add shard Numbers from the current block to the history
    for (EthereumTx tx : transactions) {
       
        int senderShard = tx.getSender().getShardNumber();
        int receiverShard = tx.getReceiver().getShardNumber();
        
        transactionHistory.add(senderShard);
        transactionHistory.add(receiverShard);
    }

    // If transaction history exceeds k blocks, remove transactions from the oldest block
    while (transactionHistory.size() > K) {
        transactionHistory.remove(0);
        transactionHistory.remove(0); // Assuming each transaction adds two shard IDs (sender and receiver)
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

    @Override
    public void migrateIfNecessary(EthereumAccount account, EthereumAccount receiver, EthereumAccount sender,
                                   Map<String, Integer> crossShardTransactionCount, boolean activate) {
        // Method not implemented for this policy
        throw new UnsupportedOperationException("Unimplemented method 'migrateIfNecessary'");
    }

    @Override
    public void migrateAccount(EthereumAccount accounts, EthereumAccount receiverAccount,
                               EthereumAccount currentAccount) {
        // Method not implemented for this policy
        throw new UnsupportedOperationException("Unimplemented method 'migrateAccount'");
    }

    // Getter method to check if main shard policy was applied
    public boolean isMainShardPolicyApplied() {
        return mainShardPolicyApplied;
    }
}
