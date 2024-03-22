import pandas as pd
import matplotlib.pyplot as plt

# Load the without migration data
df_without_migration = pd.read_csv('output/tenNodesSimulations/clientled/WithConsensus/regular/exponent1.4/seed1/Clientled-Migrations-32s6n300c.csv')

# Load the migration data
df_DataStructure = pd.read_csv('output/tenNodesSimulations/clientled/DataStructure/WithConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-32s6n300c.csv')

df_MainShard =  pd.read_csv('output/tenNodesSimulations/clientled/MainShard/WithConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-32s6n300c.csv')

data = {
    'DataStructure Migration': {
        'cross_shard': df_DataStructure['CrossShard Transactions'].max(),
        'intra_shard': df_DataStructure['IntraShard Transactions'].max(),
        'migration_count': df_DataStructure['Migration Count'].max()
    },
    'Main Shard Migration': {
        'cross_shard': df_MainShard['CrossShard Transactions'].max(),
        'intra_shard': df_MainShard['IntraShard Transactions'].max(),
        'migration_count': df_MainShard['Migration Count'].max()
    },
    'Without Migration': {
        'cross_shard': df_without_migration['CrossShard Transactions'].max(),
        'intra_shard': df_without_migration['IntraShard Transactions'].max(),
        # Assuming no migration count for "without migration" scenario
        'migration_count': None  
    }
}

labels = list(data.keys())
cross_shard_values = [info['cross_shard'] for info in data.values()]
intra_shard_values = [info['intra_shard'] for info in data.values()]
migration_counts = [info['migration_count'] for info in data.values() if info['migration_count'] is not None]

# Modify how migration_counts is constructed to include a value (e.g., 0 or None) for all scenarios
migration_counts = [info['migration_count'] if info['migration_count'] is not None else 0 for info in data.values()]

fig, ax = plt.subplots()

# Plotting the bars for cross-shard transactions
cross_shard_bars = ax.bar(labels, cross_shard_values, label='CrossShard Transactions', color='lightgreen')

# Adding the intra-shard transactions on top
for i, (label, value) in enumerate(zip(labels, intra_shard_values)):
    ax.bar(labels[i], value, bottom=cross_shard_values[i], label='IntraShard Transactions' if i == 0 else "", color='grey')

# Annotating bars with migration counts (if any, otherwise skip)
for i, bar in enumerate(cross_shard_bars):
    # Check if there is a migration count to display
    if migration_counts[i] > 0:  # Assumes 0 or None means no migration count to display
        height = bar.get_height() + intra_shard_values[i]
        ax.text(bar.get_x() + bar.get_width() / 2., height, f'{migration_counts[i]}', ha='center', va='bottom')

ax.set_ylabel('Number of Transactions')
ax.set_title('Transactions with and without Migrations with consensus: 1000Accounts 1.4exponent 32shards')
ax.legend()

plt.show()