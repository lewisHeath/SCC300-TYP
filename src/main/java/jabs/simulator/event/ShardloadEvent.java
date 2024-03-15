package jabs.simulator.event;

public class ShardloadEvent extends AbstractLogEvent {
    private final int shardId;
    private final int shardLoad;
    private final int migrationTransactions;
    private final double time;

    public ShardloadEvent(double time, int shardId, int shardLoad, int migrationTransactions) {
        super(time);
        this.time = time;
        this.shardId = shardId;
        this.shardLoad = shardLoad;
        this.migrationTransactions = migrationTransactions;
    }

    // Getters
    public int getShardId() { return shardId; }
    public int getShardLoad() { return shardLoad; }
    public int getMigrationTransactions() { return migrationTransactions; }
    public double getTime() { return time; }
}
