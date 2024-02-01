package jabs.network.networks.sharded;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;

import jabs.consensus.algorithm.ClientLedCrossShardConsensus;
import jabs.consensus.algorithm.CrossShardConsensus;
import jabs.consensus.config.ConsensusAlgorithmConfig;
import jabs.ledgerdata.TransactionFactory;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.networks.Network;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.ShardedClient;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.network.stats.NodeGlobalRegionDistribution;
import jabs.network.stats.eightysixcountries.EightySixCountries;
import jabs.network.stats.eightysixcountries.GlobalNetworkStats86Countries;
import jabs.network.stats.eightysixcountries.ethereum.EthereumNodeGlobalNetworkStats86Countries;
import jabs.simulator.Simulator;
import jabs.simulator.randengine.RandomnessEngine;

import jabs.network.stats.lan.LAN100MNetworkStats;
import jabs.network.stats.lan.SingleNodeType;

public class PBFTShardedNetwork extends Network<Node, EightySixCountries> {

    private int numberOfShards;
    private int nodesPerShard;
    private int numberOfClients;
    private int timeBetweenTxs;
    private final ArrayList<ArrayList<PBFTShardedNode>> shards = new ArrayList<ArrayList<PBFTShardedNode>>();
    private ArrayList<ShardedClient> clients = new ArrayList<ShardedClient>();
    // mapping of ethereum accounts to shards
    public HashMap<EthereumAccount, Integer> accountToShard = new HashMap<EthereumAccount, Integer>();
    private HashMap<Integer, ArrayList<EthereumAccount>> shardToAccounts = new HashMap<Integer, ArrayList<EthereumAccount>>();
    public int intraShardTransactions = 0;
    public int crossShardTransactions = 0;
    public int clientIntraShardTransactions = 0;
    public int clientCrossShardTransactions = 0;
    public int failures = 0;
    public int committedTransactions = 0;
    public NodeGlobalRegionDistribution<EightySixCountries> nodeDistribution;
    EightySixCountries region;
    public HashMap<EthereumAccount, Integer> amountOfTimesUsed = new HashMap<EthereumAccount, Integer>();
    private boolean clientLed;
    private ArrayList<Double> cdf;
    public int MigrationCounts = 0;
  
   

    
    
    public PBFTShardedNetwork(RandomnessEngine randomnessEngine, int numberOfShards, int nodesPerShard, int numberOfClients, int timeBetweenTxs, boolean clientLed) {
        super(randomnessEngine, new GlobalNetworkStats86Countries(randomnessEngine));
        this.numberOfShards = numberOfShards;
        this.nodesPerShard = nodesPerShard;
        this.numberOfClients = numberOfClients;
        this.timeBetweenTxs = timeBetweenTxs;
        this.accountToShard = new HashMap<EthereumAccount, Integer>();
        this.clients = new ArrayList<ShardedClient>();
        this.nodeDistribution = new EthereumNodeGlobalNetworkStats86Countries(randomnessEngine);
        this.region = nodeDistribution.sampleRegion();
        // add accounts
        this.generateAccounts(1000000);
        this.clientLed = clientLed;
        this.generateCDF(1.2);
        
    }

    private void generateCDF(double exponent) {
        cdf = new ArrayList<>();
        int numAccounts = accountToShard.size();
        double normalization = 0;
        for (int i = 1; i <= numAccounts; i++) {
            normalization += Math.pow(i, -exponent);
        }
        double cumulative = 0;
        for (int i = 1; i <= numAccounts; i++) {
            cumulative += Math.pow(i, -exponent) / normalization;
            cdf.add(cumulative);
        }
    }

    public PBFTShardedNode createNewPBFTShardedNode(Simulator simulator, int nodeID, int numNodesInShard, int shardNumber) {
        EightySixCountries region = nodeDistribution.sampleRegion();
        // System.out.println("region of node " + nodeID + " is " + region);
        return new PBFTShardedNode(simulator, this, nodeID,this.sampleDownloadBandwidth(region),this.sampleUploadBandwidth(region),numNodesInShard, shardNumber, this.clientLed);
    }

