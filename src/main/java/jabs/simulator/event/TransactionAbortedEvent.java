package jabs.simulator.event;

import jabs.ledgerdata.ethereum.EthereumTx;

public class TransactionAbortedEvent extends AbstractLogEvent {

    private EthereumTx tx;

    public TransactionAbortedEvent(double time, EthereumTx tx) {
        super(time);
        this.tx = tx;
    }

    public EthereumTx getTx() {
        return this.tx;
    }

}
