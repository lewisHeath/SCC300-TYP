package jabs.log;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import jabs.simulator.event.Event;
import jabs.simulator.event.MigrationEvent;

public class MigrationLogger extends AbstractCSVLogger {
   

    public MigrationLogger(Writer writer) {
        super(writer);
    }

    public MigrationLogger(Path path) throws IOException {
        super(path);
    }

    @Override
    protected String csvStartingComment() {
        // Return the starting comment for migration logging
        return "Migration Logging started";
    }

 
    @Override
    protected boolean csvOutputConditionAfterEvent(Event event) {
        
        return event instanceof MigrationEvent;
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        
        return true;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[]{"CrossShard Transactions" , "IntraShard Transactions" , "Simulation Time", "Migration Count", "Transactions Committed", "Migrations Committed"};
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        // Extract migration-related information from the event and return as an array
        MigrationEvent migrationEvent = (MigrationEvent) event;
        return new String[]{String.valueOf(migrationEvent.getCrossShardValue()),
                String.valueOf(migrationEvent.getIntraShardValue()), String.valueOf(migrationEvent.getTime()), String.valueOf(migrationEvent.getMigrationCount()), String.valueOf(migrationEvent.committedTransactions()), String.valueOf(migrationEvent.getMigrationsCommitted())};
    }

    @Override
    protected boolean csvOutputConditionBeforeEvent(Event event) {
        return false;
    }


  
    
}
