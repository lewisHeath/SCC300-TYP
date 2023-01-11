package jabs.consensus.algorithm;

import java.util.ArrayList;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.pbft.PBFTBlock;
import jabs.network.message.CoordinationMessage;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.pbft.PBFTShardedNode;

public class ShardLedCrossShardConsensus implements CrossShardConsensus{

    PBFTShardedNode node;
    private ArrayList<EthereumAccount> lockedAccounts = new ArrayList<EthereumAccount>();

    public ShardLedCrossShardConsensus(PBFTShardedNode node){
        this.node = node;
    }

    public void processCoordinationMessage(CoordinationMessage message, Node from) {
        // TODO Auto-generated method stub
        
    }

    public void processConfirmedBlock(PBFTBlock block) {
        // TODO Auto-generated method stub
        
    }

    public Boolean areAccountsLocked(ArrayList<EthereumAccount> accounts) {
        for (EthereumAccount account : accounts) {
            if (lockedAccounts.contains(account)) {
                return true;
            }
        }
        return false;
    }

    public Boolean isLocked(EthereumAccount account) {
        return lockedAccounts.contains(account);
    }

    public Boolean areAllAccountsInThisShard(ArrayList<EthereumAccount> accounts) {
        // get the accounts that are mapped to this shard
        ArrayList<EthereumAccount> shardAccounts = node.getShardAccounts();
        // check which accounts are in this shard
        int i = 0;
        for (EthereumAccount account : accounts) {
            if (shardAccounts.contains(account)) {
                i++;
            }
        }
        return i == accounts.size();
    }
}
