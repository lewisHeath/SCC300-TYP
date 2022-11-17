package jabs;

import jabs.ledgerdata.bitcoin.BitcoinBlockWithoutTx;
import jabs.log.*;
import jabs.scenario.AbstractScenario;
import jabs.scenario.BitcoinGlobalNetworkScenario;
import jabs.scenario.NormalEthereumNetworkScenario;
import jabs.scenario.PBFTLANScenario;
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

        // Simulate one day in the life of Bitcoin network
        // Nakamoto protocol with block every 600 seconds
        // Around 8000 nodes with 30 miners
        /*
        scenario = new BitcoinGlobalNetworkScenario("One day in the life of Bitcoin", 1,
                86400, 600, 6);
        scenario.AddNewLogger(new BlockConfirmationLogger(Paths.get("output/bitcoin-confirmations-log.csv")));
        scenario.AddNewLogger(new BlockPropagationDelayLogger(
                Paths.get("output/bitcoin-50-propagation-delay-log.csv"),0.5));
        scenario.AddNewLogger(new BlockPropagationDelayLogger(
                Paths.get("output/bitcoin-90-propagation-delay-log.csv"),0.9));
        scenario.AddNewLogger(new BlockchainReorgLogger<BitcoinBlockWithoutTx>(
                Paths.get("output/bitcoin-reorgs-log.csv")));
        scenario.run();

        // Simulate 1 hour in the life of Ethereum network
        // Ghost protocol with blocks every 14 seconds on average
        // Around 6000 nodes with 37 miners
        scenario = new NormalEthereumNetworkScenario("One hour in the life of Ethereum", 1,
                3600, 13.3);
        scenario.AddNewLogger(new BlockPropagationDelayLogger(
                Paths.get("output/ethereum-50-propagation-delay-log.csv"), 0.5));
        scenario.AddNewLogger(new BlockPropagationDelayLogger(
                Paths.get("output/ethereum-90-propagation-delay-log.csv"), 0.9));
        scenario.AddNewLogger(new FinalUncleBlocksLogger(
                Paths.get("output/ethereum-uncle-rate.csv")));
        scenario.run();
        */

        // Simulate PBFT Lan network of 40 nodes for 1 hour
        // scenario = new PBFTLANScenario("One hour of a PBFT lan Network", 1,
        //         40, 3600);
        // scenario.AddNewLogger(new BlockGenerationLogger(Paths.get("output/pbft-simulation-log.csv")));
        // // scenario.AddNewLogger(new PBFTCSVLogger(Paths.get("output/pbft-CSV-log.csv")));
        // scenario.AddNewLogger(new AllPassedMessagesLogger(Paths.get("output/pbft-messages-log-log.csv")));
        // scenario.AddNewLogger(new BlockDeliveryLogger(Paths.get("output/pbft-delivery-log.csv")));
        // scenario.run();

        /* SHARDED TESTING */
        scenario = new ShardedPBFTScenario("sharded PBFT scenario (5 shards, 10 nodes per shard) ", 1, 5, 10, 3600);
        // scenario.AddNewLogger(new AllPassedMessagesLogger(Paths.get("output/pbft-sharded-messages-log.csv")));
        scenario.AddNewLogger(new VoteLogger(Paths.get("output/sharded-pbft-block-delivery-log.csv")));
        scenario.run();

        // scenario = new ShardedPBFTScenario("sharded PBFT scenario (10 shards, 10 nodes per shard)", 1, 10, 10, 3600);
        // scenario.run();
    }
}