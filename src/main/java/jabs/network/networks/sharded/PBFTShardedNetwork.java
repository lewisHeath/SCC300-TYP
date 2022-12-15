package jabs.network.networks.sharded;

import java.util.ArrayList;
import java.util.HashMap;

import jabs.consensus.config.ConsensusAlgorithmConfig;
import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.network.networks.Network;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.ShardedClient;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.network.stats.eightysixcountries.EightySixCountries;
import jabs.network.stats.eightysixcountries.GlobalNetworkStats86Countries;
import jabs.simulator.Simulator;
import jabs.simulator.randengine.RandomnessEngine;

public class PBFTShardedNetwork extends Network<Node, EightySixCountries> {

    private int numberOfShards;
    private int nodesPerShard;
    private final ArrayList<ArrayList<PBFTShardedNode>> shards = new ArrayList<ArrayList<PBFTShardedNode>>();
    private ArrayList<ShardedClient> clients = new ArrayList<ShardedClient>();
    // mapping of ethereum accounts to shards
    private HashMap<EthereumAccount, Integer> accountToShard = new HashMap<EthereumAccount, Integer>();
    public int intraShardTransactions = 0;
    public int crossShardTransactions = 0;
    public int clientIntraShardTransactions = 0;
    public int clientCrossShardTransactions = 0;
    public int failures = 0;

    public PBFTShardedNetwork(RandomnessEngine randomnessEngine, int numberOfShards, int nodesPerShard) {
        super(randomnessEngine, new GlobalNetworkStats86Countries(randomnessEngine));
        this.numberOfShards = numberOfShards;
        this.nodesPerShard = nodesPerShard;
        this.accountToShard = new HashMap<EthereumAccount, Integer>();
        this.clients = new ArrayList<ShardedClient>();
        this.generateAccounts(1000);
        // TODO: populate clients
    }

    public PBFTShardedNode createNewPBFTShardedNode(Simulator simulator, int nodeID, int numNodesInShard, int shardNumber) {
        return new PBFTShardedNode(simulator, this, nodeID,
                this.sampleDownloadBandwidth(EightySixCountries.UNITED_KINGDOM),
                this.sampleUploadBandwidth(EightySixCountries.UNITED_KINGDOM),
                numNodesInShard, shardNumber);
    }

    public ShardedClient createNewShardedClient(Simulator simulator, int nodeID)  {
        return new ShardedClient(simulator, this, nodeID,
                this.sampleDownloadBandwidth(EightySixCountries.UNITED_KINGDOM), 
                this.sampleUploadBandwidth(EightySixCountries.UNITED_KINGDOM));
    }

    @Override
    public void populateNetwork(Simulator simulator, int numNodes, ConsensusAlgorithmConfig consensusAlgorithmConfig) {
        populateNetwork(simulator, consensusAlgorithmConfig);
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
        // so now each node is in this network but its neighbors are their shard
        // System.out.println("number of shards: " + shards.size());
        // System.out.println("number of nodes in shards: " + shards.get(0).size());

        // add the clients to the network FIRST TODO!!!!!!!!
        int amountOfBlockchainNodes = this.getAllNodes().size();
        for(int i = amountOfBlockchainNodes; i < 100 + amountOfBlockchainNodes; i++) {
            // create a new client and add it to the clients list
            ShardedClient client = createNewShardedClient(simulator, i);
            this.clients.add(client);
            this.addNode(client);
            System.out.println("client " + i + " created");
        }
        // connect each client to the network
        for (ShardedClient client : this.clients) {
            client.getP2pConnections().connectToNetwork(this);
        }
    }

    /**
     * @param node A PBFT node to add to the network
     */
    @Override
    public void addNode(Node node) {
        this.addNode(node, EightySixCountries.UNITED_KINGDOM);
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
        }
    }

    public void addAccount(EthereumAccount account, int shardNumber) {
        // add the account to the shard
        this.accountToShard.put(account, shardNumber);
    }

    public int getAccountShard(EthereumAccount account) {
        return accountToShard.get(account);
    }

    public EthereumAccount getRandomAccount() {
        // get a random account from the network
        int randomAccountIndex = this.getRandom().nextInt(accountToShard.size());
        return (EthereumAccount) accountToShard.keySet().toArray()[randomAccountIndex];
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
}
