package jabs.ledgerdata.ethereum;

import java.util.ArrayList;

import jabs.ledgerdata.Tx;

public class EthereumTx extends Tx<EthereumTx> {
    final long gas;
    private EthereumAccount sender;
    private EthereumAccount receiver;
    private ArrayList<EthereumAccount> receivers;
    private Double creationTime;

    public EthereumTx(int size, long gas) {
        super(size, 0); // Ethereum does not use transaction hashes in network communication
        this.gas = gas;
    }

    public void setCreationTime(Double time){
        this.creationTime = time;
    }

    public Double getCreationTime(){
        return this.creationTime;
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

    public ArrayList<Integer> getAllInvolvedShards() {
        ArrayList<Integer> shards = new ArrayList<Integer>();
        for (EthereumAccount account : this.getAllInvolvedAccounts()) {
            shards.add(account.getShardNumber());
        }
        // remove duplicates
        for (int i = 0; i < shards.size(); i++) {
            for (int j = i + 1; j < shards.size(); j++) {
                if (shards.get(i) == shards.get(j)) {
                    shards.remove(j);
                    j--;
                }
            }
        }
        return shards;
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
