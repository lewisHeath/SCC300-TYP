package jabs.ledgerdata.Sharding;

import jabs.ledgerdata.Data;
import jabs.ledgerdata.ethereum.EthereumTx;

public class Recipt extends Data{

    private EthereumTx tx;
    private EthereumTx proof;

    public Recipt(int size, EthereumTx tx, EthereumTx proof) {
        super(size, 0);
        this.tx = tx;
        this.proof = proof;
    }

    public EthereumTx getTx() {
        return tx;
    }

    public void setTx(EthereumTx tx) {
        this.tx = tx;
    }

    public EthereumTx getProof() {
        return proof;
    }

    public void setProof(EthereumTx proof) {
        this.proof = proof;
    }
}
