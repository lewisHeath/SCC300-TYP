import pandas as pd
import matplotlib.pyplot as plt

# Read migration logs
df = pd.read_csv('output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-Migrations-64s10n800c.csv')

# Group by simulation time and get the last migration count for each time
unique_times = df.groupby('Simulation Time')['Migration Count'].last().reset_index()

# Plot the data
fig, ax1 = plt.subplots(figsize=(12, 6))

# Bar plots for transactions
bar_width = 0.4
bar_positions_migration = range(len(unique_times))
ax1.bar(bar_positions_migration, df.groupby('Simulation Time').last()['CrossShard Transactions'], width=bar_width, label='CrossShard Transactions')
ax1.bar([pos + bar_width for pos in bar_positions_migration], df.groupby('Simulation Time').last()['IntraShard Transactions'], width=bar_width, label='IntraShard Transactions')

# Scatter plot for migration count (plot a dot for each unique simulation time)
ax2 = ax1.twinx()
ax2.scatter(bar_positions_migration, unique_times['Migration Count'], color='red', marker='o', label='Migration Count')

# Adjust labels as needed
ax1.set_xticks(bar_positions_migration)
ax1.set_xticklabels([f"{row['Simulation Time']}s" for _, row in unique_times.iterrows()], rotation=45, ha='right', fontsize=8)
ax1.set_xlabel('Simulation Time (s)')
ax1.set_ylabel('Transaction Count')
ax2.set_ylabel('Migration Count')

# Combine legends from both axes
lines, labels = ax1.get_legend_handles_labels()
lines2, labels2 = ax2.get_legend_handles_labels()
ax2.legend(lines + lines2, labels + labels2, loc='upper left')

plt.title('Cross-Shard and Intra-Shard Transactions with Migration Count')
plt.tight_layout()
plt.show()
