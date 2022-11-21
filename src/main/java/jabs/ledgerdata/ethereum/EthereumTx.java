package jabs.ledgerdata.ethereum;

import jabs.ledgerdata.Tx;

public class EthereumTx extends Tx<EthereumTx> {
    final long gas;
    private EthereumAccount sender;
    private EthereumAccount receiver;

    public EthereumTx(int size, long gas) {
        super(size, 0); // Ethereum does not use transaction hashes in network communication
        this.gas = gas;
    }

    public long getGas() {
        return gas; 
    }

    public EthereumAccount getSender() {
        return sender;
    }

    public void setSender(EthereumAccount sender) {
        this.sender = sender;
    }

    public EthereumAccount getReceiver() {
        return receiver;
    }

    public void setReceiver(EthereumAccount receiver) {
        this.receiver = receiver;
    }
}
