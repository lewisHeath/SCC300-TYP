package jabs.log;

import java.io.IOException;
import java.nio.file.Path;

import jabs.simulator.event.Event;
import jabs.simulator.event.MigrationEvent;
import jabs.simulator.event.ShardloadEvent;

public class ShardloadLog extends AbstractCSVLogger {

    public ShardloadLog(Path path) throws IOException {
        super(path);
    }

    @Override
    protected String csvStartingComment() {
        return "Shard Load Logging Started";
    }

    @Override
    protected boolean csvOutputConditionBeforeEvent(Event event) {
        return false;
    }

    @Override
    protected boolean csvOutputConditionAfterEvent(Event event) {
        return event instanceof ShardloadEvent;
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        return true;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[]{"Time", "Shard", "Shard Load", "Migration Transactions"};
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        ShardloadEvent shardLoadEvent = (ShardloadEvent) event;
        return new String[]{
            String.valueOf(shardLoadEvent.getTime()),
            String.valueOf(shardLoadEvent.getShardId()),
            String.valueOf(shardLoadEvent.getShardLoad()),
            String.valueOf(shardLoadEvent.getMigrationTransactions())
        };
    }
    
}
