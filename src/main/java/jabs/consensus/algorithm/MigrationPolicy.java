package jabs.consensus.algorithm;

import java.util.Map;

import jabs.ledgerdata.ethereum.EthereumAccount;

// MigrationPolicy interface
public interface MigrationPolicy {
    
    void migrateIfNecessary(EthereumAccount receiver, EthereumAccount sender, Map<String, Integer> crossShardTransactionCount, boolean activate);
    void migrateAccount(int receiverShard, EthereumAccount currentAccount);
}