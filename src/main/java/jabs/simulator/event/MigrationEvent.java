package jabs.simulator.event;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.network.networks.sharded.PBFTShardedNetwork;

public class MigrationEvent extends AbstractLogEvent {

    private EthereumAccount account;
    private int currentShard;
    private int newShard;
    private double MigrationTime;
    private int ThresholdValue;
    private int CrossShard;
    private int IntraShard;
    private int committedTransactions;
    private PBFTShardedNetwork network;
    private int MigrationCount;
    private String PolicyType;
    private int MigrationsCommitted;

    public MigrationEvent(double time, EthereumAccount account, int currentShard, int newShard, int ThresholdValue, int CrossShard, int IntraShard, int committedTransactions, int MigrationCount, int MigrationsCommitted) {
        super(time);
        this.MigrationTime = time;
        this.CrossShard = CrossShard;
        this.IntraShard = IntraShard;
        this.ThresholdValue = ThresholdValue;
        this.account = account;
        this.MigrationCount = MigrationCount;
        this.committedTransactions = committedTransactions;
        this.currentShard = currentShard;
        this.newShard = newShard;
        this.MigrationsCommitted = MigrationsCommitted;
    }

    public EthereumAccount getAccount() {
        return account;
    }

    public int getCurrentShard() {
        return currentShard;
    }

    public int getNewShard() {
        return newShard;
    }
    public double getMigrationTime() {
        return MigrationTime;
    }
    public int getThresholdValue() {
        return ThresholdValue;
    }

    public int getCrossShardValue()
    {
        return CrossShard;
    }
    public int getIntraShardValue()
    {
        return IntraShard;
    }
    public int committedTransactions()
    {
        return committedTransactions;
    }
    public int getMigrationCount()
    {
        return MigrationCount;
    }
    public double getTime()
    {
        return MigrationTime;
    }

    public int getMigrationsCommitted()
    {
        return MigrationsCommitted;
    }

    



}