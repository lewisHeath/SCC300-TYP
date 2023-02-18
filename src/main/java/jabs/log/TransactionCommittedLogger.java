package jabs.log;

import java.io.IOException;
import java.nio.file.Path;

import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.simulator.event.Event;
import jabs.simulator.event.TransactionCommittedEvent;

public class TransactionCommittedLogger extends AbstractCSVLogger {

    public TransactionCommittedLogger(Path path) throws IOException {
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
        return event instanceof TransactionCommittedEvent;
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        return false;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[]{"Time", "Tx", "TxCreationTime"};
    }

    @Override
    protected String[] csvEventOutput(Event event) {

        EthereumTx tx = ((TransactionCommittedEvent)event).getTx();

        // Time, Tx, Tx creation time
        return new String[] {
            Double.toString(this.scenario.getSimulator().getSimulationTime()),
            Integer.toString(tx.hashCode()),
            Double.toString(tx.getCreationTime())
        };
    }
    
}
