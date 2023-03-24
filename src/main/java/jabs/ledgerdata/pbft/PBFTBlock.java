package jabs.ledgerdata.pbft;

import java.util.ArrayList;
import java.util.HashSet;

import jabs.ledgerdata.SingleParentBlock;
import jabs.ledgerdata.Sharding.Recipt;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.node.nodes.Node;

public class PBFTBlock extends SingleParentBlock<PBFTBlock> {
    public static final int PBFT_BLOCK_HASH_SIZE = 32;

    protected ArrayList<EthereumTx> transactions;
    protected ArrayList<Recipt> recipts;

    public PBFTBlock(int size, int height, double creationTime, Node creator, PBFTBlock parent) {
        super(size, height, creationTime, creator, parent, PBFT_BLOCK_HASH_SIZE);
    }

    public void addTransaction(EthereumTx tx) {
        this.transactions.add(tx);
    }

    public void setTransactions(ArrayList<EthereumTx> transactions) {
        this.transactions = transactions;
        // System.out.println("Transactions set, size: " + transactions.size());
    }

    public ArrayList<EthereumTx> getTransactions() {
        return this.transactions;
    }

    public void addRecipt(Recipt recipt) {
        this.recipts.add(recipt);
    }

    public void setRecipts(ArrayList<Recipt> recipts) {
        this.recipts = recipts;
    }

    public ArrayList<Recipt> getRecipts() {
        return this.recipts;
    }
}
