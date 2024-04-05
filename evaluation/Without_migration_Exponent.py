import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.patches as mpatches

# Example file paths, replace these with your actual file paths
file_paths = {
    '0.6': {'new_accounts_with_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent0.6/seed1/Clientled-Migrations-6s6n300c.csv', 'new_accounts_without_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/regular/exponent0.6/seed1/Clientled-Migrations-6s6n300c.csv','Regular_shard_assignment' : 'output/tenNodesSimulations/clientled/WithoutConsensus/regular/exponent0.6/seed1/Clientled-Migrations-6s6n300c.csv'},
    '0.8': {'new_accounts_with_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent0.8/seed1/Clientled-Migrations-6s6n300c.csv', 'new_accounts_without_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/regular/exponent0.8/seed1/Clientled-Migrations-6s6n300c.csv','Regular_shard_assignment' : 'output/tenNodesSimulations/clientled/WithoutConsensus/regular/exponent0.8/seed1/Clientled-Migrations-6s6n300c.csv'},
    '1.2': {'new_accounts_with_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.2/seed1/Clientled-Migrations-6s6n300c.csv', 'new_accounts_without_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/regular/exponent1.2/seed1/Clientled-Migrations-6s6n300c.csv', 'Regular_shard_assignment': 'output/tenNodesSimulations/clientled/WithoutConsensus/regular/exponent1.2/seed1/Clientled-Migrations-6s6n300c.csv'},      
    '1.4': {'new_accounts_with_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-6s6n300c.csv', 'new_accounts_without_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/regular/exponent1.4/seed1/Clientled-Migrations-6s6n300c.csv','Regular_shard_assignment': 'output/tenNodesSimulations/clientled/WithoutConsensus/regular/exponent1.4/seed1/Clientled-Migrations-6s6n300c.csv'},
    '1.6': {'new_accounts_with_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.6/seed1/Clientled-Migrations-6s6n300c.csv', 'new_accounts_without_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/regular/exponent1.6/seed1/Clientled-Migrations-6s6n300c.csv','Regular_shard_assignment': 'output/tenNodesSimulations/clientled/WithoutConsensus/regular/exponent1.6/seed1/Clientled-Migrations-6s6n300c.csv'},
    #'1.8' :{'amicShardAssignment/exponent1.8/seed1/Clientled-Migrations-6s6n300c.csv', 'main_without_ShardAssignment': 'output/tenNodesSimulations/clientled/MainShard/WithoutConsensus/regular/exponent1.8/seed1/Clientled-Migrations-6s6n300c.csv', 'TransactionBased_with_ShardAssignment': 'output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.8/seed1/Clientled-Migrations-6s6n300c.csv', 'TransactionBased_without_ShardAssignment': 'output/tenNodesSimulations/clientled/DataStructure/WithoutCo//////////////////////////////////nsensus/regular/exponent1.8/seed1/Clientled-Migrations-6s6n300c.csv', 'new_accounts_with_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/DynamicShardAssignment/exponent1.8/seed1/Clientled-Migrations-6s6n300c.csv', 'new_accounts_without_ShardAssignment': 'output/tenNodesSimulations/clientled/DynamicShardAssignment/WithoutConsensus/regular/exponent1.8/seed1/Clientled-Migrations-6s6n300c.csv'} 
    # Add paths for other exponents
}

# Exponents and policies for plotting
exponents = ['0.6', '0.8', '1.2' , '1.4' , '1.6']  # Add other exponents as needed
policies = ['new_accounts_with_ShardAssignment', 'new_accounts_without_ShardAssignment', 'Regular_shard_assignment']
colors = ['blue', 'green', 'red', 'orange', 'magenta', 'brown']  # Extend as needed for policies

# Initialize plot data
data = {exp: {policy: {'cross_shard': 0, 'intra_shard': 0} for policy in policies} for exp in exponents}

# Load data (this is a placeholder - replace with your actual data loading logic)
for exponent, paths in file_paths.items():
    for policy, path in paths.items():
        df = pd.read_csv(path)
        cross_shard = df['CrossShard Transactions'].max()
        intra_shard = df['IntraShard Transactions'].max()
        migration_count = df['Migration Count'].max()
        data[exponent][policy]['cross_shard'] = cross_shard
        data[exponent][policy]['intra_shard'] = intra_shard
        data[exponent][policy]['migration_count'] = migration_count

# Adjustments for better visualization
bar_width = 0.15  # Adjust bar width for visibility
space_between_bars_within_group = 0.02  # Space between bars within the same group
space_between_groups = 0.15  # Space between different groups (exponents)

fig, ax = plt.subplots(figsize=(15, 8))

# Calculate the number of groups and the total number of bars in a group
num_groups = len(exponents)
bars_per_group = len(policies)

# Generate base positions for each group
base_positions = np.arange(num_groups) * (bars_per_group * bar_width + space_between_groups)

for i, exponent in enumerate(exponents):
    for j, policy in enumerate(policies):
        # Calculate specific position for each bar
        position = base_positions[i] + (j * bar_width)
        # Retrieve cross-shard, intra-shard, and migration count data
        cross_shard = data[exponent][policy]['cross_shard']
        intra_shard = data[exponent][policy]['intra_shard']
        migration_count = data[exponent][policy]['migration_count']
        # Plot bars
        ax.bar(position, cross_shard, width=bar_width, color=colors[j], edgecolor='black', label=f'{policy} CrossShard')
        ax.bar(position, intra_shard, width=bar_width, bottom=cross_shard, color=colors[j], alpha=0.5, edgecolor='black', label=f'{policy} IntraShard')
        # Add migration count on top of the cross-shard transaction bar
        total_height = cross_shard + intra_shard
        migration_count = migration_count  # Placeholder, replace with actual logic to retrieve migration count
        ax.text(position, total_height, str(migration_count), ha='center', va='bottom')

# Customizing plot
ax.set_xticks(base_positions + bars_per_group * bar_width / 2 - bar_width / 2)
ax.set_xticklabels(exponents)
ax.set_xlabel('Exponent')
ax.set_ylabel('Transactions')
ax.set_title('Cross-Shard Intra-ShardTransactions by Exponent and Policy')

# Create and add custom legend outside the plot
legend_handles = [mpatches.Patch(color=color, label=policy) for policy, color in zip(policies, colors)]
ax.legend(handles=legend_handles, title="Policies", bbox_to_anchor=(1.05, 1), loc='upper left')

plt.tight_layout()
plt.show()