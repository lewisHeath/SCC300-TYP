package jabs.log;

import java.io.IOException;
import java.nio.file.Path;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.simulator.event.AccountLockingEvent;
import jabs.simulator.event.Event;

public class AccountLockingLogger extends AbstractCSVLogger {

    public AccountLockingLogger(Path path) throws IOException {
        super(path);
    }

    @Override
    protected String csvStartingComment() {
        return null;
    }

    @Override
    protected boolean csvOutputConditionBeforeEvent(Event event) {
        return false;
    }

    @Override
    protected boolean csvOutputConditionAfterEvent(Event event) {
        return event instanceof AccountLockingEvent;
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        return false;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[] { "Time", "Account" };
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        AccountLockingEvent lockingEvent = (AccountLockingEvent)event;
        EthereumAccount account = lockingEvent.getAccount();

        // Time, Account
        return new String[] {
                Double.toString(this.scenario.getSimulator().getSimulationTime()),
                account.toString()
        };
    }
    
}
