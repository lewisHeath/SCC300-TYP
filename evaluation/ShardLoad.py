import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.patches import Patch
# Define your file paths and policy names
file_paths = {
    'Main_Shard': 'output/tenNodesSimulations/clientled/MainShard/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-ShardLoad-6s6n300c.csv',
    'Transaction_Based': 'output/tenNodesSimulations/clientled/DataStructure/WithoutConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-ShardLoad-6s6n300c.csv',
    'Regular': 'output/tenNodesSimulations/clientled/WithoutConsensus/regular/exponent1.4/seed1/Clientled-ShardLoad-6s6n300c.csv'
}

# Initialize the figure
fig, ax = plt.subplots(figsize=(10, 8))

# Define bar width and the number of shards
bar_width = 0.1
indices = np.arange(6)  # Assuming 6 shards

# Policy colors
policy_colors = ['red', 'green', 'blue', 'purple']  # Extend this list based on the number of policies

# Plot bars for each policy
for policy_idx, (policy_name, file_path) in enumerate(file_paths.items()):
    # Load the data
    df = pd.read_csv(file_path)

    # Aggregate data by shard
    agg_df = df.groupby('Shard').agg({'Shard Load': 'max', 'Migration Transactions': 'sum'}).reset_index()

    # Position adjustments for side-by-side bars
    position_adjustment = (bar_width * len(file_paths)) / 2 - (policy_idx * bar_width)

    # Plotting each shard's data
    for i, row in agg_df.iterrows():
        position = indices[i] - position_adjustment
        # Shard Load
        ax.bar(position, row['Shard Load'], width=bar_width, color=policy_colors[policy_idx], label=f"{policy_name} Shard Load" if i == 0 else "", edgecolor='black')
        # Migration Transactions
        ax.bar(position, row['Migration Transactions'], width=bar_width, bottom=row['Shard Load'], color=policy_colors[policy_idx], alpha=0.5, label=f"{policy_name} Migrations" if i == 0 else "", edgecolor='black')

# Customizing the plot
ax.set_xlabel('Shard ID')
ax.set_ylabel('Load / Transactions')
ax.set_title('Shard Load and Migration Transactions per Shard by Policy')
ax.set_xticks(indices)
ax.set_xticklabels([f'Shard {i+1}' for i in range(6)])

# Legend
legend_elements = [
    Patch(facecolor=color, label=policy, edgecolor='black') for policy, color in zip(file_paths.keys(), policy_colors)
] + [
   
    Patch(facecolor='grey', alpha=0.5, label='Migration Transactions', edgecolor='black')
]
ax.legend(handles=legend_elements, loc='upper left')

max_height = max(agg_df['Shard Load'] + agg_df['Migration Transactions'])
ax.set_ylim(0, max_height * 1.1)  # Set y-axis limit 10% higher than the max height

plt.tight_layout()
plt.show()