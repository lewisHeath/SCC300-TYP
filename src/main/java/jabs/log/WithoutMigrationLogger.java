package jabs.log;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.simulator.event.Event;
import jabs.simulator.event.TransactionCreationEvent;

public class WithoutMigrationLogger extends AbstractCSVLogger {

    public WithoutMigrationLogger(Path path) throws IOException {
        super(path);
    }

    @Override
    protected String csvStartingComment() {
        return "Without Migration Logging started";
    }

    @Override
    protected boolean csvOutputConditionBeforeEvent(Event event) {
        return false;
    }

    @Override
    protected boolean csvOutputConditionAfterEvent(Event event) {
        return event instanceof TransactionCreationEvent; // Use TransactionCreationEvent here
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        return false;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[] { "CrossShard Transactions", "IntraShard Transactions", "Simulation Time" };
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        TransactionCreationEvent creationEvent = (TransactionCreationEvent) event; // Cast to TransactionCreationEvent
        
        EthereumTx tx = creationEvent.getTx();
       
        // Extract cross-shard transactions, intra-shard transactions, and simulation time from the event
        return new String[] {
            String.valueOf(creationEvent.getCrossShard()), // Convert boolean to integer representation
            String.valueOf(creationEvent.getIntraShard()), // Invert the boolean value for intra-shard transactions
            String.valueOf(creationEvent.getTime()),
        };
    }
}