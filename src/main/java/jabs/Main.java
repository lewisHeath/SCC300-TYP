package jabs;

import jabs.log.*;
import jabs.scenario.AbstractScenario;
import jabs.scenario.ShardedPBFTScenario;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;


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

        /* 2,4,6,680,52,8,122,256 shards with 6 per shard testing, client led */
        /* transaction committed logger, as well as creation logger, account locks and unlocks logger, coordination message logger, aborted logger */
        int[] clients = {50, 50, 80, 80, 300, 400, 200, 1500, 5000, 5000};

        // print what time it is
        Date start = new Date();
        System.out.println("Current time: " + start.toString());

        // SHARD LED SIMULATIONS
/* 
        int randomNum = 1;
        for(randomNum = 1; randomNum < 2; randomNum++){
            System.out.println("randomNum: " + randomNum);
            for(int i = 122, j = 4; i <= 122; i = i * 2, j++) {
                scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum, i,5, clients[j], 1, 80, "shard");
                // System.out.println("output/tenNodesSimulations/shardled/8000accounts/Shardled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv");
                scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.4/seed" + randomNum + "/Shardled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new TransactionCreationLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.4/seed" + randomNum + "/Shardled-CreationLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new TransactionAbortedLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.4/seed" + randomNum + "/Shardled-AbortedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.4/seed" + randomNum + "/ShardledAccountLockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.4/seed" + randomNum + "/ShardledAccountUnlockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/shardled/exponent1.4/seed" + randomNum + "/ShardledCoordinationMessageLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.run();
            }
        }
        */
  /* 
        // CLIENT LED SIMULATIONS WITHout MIGRATION

        int randomNum = 1;
        for(randomNum = 1; randomNum < 2; randomNum++){
            System.out.println("randomNum: " + randomNum);
            for(int i =32, j = 4; i <=32; i = i * 2, j++) {
                scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum,32, 10, clients[j], 1,60, "client", true, false, false , true);
                // System.out.println("output/tenNodesSimulations/shardled/8000accounts/Shardled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv");
                scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledCoordinationMessageLogger-" + i + "s6n" + clients[j] + "c.csv")));
             //   scenario.AddNewLogger(new MigrationLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Migrations-" + i + "s6n" + clients[j] + "c.csv")));
              //  scenario.AddNewLogger(new ShardloadLog(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Shardload-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.run();
            }
        }
        
*/  
            
            

      
      

      // CLIENT LED SIMULATIONS with datastr    main_with_ShardAssignment': 'output/tenNodesSimulations/clientled/MainShard/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-6s6n300c.csv', 'main_without_ShardAssignment': 'output/tenNodesSimulations/clientled/MainShard/WithoutConsensus//DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-6s6n300c.csv', 'threshold_with_ShardAssignment': 'output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-6s6n300c.csv', 'threshold_without_ShardAssignment': 'output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus//DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-6s6n300c.csv', 'new_accounts_with_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-6s6n300c.csv', 'new_accounts_without_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus//DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-6s6n300c.csv'
 
          int randomNum = 1;
          for(randomNum = 1; randomNum < 2; randomNum++){
              System.out.println("randomNum: " + randomNum);
              for(int i =32, j = 4; i <=32; i = i * 2, j++) {
                scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum,32, 10, clients[j], 1,60, "client", false, true , false , true);
                // System.out.println("output/tenNodesSimulations/shardled/8000accounts/Shardled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv");
                 //scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/clientled/MainShard/WithConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledCoordinationMessageLogger-" + i + "s6n" + clients[j] + "c.csv")));
                 scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/clientled/MainShard/WithConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv")));
              //  scenario.AddNewLogger(new MigrationLogger(Paths.get("output/tenNodesSimulations/clientled/MainShard/WithConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Migrations-" + i + "s6n" + clients[j] + "c.csv")));
                //scenario.AddNewLogger(new ShardloadLog(Paths.get("output/tenNodesSimulations/clientled/MainShard/WithConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Shardload-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.run();
              }
          }
 /* 
          randomNum = 1;
          for(randomNum = 1; randomNum < 2; randomNum++){
              System.out.println("randomNum: " + randomNum);
              for(int i =32, j = 4; i <=32; i = i * 2, j++) {
                scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum,32, 10, clients[j], 1,60, "client", false, false , false , false);
                // System.out.println("output/tenNodesSimulations/shardled/8000accounts/Shardled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv");
                scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/clientled/WithConsensus/regular/exponent1.4/seed" + randomNum + "/Clientled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv")));
               // scenario.AddNewLogger(new TransactionAbortedLogger(Paths.get("output/tenNodesSimulations/clientled/WithoutConsensus/regular/exponent1.4/seed" + randomNum + "/Clientled-AbortedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                //scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/tenNodesSimulations/clientled/WithoutConsensus/regular/exponent1.4/seed" + randomNum + "/ClientledAccountLockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
              //  scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/tenNodesSimulations/clientled/WithoutConsensus/regular/exponent1.4/seed" + randomNum + "/ClientledAccountUnlockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/clientled/WithConsensus/regular/exponent1.4/seed" + randomNum + "/ClientledCoordinationMessageLogger-" + i + "s6n" + clients[j] + "c.csv")));
               // scenario.AddNewLogger(new MigrationLogger(Paths.get("output/tenNodesSimulations/clientled/WithConsensus/regular/exponent1.4/seed" + randomNum + "/Clientled-Migrations-" + i + "s6n" + clients[j] + "c.csv")));
                //scenario.AddNewLogger(new ShardloadLog(Paths.get("output/tenNodesSimulations/clientled/WithConsensus/regular/exponent1.4/seed" + randomNum + "/Clientled-Shardload-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.run();
              }
          }

  /*    
            // main shard policy
         randomNum = 1;
          for(randomNum = 1; randomNum < 2; randomNum++){
              System.out.println("randomNum: " + randomNum); // DS NO CONSENSUS WITH NEW ACCOUNTS
              for(int i = 6, j = 4; i <= 6; i = i * 2, j++) {
                scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum, 6, 10, clients[j], 1,60, "client", true, false , false ,false);
                // System.out.println("output/tenNodesSimulations/shardled/8000accounts/Shardled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv");
                scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new TransactionCreationLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-CreationLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new TransactionAbortedLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-AbortedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledAccountLockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledAccountUnlockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledCoordinationMessageLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new MigrationLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Migrations-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new ShardloadLog(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Shardload-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.run();
              }
          }
        

            // all of the policies combined
          randomNum = 1;
          for(randomNum = 1; randomNum < 2; randomNum++){ // DS NO CONSENSUS WITH NEW ACCOUNTS
              System.out.println("randomNum: " + randomNum); 
              for(int i = 6, j = 4; i <=6; i = i * 2, j++) {
                 scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum, 6, 10, clients[j], 1,100, "client",true, false ,false , true);
                // System.out.println("output/tenNodesSimulations/shardled/8000accounts/Shardled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv");
                scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new TransactionCreationLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-CreationLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new TransactionAbortedLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-AbortedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledAccountLockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledAccountUnlockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledCoordinationMessageLogger-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new MigrationLogger(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Migrations-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.AddNewLogger(new ShardloadLog(Paths.get("output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Shardload-" + i + "s6n" + clients[j] + "c.csv")));
                scenario.run();
              }

              
          }

 
            // all of the policies combined
            randomNum = 1;
            for(randomNum = 1; randomNum < 2; randomNum++){ // MAIN SHARD NO CONSENSUS WITH NEW ACCOUNTS
                System.out.println("randomNum: " + randomNum); 
                for(int i = 6, j = 4; i <=6; i = i * 2, j++) {
                    scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum, 6, 10, clients[j], 1,100, "client", false, false, true , false);
                    // System.out.println("output/tenNodesSimulations/shardled/8000accounts/Shardled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv");
                    scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new TransactionCreationLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-CreationLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new TransactionAbortedLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-AbortedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledAccountLockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledAccountUnlockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledCoordinationMessageLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new MigrationLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Migrations-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new ShardloadLog(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Shardload-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.run();
                }
            }


             // all of the policies combined
             randomNum = 1;
             for(randomNum = 1; randomNum < 2; randomNum++){ // MAIN SHARD NO CONSENSUS /DynamicShardAssignment
                 System.out.println("randomNum: " + randomNum); 
                 for(int i = 6, j = 4; i <=6; i = i * 2, j++) {
                    scenario = new ShardedPBFTScenario("sharded PBFT scenario", randomNum, 6, 10, clients[j], 1,100, "client", false, false, true , true);
                    // System.out.println("output/tenNodesSimulations/shardled/8000accounts/Shardled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv");
                    scenario.AddNewLogger(new TransactionCommittedLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-CommittedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new TransactionCreationLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-CreationLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new TransactionAbortedLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-AbortedLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new AccountLockingLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledAccountLockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new AccountUnlockingLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledAccountUnlockingLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new CoordinationMessagesLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/ClientledCoordinationMessageLogger-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new MigrationLogger(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Migrations-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.AddNewLogger(new ShardloadLog(Paths.get("output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed" + randomNum + "/Clientled-Shardload-" + i + "s6n" + clients[j] + "c.csv")));
                    scenario.run();
                    }
                }

                */
        // print what time it is
        Date now = new Date();
        System.out.println("Time finished: " + now.toString());
        // print how long it took in hours and minutes
        long diff = now.getTime() - start.getTime();
        long diffSeconds = diff / 800 % 80;
        long diffMinutes = diff / (80 * 800) % 80;
        long diffHours = diff / (80 * 80 * 800) % 24;
        System.out.println("Time taken: " + diffHours + " hours, " + diffMinutes + " minutes, " + diffSeconds + " seconds.");

    }
}
