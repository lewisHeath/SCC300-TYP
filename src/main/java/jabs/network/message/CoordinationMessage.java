package jabs.network.message;

import jabs.ledgerdata.Data;
import jabs.network.node.nodes.Node;

public class CoordinationMessage extends DataMessage {

    private String type;
    private Node from;

    public CoordinationMessage(Data data, String type) {
        super(data);
        this.type = type;
    }

    public CoordinationMessage(Data data, String type, Node from) {
        super(data);
        this.type = type;
        this.from = from;
    }

    public String getType() {
        return this.type;
    }

    public Node getFrom() {
        return this.from;
    }
    
}
