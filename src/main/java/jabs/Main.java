package jabs;

import jabs.log.*;
import jabs.scenario.AbstractScenario;
import jabs.scenario.ShardedPBFTScenario;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

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
        scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum, 2, 20, 3, 1, 3600);
        // scenario.AddNewLogger(new VoteLogger(Paths.get("output/sharded-pbft-block-delivery-log.csv")));
        // scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/sharded-pbft-coordination-messages-log.csv")));
        // scenario.AddNewLogger(new ShardedBlockConfirmationLogger(Paths.get("output/sharded-pbft-block-log.csv")));
        // scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/transactionCommittedLog-shard-led-8-shards.csv")));
        // scenario.AddNewLogger(new TransactionCreationLogger(Paths.get("output/transactionCreationLog-shard-led-8-shards.csv")));
        // // files 5 - 14 are for client-led
        // scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/accountLockingLog-shardled-8shards.csv")));
        // scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/accountUnlockingLog-shardled-8shards.csv")));
        scenario.run();

        // double scale = 1000;
        // double shape = 200;

        // // create a new instance of the Pareto distribution
        // ParetoDistribution pareto = new ParetoDistribution(scale, shape);

        // // keep trac of how many times each integer is generated
        // int[] counts = new int[1001];

        // for (int i = 0; i < 1000000; i++) {
        //     // generate a random number from the Pareto distribution
        //     double paretoRand = pareto.sample();

        //     // scale the Pareto random variable to the range 0-1000
        //     double cdfRand = pareto.cumulativeProbability(paretoRand) * 1000;

        //     // convert the scaled random variable to an integer
        //     int randInt = (int) Math.round(cdfRand);

        //     // increment the count for the integer
        //     counts[randInt]++;
        // }

        // // print the counts
        // for (int i = 0; i < counts.length; i++) {
        //     System.out.println(i + ": " + counts[i]);
        // }

        // System.out.println("Done");

        // RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        // BetaDistribution betaDistribution = new BetaDistribution(0.5, 5);
        // counts = new int[1001];
        // for (int i = 0; i < 1000000; i++) {
        //     int randomInt = (int) Math.round(betaDistribution.sample() * 1000);
        //     // System.out.println(randomInt);
        //     counts[randomInt]++;
        // }

        // for (int i = 0; i < counts.length; i++) {
        //     System.out.println(i + ": " + counts[i]);
        // }
        
    }
}
