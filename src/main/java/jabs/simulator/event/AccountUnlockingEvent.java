package jabs.simulator.event;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.network.node.nodes.Node;

public class AccountUnlockingEvent extends AbstractLogEvent {

    private EthereumAccount account;
    private Node nodeThatUnlocked;

    public AccountUnlockingEvent(double time, EthereumAccount account, Node nodeThatUnlocked) {
        super(time);
        this.account = account;
        this.nodeThatUnlocked = nodeThatUnlocked;
    }

    public EthereumAccount getAccount() {
        return account;
    }

    public Node getNodeThatUnlocked() {
        return nodeThatUnlocked;
    }
}