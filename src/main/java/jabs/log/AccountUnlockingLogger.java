package jabs.log;

import java.io.IOException;
import java.nio.file.Path;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.simulator.event.AccountUnlockingEvent;
import jabs.simulator.event.Event;

public class AccountUnlockingLogger extends AbstractCSVLogger {

    public AccountUnlockingLogger(Path path) throws IOException {
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
        return event instanceof AccountUnlockingEvent;
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
        AccountUnlockingEvent lockingEvent = (AccountUnlockingEvent) event;
        EthereumAccount account = lockingEvent.getAccount();

        // Time, Account
        return new String[] {
                Double.toString(this.scenario.getSimulator().getSimulationTime()),
                account.toString()
        };
    }

}
