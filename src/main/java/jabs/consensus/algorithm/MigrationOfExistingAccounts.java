package jabs.consensus.algorithm;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.networks.Network;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.networks.sharded.ShardLoadTracker;
import java.util.*;


public class MigrationOfExistingAccounts implements MigrationPolicy {
    private ShardLoadTracker shardLoadTracker;
    private boolean mainShardPolicyApplied = false;
    private List<Pair<EthereumAccount, EthereumAccount>> transactionHistory = new ArrayList<>();
    Network network;
    private static final int K = 100; // Number of blocks to consider for transaction history

    // Constructor to initialize with ShardLoadTracker
    public MigrationOfExistingAccounts(ShardLoadTracker shardLoadTracker, Network network) {
        this.shardLoadTracker = shardLoadTracker;
        this.network = network;
    }

    public boolean shouldMigrate(EthereumAccount account, EthereumAccount[][] crossShardVector, int mainShard, boolean activate) {
        // Calculate the alignment vector based on shard load information and transaction history
        if (activate) {
            EthereumAccount[][] alignmentVector = calculateAlignmentVector(crossShardVector);
    
            // Get the current shard of the account
            int currentShard = getCurrentShard(account);
    
                // Getting the alignment of the account towards its current shard and the main shard and checking if they are null
            int alignmentToCurrentShard = (crossShardVector[currentShard] != null) ? crossShardVector[currentShard].length + 1 : 0;
        //    int alignmentToMainShard = (crossShardVector[mainShard] != null) ? crossShardVector[mainShard].length + 1 : 0;

    
            // Calculate the sum of alignments towards other shards (excluding the current shard)
            int sumOfOtherAlignments = getSumOfOtherAlignments(alignmentVector, account, currentShard);
    
            // Determine if the account should be migrated
            if (alignmentToCurrentShard < (sumOfOtherAlignments - alignmentToCurrentShard)) {
                return true; // Migrate to the main shard
            } else {
                return false; // Remain in the current shard
            }
        } else {
            return false;
        }
    }

    // Method to calculate the alignment vector
    // iterates through each shard, calculates alignment inversely proportional to load, and adds the recent transaction count to adjust the alignment.
    // the shard with the least alignment vector is what we want
    private EthereumAccount[][] calculateAlignmentVector(EthereumAccount[][] crossShardVector) {
        EthereumAccount[][] alignmentVector = new EthereumAccount[shardLoadTracker.getShardLoads().size()][];
        // Update alignment for each shard based on their loads and transaction history
        for (Map.Entry<Integer, Integer> entry : shardLoadTracker.getShardLoads().entrySet()) {
            int shardNumber = entry.getKey(); // get shard number
            int shardLoad = entry.getValue(); // and get shard load
            // Calculate alignment based on shard load
            int alignment = 1 / shardLoad;
            // Adjust alignment based on transaction history / if shard have had many transactions,
            // increase the alignment as well
            int transactionsInShard = countTransactionsInShard(shardNumber); // get how many transactions were in the shard
            alignment += transactionsInShard; // increase the alignment 
            // Assign alignment vector for the shard
            alignmentVector[shardNumber] = new EthereumAccount[alignment];
        }
    
        return alignmentVector;
    }
    
    private int getSumOfOtherAlignments(EthereumAccount[][] alignmentVector, EthereumAccount account, int currentShard) {
        int sum = 0;
        // loop around and increase everytime we are not in the current shard
        for (int i = 0; i < alignmentVector.length; i++) {
            if (i != currentShard) { 
                sum += alignmentVector[i].length;
            }
        }
        System.out.println("Alignment towards other shards :" + sum);
        return sum;
    }
    
