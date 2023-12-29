package jabs.log;
import java.io.IOException;
import java.nio.file.Path;
import jabs.ledgerdata.Block;
import jabs.network.node.nodes.Node;
import jabs.simulator.event.Event;
import jabs.simulator.event.ShardedBlockConfirmationEvent;

public class ShardedBlockConfirmationLogger extends BlockConfirmationLogger {

    public ShardedBlockConfirmationLogger(Path path) throws IOException {
        super(path);
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[] { "Time", "NodeID", "BlockHeight", "BlockHashCode", "BlockSize", "BlockCreationTime", "BlockCreator", "Shard", "TransactionsInBlock" };
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        Node node = ((ShardedBlockConfirmationEvent) event).getNode();
        Block block = ((ShardedBlockConfirmationEvent) event).getBlock();

        return new String[] {
                Double.toString(this.scenario.getSimulator().getSimulationTime()),
                Integer.toString(node.nodeID),
                Integer.toString(block.getHeight()),
                Integer.toString(block.hashCode()),
                Integer.toString(block.getSize()),
                Double.toString(block.getCreationTime()),
                Integer.toString(block.getCreator().nodeID),
                Integer.toString(((ShardedBlockConfirmationEvent) event).getShard()),
                Integer.toString(((ShardedBlockConfirmationEvent) event).getTransactionsInBlock())
        };
    }
    
}
