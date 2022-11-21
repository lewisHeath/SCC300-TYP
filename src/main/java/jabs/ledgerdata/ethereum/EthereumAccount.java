package jabs.ledgerdata.ethereum;

import jabs.ledgerdata.BasicData;

public class EthereumAccount extends BasicData {
    private int balance;
    private int shard;
    private int accountNumber;
    public EthereumAccount(int shard, int accountNumber) {
        super(100);
        this.shard = shard;
        this.balance = 0;
        this.accountNumber = accountNumber;
    }

    public int getShardNumber() {
        return shard;
    }

    public int getAccountNumber() {
        return accountNumber;
    }
}