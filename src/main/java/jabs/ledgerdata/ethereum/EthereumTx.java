package jabs.ledgerdata.ethereum;

import java.util.ArrayList;

import jabs.ledgerdata.Tx;

public class EthereumTx extends Tx<EthereumTx> {
    final long gas;
    private EthereumAccount sender;
    private EthereumAccount receiver;
    private ArrayList<EthereumAccount> receivers;

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

    public void setReceivers(ArrayList<EthereumAccount> receivers) {
        this.receivers = receivers;
    }

    public ArrayList<EthereumAccount> getReceivers() {
        return receivers;
    }

    public EthereumAccount getReceiver() {
        return receiver;
    }

    public void setReceiver(EthereumAccount receiver) {
        this.receiver = receiver;
    }

    public ArrayList<EthereumAccount> getAllInvolvedAccounts() {
        ArrayList<EthereumAccount> accounts = new ArrayList<EthereumAccount>();
        accounts.add(this.sender);
        accounts.addAll(this.receivers);
        return accounts;
    }

    // @Override
    // public int hashCode() {
    //     // create a hash
    //     // hash is the sender account number and the receiver account number
    //     // this is a very simple hash function
    //     // if the receiver is null treat it as -1
    //     return (int) (this.sender.getAccountNumber() + (this.receiver == null ? -1 : this.receiver.getAccountNumber()));
    // }
}