    public ShardedClient createNewShardedClient(Simulator simulator, int nodeID)  {
        EightySixCountries region = nodeDistribution.sampleRegion();
        // System.out.println("region of client " + nodeID + " is " + region);
        return new ShardedClient(simulator, this, nodeID,this.sampleDownloadBandwidth(region), this.sampleUploadBandwidth(region),this.timeBetweenTxs, this.clientLed);
    }

    @Override
    public void populateNetwork(Simulator simulator, int numNodes, ConsensusAlgorithmConfig consensusAlgorithmConfig) {
        populateNetwork(simulator, consensusAlgorithmConfig);
    }

    public void startClientTxGenerationProcesses(){
        for(ShardedClient client : clients){
            client.startTxGenerationProcess();
        }
    }

    @Override
    public void populateNetwork(Simulator simulator, ConsensusAlgorithmConfig pbfConsensusAlgorithmConfig) {
        // add the nodes to each shard
        for (int i = 0; i < numberOfShards; i++){
            // initialise shard
            shards.add(i, new ArrayList<PBFTShardedNode>());
            // add j nodes to shard i
            for (int j = nodesPerShard * i; j < nodesPerShard * (i + 1); j++){
                // add the node to the network
                this.addNode(createNewPBFTShardedNode(simulator, j, nodesPerShard, i));
                // adding that node to the shard
                shards.get(i).add((PBFTShardedNode)this.getNode(j));
            }
        }
        // connect each node to its sharded p2p connections
        for (Node node : this.getAllNodes()) {
            node.getP2pConnections().connectToNetwork(this);
        }
        
        // create the clients
        int amountOfBlockchainNodes = this.getAllNodes().size();
        for(int i = amountOfBlockchainNodes; i < numberOfClients + amountOfBlockchainNodes; i++) {
            // create a new client and add it to the clients list
            ShardedClient client = createNewShardedClient(simulator, i);
            this.clients.add(client);
            this.addNode(client);
            // System.out.println("client " + i + " created");
        }
        // connect each client to the network
        for (ShardedClient client : this.clients) {
            client.getP2pConnections().connectToNetwork(this);
        }

        // tell each node in each shard which accounts are in their shard
        for (int i = 0; i < numberOfShards; i++){
            for (int j = 0; j < shards.get(i).size(); j++){
                shards.get(i).get(j).setShardAccounts(this.shardToAccounts.get(i));
            }
        }

        // tell each nodes cross shard consensus protocol what their ID is
        for (int i = 0; i < numberOfShards; i++) {
            for (int j = 0; j < shards.get(i).size(); j++) {
                shards.get(i).get(j).getCrossShardConsensus().setID(j);
            }
        }
    }

    /**
     * @param node A PBFT node to add to the network
     */
    @Override
    public void addNode(Node node) {
        EightySixCountries region = nodeDistribution.sampleRegion();
        this.addNode(node, region);
    }

    public ArrayList<PBFTShardedNode> getShard(int shardNumber){
        // System.out.println("size of shard(" + shardNumber + ") in getShard() method: " + shards.get(shardNumber).size());
        return this.shards.get(shardNumber);
    }

    public int getIndexOfNode(PBFTShardedNode node, int shardNumber) {
        // loop through the shard and get the index of the node
        for (int i = 0; i < shards.get(shardNumber).size(); i++){
            if (shards.get(shardNumber).get(i).equals(node)){
                return i;
            }
        } return -1;
    }

    private void generateAccounts(int numOfAccounts) {
        // generate lots of random ethereum accounts
        for(int i = 0; i < numOfAccounts; i++){
            // generate random shard number
            int shardNumber = this.getRandom().nextInt(numberOfShards);
            EthereumAccount account = new EthereumAccount(shardNumber, i);
            // add the account to the network
            this.addAccount(account, shardNumber);
            if(this.shardToAccounts.get(shardNumber) == null) {
                this.shardToAccounts.put(shardNumber, new ArrayList<EthereumAccount>());
            }
            this.shardToAccounts.get(shardNumber).add(account);
        }
        for(int i = 0; i < this.shardToAccounts.size(); i++){
            System.out.println("size of shard(" + i + ") in generateAccounts() method: " + this.shardToAccounts.get(i).size());
        }
    }

