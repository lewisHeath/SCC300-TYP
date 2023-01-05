package jabs.network.message;

import jabs.ledgerdata.Data;

public class CoordinationMessage extends DataMessage {

    private String type;

    public CoordinationMessage(Data data, String type) {
        super(data);
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
    
}
