package jabs.consensus.algorithm;

import jabs.network.message.CoordinationMessage;
import jabs.network.node.nodes.Node;

import java.util.ArrayList;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.pbft.PBFTBlock;

public interface CrossShardConsensus {

    public void processCoordinationMessage(CoordinationMessage message, Node from);

    public void processConfirmedBlock(PBFTBlock block);

    public Boolean areAccountsLocked(ArrayList<EthereumAccount> accounts);

    public Boolean isLocked(EthereumAccount account);

    public Boolean areAllAccountsInThisShard(ArrayList<EthereumAccount> accounts);
}
