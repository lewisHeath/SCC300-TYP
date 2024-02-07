package jabs.simulator.event;

import jabs.ledgerdata.ethereum.EthereumTx;
public class TransactionCreationEvent extends AbstractLogEvent {

    private EthereumTx tx;
    private double time;
    private int intraShard;
    private int crossShard;

    public TransactionCreationEvent(double time, EthereumTx tx, int intraShard, int crossShard) {
        super(time);
        this.time = time;
        this.tx = tx;
        this.crossShard = crossShard;
        this.intraShard = intraShard;
    }

    public EthereumTx getTx() {
        return this.tx;
    }

    public double getTime() {
        return time;
    }

    public int getCrossShard() {
        return crossShard;
    }

    public int getIntraShard() {
        return intraShard;
    }
}