    public void addAccount(EthereumAccount account, int shardNumber) {
        // add the account to the shard
        this.accountToShard.put(account, shardNumber);
    }

    public int getAccountShard(EthereumAccount account) {
        return accountToShard.get(account);
    }

    public EthereumAccount getRandomAccount(Boolean use_cdf) {
        // get a random account from the network
        // Generate a random number between 0 and 1
        double randomNumber = this.randomnessEngine.nextDouble();

        // Find the corresponding value of the random number in the CDF
        int index = 0;
        for (int i = 0; i < cdf.size(); i++) {
            if (randomNumber <= cdf.get(i)) {
                index = i;
                break;
            }
        }
        if(use_cdf) {
            return (EthereumAccount) accountToShard.keySet().toArray()[index];
        }
        int randomInt = this.randomnessEngine.nextInt(this.accountToShard.size());

        // int randomAccountIndex = this.getRandom().nextInt(accountToShard.size());
        return (EthereumAccount) accountToShard.keySet().toArray()[randomInt];
    }

    public EthereumAccount getRandomAccountFromShard(int shardNumber) {
        // get a random account from the network
        int randomAccountIndex = this.getRandom().nextInt(accountToShard.size());
        EthereumAccount account = (EthereumAccount) accountToShard.keySet().toArray()[randomAccountIndex];
        while (account.getShardNumber() != shardNumber){
            randomAccountIndex = this.getRandom().nextInt(accountToShard.size());
            account = (EthereumAccount) accountToShard.keySet().toArray()[randomAccountIndex];
        }
        return account;
    }

    public PBFTShardedNode getRandomNodeInShard(int shardNumber) {
        // get a random node from the shard
        int randomNodeIndex = this.getRandom().nextInt(shards.get(shardNumber).size());
        return shards.get(shardNumber).get(randomNodeIndex);
    }

    public PBFTShardedNode getNodeInShard(int shardNumber, int node){
        return shards.get(shardNumber).get(node);
    }

    public ArrayList<ShardedClient> getClients() {
        return this.clients;
    }

    public ShardedClient getRandomClient() {
        // get a random client from the network
        int randomClientIndex = this.getRandom().nextInt(100);
        return this.clients.get(randomClientIndex);
    }


    public ArrayList<PBFTShardedNode> getAllNodesFromShard(int shardNumber) {
        return this.shards.get(shardNumber);
    }

    private void generateMempools(int transactionsPerShard) {
        // generate the amount of transactions per shard
        for (int i = 0; i < this.shards.size(); i++) {
            ArrayList<EthereumTx> mempool = new ArrayList<>();
            for (int j = 0; j < transactionsPerShard; j++) {
                // generate random transaction
                EthereumTx tx = TransactionFactory.sampleEthereumTransaction(this.getRandom());
                // get 2 random accounts with the sender in the correct shard
                EthereumAccount sender = getRandomAccountFromShard(i);
                EthereumAccount receiver = getRandomAccount(false);
                tx.setSender(sender);
                tx.setReceiver(receiver);
                // add the transaction to the mempool
                mempool.add(tx);
            }
            // add the mempool to each node in this shard
            ArrayList<PBFTShardedNode> shard = this.shards.get(i);
            for (PBFTShardedNode node : shard) {
                node.setMempool(mempool);
            }
        }
    }

    public int getF() {
        // get the size of one of the shards and return a third of it
        return (int) Math.floor((double) this.shards.get(0).size() / 3);
    }

    public int getNumberOfShards() {
        return this.numberOfShards;
    }

    public int getNodesPerShard() {
        return this.nodesPerShard;
    }

   
}
