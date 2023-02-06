package jabs;

import jabs.log.*;
import jabs.scenario.AbstractScenario;
import jabs.scenario.ShardedPBFTScenario;

import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 */
public class Main {
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        AbstractScenario scenario;
        /* SHARDED TESTING */
        // generate a random number between 1 and 1000000
        // int randomNum = 1 + (int)(Math.random() * 1000000);
        int randomNum = 1;
        scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum, 1, 20, 1, 1, 3600);
        scenario.AddNewLogger(new VoteLogger(Paths.get("output/sharded-pbft-block-delivery-log.csv")));
        scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/sharded-pbft-coordination-messages-log.csv")));
        scenario.AddNewLogger(new ShardedBlockConfirmationLogger(Paths.get("output/sharded-pbft-block-log.csv")));
        scenario.run();
    }
}