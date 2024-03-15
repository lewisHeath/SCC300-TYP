import pandas as pd
import matplotlib.pyplot as plt

# Load the without migration data
df_0_2Exponent = pd.read_csv('output/tenNodesSimulations/clientled/exponent1.8/seed1/Clientled-WithoutMigrations-6s6n300c.csv')

# Load the migration data
df_0_4Exponent = pd.read_csv('output/tenNodesSimulations/clientled/NewAccounts/without_consensus/exponent0.4/seed1/Clientled-Migrations-6s6n300c.csv')

df_0_6Exponent =  pd.read_csv('output/tenNodesSimulations/clientled/NewAccounts/without_consensus/exponent0.6/seed1/Clientled-Migrations-6s6n300c.csv')

df_0_8Exponent =  pd.read_csv('output/tenNodesSimulations/clientled/NewAccounts/without_consensus/exponent0.8/seed1/Clientled-Migrations-6s6n300c.csv')

df_1_0Exponent =  pd.read_csv('output/tenNodesSimulations/clientled/NewAccounts/without_consensus/exponent1.6/seed1/Clientled-Migrations-6s6n300c.csv')

df_1_2Exponent =  pd.read_csv('output/tenNodesSimulations/clientled/NewAccounts/without_consensus/exponent1.2/seed1/Clientled-Migrations-6s6n300c.csv')



# Convert columns to numeric, handling non-numeric values
numeric_cols = ['CrossShard Transactions', 'IntraShard Transactions', 'Simulation Time']
df_0_4Exponent[numeric_cols] = df_0_4Exponent[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_0_2Exponent[numeric_cols] = df_0_2Exponent[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_0_6Exponent[numeric_cols] = df_0_6Exponent[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_0_8Exponent[numeric_cols] = df_0_8Exponent[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_1_0Exponent[numeric_cols] = df_1_0Exponent[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_1_2Exponent[numeric_cols] = df_1_2Exponent[numeric_cols].apply(pd.to_numeric, errors='coerce')
# Calculate the total transactions for each scenario
crossShard_0_4Exponent = df_0_4Exponent['CrossShard Transactions'].max()
intraShard_0_4Exponent = df_0_4Exponent['IntraShard Transactions'].max()
crossShard_0_6Exponent = df_0_6Exponent['CrossShard Transactions'].max()
intraShard_0_6Exponent = df_0_6Exponent['IntraShard Transactions'].max()
crossShard_0_8Exponent = df_0_8Exponent['CrossShard Transactions'].max()
intraShard_0_8Exponent = df_0_8Exponent['IntraShard Transactions'].max()
crossShard_0_2Exponent = df_0_2Exponent['CrossShard Transactions'].max()
intraShard_0_2Exponent = df_0_2Exponent['IntraShard Transactions'].max()
crossShard_1_2Exponent = df_1_2Exponent['CrossShard Transactions'].max()
intraShard_1_2Exponent = df_1_2Exponent['IntraShard Transactions'].max()
crossShard_1_0Exponent = df_1_0Exponent['CrossShard Transactions'].max()
intraShard_1_0Exponent = df_1_0Exponent['IntraShard Transactions'].max()




# Plotting
fig, ax = plt.subplots()

# Assuming these are your total migration counts
df0_4Exponent_migrationCount = df_0_4Exponent['Migration Count'].max()
df_0_6Exponent_migrationCount = df_0_6Exponent['Migration Count'].max()
df_0_8Exponent_migrationCount = df_0_8Exponent['Migration Count'].max()
df0_2Exponent_migrationCount = df_0_2Exponent['Migration Count'].max()
df_1_0Exponent_migrationCount = df_1_0Exponent['Migration Count'].max()
df1_2Exponent_migrationCount = df_1_2Exponent['Migration Count'].max()


# Labels for your bars
labels = ['0.2Exponent', '0.4 Exponent', '0.6 Exponent', '0.8 Exponent', '1.0 Exponent', '1.2 Exponent']

# Values for CrossShard Transactions
cross_shard_values = [crossShard_0_2Exponent, crossShard_0_4Exponent, crossShard_0_6Exponent, crossShard_0_8Exponent, crossShard_1_0Exponent, crossShard_1_2Exponent]

# Values for IntraShard Transactions, note the last value for "Without Migration" is not stacked
intra_shard_values = [intraShard_0_2Exponent, intraShard_0_4Exponent, intraShard_0_6Exponent, intraShard_0_8Exponent, intraShard_1_0Exponent, intraShard_1_2Exponent]

# Migration counts to be annotated
migration_counts = [df0_2Exponent_migrationCount, df0_4Exponent_migrationCount, df_0_6Exponent_migrationCount, df_0_8Exponent_migrationCount,  df_1_0Exponent_migrationCount,  df1_2Exponent_migrationCount ]


# Secondary axis for Intra-Shard Transactions
ax2 = ax.twinx()

# Plot Intra-Shard Transactions on secondary y-axis
# Note: For illustration, using the same bar positions with an offset. In practice, adjust as needed.
offset = 0.35  # Adjust offset as needed for clarity
ax2.bar('0_4Exponent Migration', intra_shard_values, width=offset, color='none')

# Plotting the bars
bars = ax.bar(labels, cross_shard_values, label='CrossShard Transactions', color='blue')
# Stacking IntraShard Transactions on top of the corresponding CrossShard Transaction bars
ax.bar(labels, intra_shard_values, bottom=cross_shard_values, label='IntraShard Transactions', color='red')
# Invert secondary y-axis to start from top
ax2.invert_yaxis()
ax2.legend(loc='upper right')
ax.set_ylabel('Number of Transactions')
ax.set_title('Cross-shard and Intra-shard transactions with different exponent (no consensus)')
ax.legend()

# Function to annotate bars
def annotate_bars(bars, counts):
    for bar, count in zip(bars, counts):
        yval = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2, yval + 5, count, ha='center', va='bottom')

# Only pass the bars that correspond to migration scenarios (exclude "Without Migration")
annotate_bars(bars[:-1], migration_counts)

plt.show()