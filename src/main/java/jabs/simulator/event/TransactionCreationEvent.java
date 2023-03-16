package jabs.simulator.event;

import jabs.ledgerdata.ethereum.EthereumTx;

public class TransactionCreationEvent extends AbstractLogEvent {

    private EthereumTx tx;

    public TransactionCreationEvent(double time, EthereumTx tx) {
        super(time);
        this.tx = tx;
    }

    public EthereumTx getTx() {
        return this.tx;
    }

}