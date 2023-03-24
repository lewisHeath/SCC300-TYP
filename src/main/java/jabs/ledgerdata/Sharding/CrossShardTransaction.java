package jabs.ledgerdata.Sharding;

import jabs.ledgerdata.Data;
import jabs.ledgerdata.ethereum.EthereumAccount;

public class CrossShardTransaction extends Data{

    private EthereumAccount sender;
    private EthereumAccount receiver;
    

    public CrossShardTransaction(int size) {
        super(size, 0);
    }
}
