import matplotlib.pyplot as plt
import pandas as pd
import numpy as np

# Assuming your data includes a column 'Time' and 'Shard Load' for simulation time and load, respectively.

file_paths = {
    'Main_Shard': 'output/tenNodesSimulations/clientled/MainShard/WithConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-ShardLoad-32s6n300c.csv',
    'Transaction_Based': 'output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-ShardLoad-32s6n300c.csv',
    'Regular': 'output/tenNodesSimulations/clientled/WithConsensus/regular/exponent1.4/seed1/Clientled-ShardLoad-32s6n300c.csv'
}

plt.figure(figsize=(12, 8))

for policy, path in file_paths.items():
    df = pd.read_csv(path)
    # Assume 'Time' is your simulation time and 'Shard Load' is the load at that time.
    # Here, we're simplifying by averaging the load across all shards at each time point.
    # This might need adjusting based on your actual data structure.
    avg_load_over_time = df.groupby('Time')['Shard Load'].mean().reset_index()

    plt.plot(avg_load_over_time['Time'], avg_load_over_time['Shard Load'], label=policy)

plt.xlabel('Simulation Time')
plt.ylabel('Average Shard Load')
plt.title('Shard Load Over Time by Policy')
plt.legend()
plt.tight_layout()
plt.show()