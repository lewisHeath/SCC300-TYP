import pandas as pd
import matplotlib.pyplot as plt

# Load the CSV data
df = pd.read_csv('output/tenNodesSimulations/clientled/exponent0.6/seed1/Clientled-ShardLoad-6s6n300c.csv')

# Aggregate the data for each shard
agg_df = df.groupby('Shard').agg({'Shard Load': 'max', 'Migration Transactions': 'sum'}).reset_index()

# Plotting
fig, ax = plt.subplots(figsize=(10, 6))

# Colors for the bar segments
colors = ['skyblue', 'orange']

# Plot total transactions and migration transactions for each shard
for i, row in agg_df.iterrows():
    # Total Shard Load
    ax.bar(row['Shard'], row['Shard Load'], color=colors[0], label='Total Shard Load' if i == 0 else "")
    # Migration Transactions
    ax.bar(row['Shard'], row['Migration Transactions'], color=colors[1], bottom=row['Shard Load'] - row['Migration Transactions'], label='Migration Transactions' if i == 0 else "")

# Setting labels and title
ax.set_xlabel('Shard ID')
ax.set_ylabel('Transactions')
ax.set_title('Shard Load and Migration Transactions per Shard')

# Adding legend
handles, labels = ax.get_legend_handles_labels()
ax.legend(handles[::-1], labels[::-1], loc='upper left')

plt.show()