    // Method to update transaction history with transactions from the last k blocks
    public void updateTransactionHistory(ArrayList<EthereumTx> transactions, EthereumAccount[][] alignmentVector) {
        // Add sender and receiver accounts from the current block to the history
        for (EthereumTx tx : transactions) {
            EthereumAccount senderAccount = tx.getSender();
            EthereumAccount receiverAccount = tx.getReceiver();
            
            // Add sender and receiver accounts to the transaction history
            transactionHistory.add(new Pair<>(senderAccount, receiverAccount));
            
            // Update alignment vector
            updateAlignmentVector(senderAccount, receiverAccount, alignmentVector);
        }

        // If transaction history exceeds k blocks, remove transactions from the oldest block
        while (transactionHistory.size() > K) {
            // Remove the oldest transaction from history
            Pair<EthereumAccount, EthereumAccount> removedTransaction = transactionHistory.remove(0);
            // Remove the transaction from alignment vector
            removeTransactionFromAlignmentVector(removedTransaction.getFirst(), removedTransaction.getSecond(), alignmentVector);
        }
    }

    // Method to remove transaction data from the alignment vector
    private void removeTransactionFromAlignmentVector(EthereumAccount senderAccount, EthereumAccount receiverAccount, EthereumAccount[][] alignmentVector) {
        int sender = senderAccount.getAccountNumber();
        int receiver = receiverAccount.getAccountNumber();
        
        // remove alignment for sender's shard
        if (alignmentVector[sender] != null && alignmentVector[sender].length > receiver) {
            alignmentVector[sender][receiver] = null;
        }
        
        // remove alignment for receiver's shard
        if (alignmentVector[receiver] != null && alignmentVector[receiver].length > sender) {
            alignmentVector[receiver][sender] = null;
        }
    }
    
    
    private void updateAlignmentVector(EthereumAccount sender, EthereumAccount receiver, EthereumAccount[][] alignmentVector) {
        // Update alignment vector based on sender and receiver accounts
        int senderShard = sender.getShardNumber();
        int receiverShard = receiver.getShardNumber();
        
        // add alignment for sender's shard
        if (alignmentVector[sender.getAccountNumber()] != null) {
            if (alignmentVector[sender.getAccountNumber()][senderShard] != null) {
                alignmentVector[sender.getAccountNumber()][senderShard] = sender; // Update the Account
            } else {
                alignmentVector[sender.getAccountNumber()][senderShard] = sender; // Store the EthereumAccount 
            }
        } else {
            alignmentVector[sender.getAccountNumber()] = new EthereumAccount[shardLoadTracker.getShardLoads().size()];
            alignmentVector[sender.getAccountNumber()][senderShard] = sender; // Store the EthereumAccount 
        }
        
        // add alignment for receiver's shard
        if (alignmentVector[receiver.getAccountNumber()] != null) {
            if (alignmentVector[receiver.getAccountNumber()][receiverShard] != null) {
                alignmentVector[receiver.getAccountNumber()][receiverShard] = receiver; // Update the Account 
            } else {
                alignmentVector[receiver.getAccountNumber()][receiverShard] = receiver; // Store the Account 
            }
        } else { // if it doesnt exist, declare the size of it as it shardload, and add the account
            alignmentVector[receiver.getAccountNumber()] = new EthereumAccount[shardLoadTracker.getShardLoads().size()];
            alignmentVector[receiver.getAccountNumber()][receiverShard] = receiver; // Store the Account 
        }
    }
    
    // Method to count transactions in a shard from the transaction history
    private int countTransactionsInShard(int shardNumber) {
        int count = 0;
        for (Pair<EthereumAccount, EthereumAccount> transaction : transactionHistory) {
            EthereumAccount senderAccount = transaction.getFirst(); // get sender
            EthereumAccount receiverAccount = transaction.getSecond(); // get receiver

            if (senderAccount.getShardNumber() == shardNumber || receiverAccount.getShardNumber() == shardNumber) {
                count++; // increase
            }
        }
        return count; 
    }

    // Method to get the current shard of the account
    private int getCurrentShard(EthereumAccount account) {
        return account.getShardNumber();
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
