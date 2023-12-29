package jabs.simulator.event;

import jabs.ledgerdata.ethereum.EthereumAccount;

public class MigrationEvent extends AbstractLogEvent {

    private EthereumAccount account;
    private int currentShard;
    private int newShard;

    public MigrationEvent(double time, EthereumAccount account, int currentShard, int newShard) {
        super(time);
        this.account = account;
        this.currentShard = currentShard;
        this.newShard = newShard;
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
}