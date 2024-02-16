package jabs.consensus.algorithm;

import java.util.Map;

import jabs.ledgerdata.ethereum.EthereumAccount;

// MigrationPolicy interface
public interface MigrationPolicy {
    
    void migrateIfNecessary(EthereumAccount account, EthereumAccount receiver, EthereumAccount sender, Map<String, Integer> crossShardTransactionCount);
    void migrateAccount(EthereumAccount accounts, EthereumAccount receiverAccount, EthereumAccount currentAccount);
}