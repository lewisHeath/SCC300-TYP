package jabs.network.message;

import jabs.ledgerdata.Data;
import jabs.network.node.nodes.Node;

public class CoordinationMessage extends Message {

    private String type;
    private Node from;
    private final Data data;

    public CoordinationMessage(Data data, String type) {
        super(data.getSize());
        this.type = type;
        this.data = data;
    }

    public CoordinationMessage(Data data, String type, Node from) {
        super(40);
        this.type = type;
        this.from = from;
        this.data = data;
    }

    public String getType() {
        return this.type;
    }

    public Node getFrom() {
        return this.from;
    }

    public Data getData() {
        return data;
    }
    
}
