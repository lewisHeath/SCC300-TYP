package jabs.simulator.event;

import jabs.ledgerdata.ethereum.EthereumTx;

public class TransactionCommittedEvent extends AbstractLogEvent{

    private EthereumTx tx;

    public TransactionCommittedEvent(double time, EthereumTx tx) {
        super(time);
        this.tx = tx;
    }

    public EthereumTx getTx(){
        return this.tx;
    }
    
}
