package jabs;

import jabs.log.*;
import jabs.scenario.AbstractScenario;
import jabs.scenario.ShardedPBFTScenario;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

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

        /* 2,4,6,8,16,32,64,128,256 shards with 10 per shard testing, client led */
        /* transaction committed logger, as well as creation logger, account locks and unlocks logger, coordination message logger, aborted logger */
        int[] clients = {50, 50, 100, 100, 200, 400, 800, 1500, 3000, 5000};

        // print what time it is
        Date start = new Date();
        System.out.println("Current time: " + start.toString());

        // SHARD LED SIMULATIONS
/* 
        int randomNum = 1;
        for(randomNum = 1; randomNum < 2; randomNum++){
            System.out.println("randomNum: " + randomNum);
            for(int i = 128, j = 6; i <= 128; i = i * 2, j++) {
                scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum, i, 10, clients[j], 1, 60, "shard");
                // System.out.println("output/tenNodesSimulations/shardled/10000accounts/Shardled-CommittedLogger-" + i + "s10n" + clients[j] + "c.csv");
                scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.2/seed" + randomNum + "/Shardled-CommittedLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new TransactionCreationLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.2/seed" + randomNum + "/Shardled-CreationLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new TransactionAbortedLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.2/seed" + randomNum + "/Shardled-AbortedLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.2/seed" + randomNum + "/ShardledAccountLockingLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.2/seed" + randomNum + "/ShardledAccountUnlockingLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.2/seed" + randomNum + "/ShardledCoordinationMessageLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.run();
            }
        }
        */

        // CLIENT LED SIMULATIONS WITH MIGRATION

        int randomNum = 1;
        for(randomNum = 1; randomNum < 2; randomNum++){
            System.out.println("randomNum: " + randomNum);
            for(int i = 4, j = 6; i <= 4; i = i * 2, j++) {
                scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum,4, 5, clients[j], 1,180, "client", false);
                // System.out.println("output/tenNodesSimulations/shardled/10000accounts/Shardled-CommittedLogger-" + i + "s10n" + clients[j] + "c.csv");
                scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/Clientled-CommittedLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new TransactionCreationLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/Clientled-CreationLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new TransactionAbortedLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/Clientled-AbortedLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/ClientledAccountLockingLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/ClientledAccountUnlockingLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/ClientledCoordinationMessageLogger-" + i + "s10n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new WithoutMigrationLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/Clientled-WithoutMigrations-" + i + "s10n" + clients[j] + "c.csv"))); // Corrected file path
                scenario.run();
            }
        }

          // CLIENT LED SIMULATIONS

          randomNum = 1;
          for(randomNum = 1; randomNum < 2; randomNum++){
              System.out.println("randomNum: " + randomNum);
              for(int i =4, j = 6; i <= 4; i = i * 2, j++) {
                  scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum, 4, 5, clients[j], 1, 180, "client", true);
                  // System.out.println("output/tenNodesSimulations/shardled/10000accounts/Shardled-CommittedLogger-" + i + "s10n" + clients[j] + "c.csv");
                  scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/Clientled-CommittedLogger-" + i + "s10n" + clients[j] + "c.csv")));
                  scenario.AddNewLogger(new TransactionCreationLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/Clientled-CreationLogger-" + i + "s10n" + clients[j] + "c.csv")));
                  scenario.AddNewLogger(new TransactionAbortedLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/Clientled-AbortedLogger-" + i + "s10n" + clients[j] + "c.csv")));
                  scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/ClientledAccountLockingLogger-" + i + "s10n" + clients[j] + "c.csv")));
                  scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/ClientledAccountUnlockingLogger-" + i + "s10n" + clients[j] + "c.csv")));
                  scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/ClientledCoordinationMessageLogger-" + i + "s10n" + clients[j] + "c.csv")));
                  scenario.AddNewLogger(new MigrationLogger(Paths.get("output/tenNodesSimulations/clientled/exponent1.2/seed" + randomNum + "/Clientled-Migrations-" + i + "s10n" + clients[j] + "c.csv")));
                  scenario.run();
              }
          }

        // for one run
        // int i = 8;
        // int j = 100;
        // scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum, i, 10, j, 1, 60, "shard");
        // System.out.println("output/tenNodesSimulations/shardled/10000accounts/Shardled-CommittedLogger-" + i + "s10n" + j + "c.csv");
        // scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.5/Shardled-CommittedLogger-" + i + "s10n" + j + "c.csv")));
        // scenario.AddNewLogger(new TransactionCreationLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.5/Shardled-CreationLogger-" + i + "s10n" + j + "c.csv")));
        // scenario.AddNewLogger(new TransactionAbortedLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.5/Shardled-AbortedLogger-" + i + "s10n" + j + "c.csv")));
        // scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.5/ShardledAccountLockingLogger-" + i + "s10n" + j + "c.csv")));
        // scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.5/ShardledAccountUnlockingLogger-" + i + "s10n" + j + "c.csv")));
        // scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.5/ShardledCoordinationMessageLogger-" + i + "s10n" + j + "c.csv")));
        // scenario.run();

        // print what time it is
        Date now = new Date();
        System.out.println("Time finished: " + now.toString());
        // print how long it took in hours and minutes
        long diff = now.getTime() - start.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        System.out.println("Time taken: " + diffHours + " hours, " + diffMinutes + " minutes, " + diffSeconds + " seconds.");

    }
}
