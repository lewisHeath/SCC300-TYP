package jabs.simulator.event;

import jabs.ledgerdata.ethereum.EthereumAccount;

public class AccountLockingEvent extends AbstractLogEvent {

    private EthereumAccount account;

    public AccountLockingEvent(double time, EthereumAccount account) {
        super(time);
        this.account = account;
    }

    public EthereumAccount getAccount() {
        return account;
    }
}