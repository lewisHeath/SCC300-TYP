import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.patches import Patch
# Define your file paths and policy names
file_paths = {
    'Main_Shard': 'output/tenNodesSimulations/clientled/MainShard/WithConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-ShardLoad-32s6n300c.csv',
    'Transaction_Based': 'output/tenNodesSimulations/clientled/DataStructure/WithConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-ShardLoad-32s6n300c.csv',
    'Regular': 'output/tenNodesSimulations/clientled/WithConsensus/regular/exponent1.4/seed1/Clientled-ShardLoad-32s6n300c.csv'

}


fig, ax = plt.subplots(figsize=(25, 10))  # Increased figure size

bar_width = 0.2 # Reduced bar width
indices = np.arange(32)

policy_colors = ['red', 'yellow', 'blue']

max_value = 0

for policy_idx, (policy_name, file_path) in enumerate(file_paths.items()):
    df = pd.read_csv(file_path)
    agg_df = df.groupby('Shard').agg({'Shard Load': 'max', 'Migration Transactions': 'sum'}).reset_index()
    current_max = max(agg_df['Shard Load'] + agg_df['Migration Transactions'])
    max_value = max(max_value, current_max)

    positions = indices - (len(file_paths) / 2 - policy_idx) * bar_width

    for i, row in agg_df.iterrows():
        ax.bar(positions[i], row['Shard Load'], width=bar_width, color=policy_colors[policy_idx], edgecolor='black')
        ax.bar(positions[i], row['Migration Transactions'], width=bar_width, bottom=row['Shard Load'], color=policy_colors[policy_idx], alpha=0.5, edgecolor='black')

ax.set_xlabel('Shard ID')
ax.set_ylabel('Load / Transactions')
ax.set_title('Shard Load and Migration Transactions per Shard by Policy (WITH CONSENSUS)')
ax.set_xticks(indices[::10])  # Only label every 10th shard
ax.set_xticklabels([f'{i+1}' for i in range(0, 32, 10)], rotation=45)

ax.set_ylim(0, max_value * 1.2)

legend_elements = [Patch(facecolor=color, edgecolor='black', label=policy) for policy, color in zip(file_paths.keys(), policy_colors)]
ax.legend(handles=legend_elements, loc='upper right', title="Policies")

plt.tight_layout()
plt.show()