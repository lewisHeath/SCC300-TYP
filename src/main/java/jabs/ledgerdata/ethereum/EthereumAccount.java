package jabs.ledgerdata.ethereum;

import jabs.ledgerdata.BasicData;

public class EthereumAccount extends BasicData {
    private int balance;
    private int shard;
    private int accountNumber;
    private String hash;
    private boolean locked;
    public boolean isAssigned = false;
    private boolean migrated = false;
    public EthereumAccount( int accountNumber) {
        super(100);
       // this.shard = shard;
        this.balance = 0;
        this.accountNumber = accountNumber;
        this.locked = false;
        this.isAssigned = false;
    }

    public int getShardNumber() {
        return shard;
    }

    public void MigrateStatus(boolean migrated)
    {
        this.migrated = migrated;
    }

    public boolean haveMigrated()
    {
        return this.migrated;
    }

    public boolean isAssigned()
    {
        return isAssigned;
    }
    // @isAssigned is assigned to an account or not
    public void Uniassigned(boolean isAssigned)
    { 
        this.isAssigned = isAssigned;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public boolean getLocked() {
        return locked;
    }

    public boolean lock() {
        if (locked) {
            return false;
        } else {
            locked = true;
            return true;
        }
    }

    public boolean unlock() {
        if (locked) {
            locked = false;
            return true;
        } else {
            return false;
        }
    }

    public void SetShard(int shard)
    {
        this.shard = shard;
    }
}