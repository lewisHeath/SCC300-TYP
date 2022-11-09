package jabs.log;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

import jabs.ledgerdata.Block;
import jabs.ledgerdata.Vote;
import jabs.ledgerdata.pbft.PBFTBlockVote;
import jabs.network.message.Message;
import jabs.network.message.Packet;
import jabs.network.message.VoteMessage;
import jabs.simulator.event.Event;
import jabs.simulator.event.PacketDeliveryEvent;

public class VoteLogger extends BlockDeliveryLogger {

    /**
     * creates an abstract CSV logger
     * 
     * @param writer this is output CSV of the logger
     */
    public VoteLogger(Writer writer) {
        super(writer);
    }

    /**
     * creates an abstract CSV logger
     * 
     * @param path this is output path of CSV file
     */
    public VoteLogger(Path path) throws IOException {
        super(path);
    }

    @Override
    protected boolean csvOutputConditionAfterEvent(Event event) {
        if (event instanceof PacketDeliveryEvent) {
            Message message = ((PacketDeliveryEvent) event).packet.getMessage();
            if (message instanceof VoteMessage) {
                return (((VoteMessage) message).getVote() instanceof Vote);
            }
        }
        return false;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[] { "Time", "BlockHeight", "BlockCreator", "BlockSize", "BlockHashCode",
                "Receiver", "Sender" };
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        Packet packet = ((PacketDeliveryEvent) event).packet;
        Block block = ((PBFTBlockVote)((VoteMessage) packet.getMessage()).getVote()).getBlock();

        return new String[] {
                Double.toString(this.scenario.getSimulator().getSimulationTime()),
                Integer.toString(block.getHeight()),
                Integer.toString(block.getCreator().nodeID),
                Integer.toString(block.getSize()),
                Integer.toString(block.hashCode()),
                Integer.toString(packet.getTo().nodeID),
                Integer.toString(packet.getFrom().nodeID)
        };
    }
}
