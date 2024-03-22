import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.patches import Patch

# Load data for each scenario
df_without_migration_with = pd.read_csv('output/tenNodesSimulations/clientled/WithConsensus/regular/exponent1.4/seed1/Clientled-Migrations-32s6n300c.csv')
df_DataStructure_with = pd.read_csv('output/tenNodesSimulations/clientled/DataStructure/WithConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-32s6n300c.csv')
df_MainShard_with = pd.read_csv('output/tenNodesSimulations/clientled/MainShard/WithConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-32s6n300c.csv')

df_without_migration_without = pd.read_csv('output/tenNodesSimulations/clientled/WithoutConsensus/regular/exponent1.4/seed1/Clientled-Migrations-32s6n300c.csv')
df_DataStructure_without = pd.read_csv('output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-32s6n300c.csv')
df_MainShard_without = pd.read_csv('output/tenNodesSimulations/clientled/MainShard/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-Migrations-32s6n300c.csv')




data = {
    'With Consensus': {
        'Transaction based': {
            'cross_shard': df_DataStructure_with['CrossShard Transactions'].max(),
            'intra_shard': df_DataStructure_with['IntraShard Transactions'].max(),
            'migration_count': df_DataStructure_with['Migration Count'].max()  # Assuming this column exists
        },
        'Main Shard': {
            'cross_shard': df_MainShard_with['CrossShard Transactions'].max(),
            'intra_shard': df_MainShard_with['IntraShard Transactions'].max(),
            'migration_count': df_MainShard_with['Migration Count'].max()
        },
        'Without Migration': {
            'cross_shard': df_without_migration_with['CrossShard Transactions'].max(),
            'intra_shard': df_without_migration_with['IntraShard Transactions'].max(),
            'migration_count' : df_without_migration_with['Migration Count'].max()
        }
    },
    'Without Consensus': {
        'Transaction based': {
            'cross_shard': df_DataStructure_without['CrossShard Transactions'].max(),
            'intra_shard': df_DataStructure_without['IntraShard Transactions'].max(),
            'migration_count': df_DataStructure_without['Migration Count'].max()
        },
        'Main Shard': {
            'cross_shard': df_MainShard_without['CrossShard Transactions'].max(),
            'intra_shard': df_MainShard_without['IntraShard Transactions'].max(),
            'migration_count': df_MainShard_without['Migration Count'].max()
        },
        'Without Migration': {
            'cross_shard': df_without_migration_without['CrossShard Transactions'].max(),
            'intra_shard': df_without_migration_without['IntraShard Transactions'].max(),
            'migration_count' : df_without_migration_without['Migration Count'].max()
        }
    }
}
fig, ax = plt.subplots(figsize=(14, 8))

bar_width = 0.15
space_between_groups = bar_width / 2
n_groups = len(data['With Consensus'])
base_positions_with = np.arange(n_groups) * (2 * bar_width + space_between_groups)
offset_without = bar_width

colors = {
    'CrossShard With': 'blue',
    'IntraShard With': 'skyblue',
    'CrossShard Without': 'green',
    'IntraShard Without': 'lightgreen'
}

# Iterate through each policy and plot bars for 'With' and 'Without Consensus'
for i, (policy, transactions) in enumerate(data['With Consensus'].items()):
    position_with = base_positions_with[i]
    # With Consensus
    cross_shard_with = transactions['cross_shard']
    intra_shard_with = transactions['intra_shard']
    # Assuming migration count is available for "With Consensus"
    migration_count_with = transactions.get('migration_count', 0)
    
    # Plotting 'With Consensus' bars
    ax.bar(position_with, cross_shard_with, width=bar_width, color=colors['CrossShard With'], edgecolor='black')
    ax.bar(position_with, intra_shard_with, width=bar_width, bottom=cross_shard_with, color=colors['IntraShard With'], edgecolor='black')
    # Annotating with migration count for 'With Consensus'
    if migration_count_with:
        ax.text(position_with, cross_shard_with + intra_shard_with, f'{migration_count_with}', ha='center', va='bottom')

    # Without Consensus
    transactions_without = data['Without Consensus'][policy]
    position_without = position_with + offset_without
    cross_shard_without = transactions_without['cross_shard']
    intra_shard_without = transactions_without['intra_shard']
    # Assuming migration count is available for "Without Consensus"
    migration_count_without = transactions_without.get('migration_count', 0)
    
    # Plotting 'Without Consensus' bars
    ax.bar(position_without, cross_shard_without, width=bar_width, color=colors['CrossShard Without'], edgecolor='black')
    ax.bar(position_without, intra_shard_without, width=bar_width, bottom=cross_shard_without, color=colors['IntraShard Without'], edgecolor='black')
    # Annotating with migration count for 'Without Consensus'
    if migration_count_without:
        ax.text(position_without, cross_shard_without + intra_shard_without, f'{migration_count_without}', ha='center', va='bottom')

# Adjusting x-ticks to center them between 'With' and 'Without' bars for each policy
ax.set_xticks(base_positions_with + offset_without / 2)
ax.set_xticklabels(list(data['With Consensus'].keys()), rotation=45)

ax.set_xlabel('Policies')
ax.set_ylabel('Transactions')
ax.set_title('Transactions and Migration Counts by Policy and Consensus Status')

# Creating a custom legend
legend_elements = [
    Patch(facecolor=colors['CrossShard With'], edgecolor='black', label='CrossShard Transactions (With Consensus)'),
    Patch(facecolor=colors['IntraShard With'], edgecolor='black', label='IntraShard Transactions (With Consensus)'),
    Patch(facecolor=colors['CrossShard Without'], edgecolor='black', label='CrossShard Transactions (Without Consensus)'),
    Patch(facecolor=colors['IntraShard Without'], edgecolor='black', label='IntraShard Transactions (Without Consensus)'),
]
ax.legend(handles=legend_elements, loc='upper right')

plt.tight_layout()
plt.show()