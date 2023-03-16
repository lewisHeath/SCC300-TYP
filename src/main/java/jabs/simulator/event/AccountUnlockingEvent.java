package jabs.simulator.event;

import jabs.ledgerdata.ethereum.EthereumAccount;

public class AccountUnlockingEvent extends AbstractLogEvent {

    private EthereumAccount account;

    public AccountUnlockingEvent(double time, EthereumAccount account) {
        super(time);
        this.account = account;
    }

    public EthereumAccount getAccount() {
        return account;
    }
}