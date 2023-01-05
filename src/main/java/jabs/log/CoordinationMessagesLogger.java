package jabs.log;

import jabs.simulator.event.Event;
import jabs.simulator.event.PacketDeliveryEvent;
import jabs.network.message.CoordinationMessage;
import jabs.network.message.Message;
import jabs.network.message.Packet;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class CoordinationMessagesLogger extends AbstractCSVLogger {
    /**
     * creates an abstract CSV logger
     * 
     * @param writer this is output CSV of the logger
     */
    public CoordinationMessagesLogger(Writer writer) {
        super(writer);
    }

    /**
     * creates an abstract CSV logger
     * 
     * @param path this is output path of CSV file
     */
    public CoordinationMessagesLogger(Path path) throws IOException {
        super(path);
    }

    @Override
    protected String csvStartingComment() {
        return String.format("Coordination messages logger");
    }

    @Override
    protected boolean csvOutputConditionBeforeEvent(Event event) {
        return false;
    }

    @Override
    protected boolean csvOutputConditionAfterEvent(Event event) {
        if (event instanceof PacketDeliveryEvent) {
            Message message = ((PacketDeliveryEvent) event).packet.getMessage();
            if (message instanceof CoordinationMessage) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        return false;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[] { "Time", "MessageType", "Sender", "Receiver" };
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        Packet packet = ((PacketDeliveryEvent) event).packet;
        CoordinationMessage message = (CoordinationMessage) packet.getMessage();

        return new String[] {
                Double.toString(this.scenario.getSimulator().getSimulationTime()),
                message.getType(),
                Integer.toString(packet.getFrom().nodeID),
                Integer.toString(packet.getTo().nodeID)
        };
    }
}
