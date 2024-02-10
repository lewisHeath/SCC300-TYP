package jabs.log;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import jabs.simulator.event.Event;
import jabs.simulator.event.MigrationEvent;

public class MigrationLogger extends AbstractCSVLogger {
    // Add any additional fields or methods specific to migration logging

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
        // Add conditions to determine if migration should be logged after an event
        // For example, return true if the event is related to migration
        return event instanceof MigrationEvent;
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        // Add conditions to determine if migration should be logged finally per node
        // For example, return true if you want to summarize migration data per node
        return true;
    }

    @Override
    protected String[] csvHeaderOutput() {
        // Define the header for migration logging
        return new String[]{"CrossShard Transactions" , "IntraShard Transactions" , "Simulation Time", "Migration Count"};
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        // Extract migration-related information from the event and return as an array
        MigrationEvent migrationEvent = (MigrationEvent) event;
        return new String[]{String.valueOf(migrationEvent.getCrossShardValue()),
                String.valueOf(migrationEvent.getIntraShardValue()), String.valueOf(migrationEvent.getTime()), String.valueOf(migrationEvent.getMigrationCount())};
    }

    @Override
    protected boolean csvOutputConditionBeforeEvent(Event event) {
        return false;
    }


  
    
}
