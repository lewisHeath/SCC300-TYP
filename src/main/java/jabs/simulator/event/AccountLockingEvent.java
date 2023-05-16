package jabs.simulator.event;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.network.node.nodes.Node;

public class AccountLockingEvent extends AbstractLogEvent {

    private EthereumAccount account;
    private Node nodeThatLocked;

    public AccountLockingEvent(double time, EthereumAccount account, Node nodeThatLocked) {
        super(time);
        this.account = account;
        this.nodeThatLocked = nodeThatLocked;
    }

    public EthereumAccount getAccount() {
        return account;
    }

    public Node getNodeThatLocked() {
        return nodeThatLocked;
    }
}