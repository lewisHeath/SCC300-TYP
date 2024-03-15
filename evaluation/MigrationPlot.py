import pandas as pd
import matplotlib.pyplot as plt

# Load the without migration data
df_without_migration = pd.read_csv('output/tenNodesSimulations/clientled/exponent1.6/seed1/Clientled-WithoutMigrations-6s6n300c.csv')

# Load the migration data
df_DataStructure = pd.read_csv('output/tenNodesSimulations/clientled/DataStructure/exponent1.6/seed1/Clientled-Migrations-6s6n300c.csv')

df_MainShard =  pd.read_csv('output/tenNodesSimulations/clientled/MainShard/exponent1.6/seed1/Clientled-Migrations-6s6n300c.csv')

df_NewAccounts =  pd.read_csv('output/tenNodesSimulations/clientled/NewAccounts/without_consensus/exponent1.6/seed1/Clientled-Migrations-6s6n300c.csv')
# Convert columns to numeric, handling non-numeric values
numeric_cols = ['CrossShard Transactions', 'IntraShard Transactions', 'Simulation Time']
df_DataStructure[numeric_cols] = df_DataStructure[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_without_migration[numeric_cols] = df_without_migration[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_MainShard[numeric_cols] = df_MainShard[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_NewAccounts[numeric_cols] = df_NewAccounts[numeric_cols].apply(pd.to_numeric, errors='coerce')
# Calculate the total transactions for each scenario
DtcrossShard_migration = df_DataStructure['CrossShard Transactions'].max()
DtintraShard_migration = df_DataStructure['IntraShard Transactions'].max()
MscrossShard_migration = df_MainShard['CrossShard Transactions'].max()
MSintraShard_migration = df_MainShard['IntraShard Transactions'].max()
NAcrossShard_migration = df_NewAccounts['CrossShard Transactions'].max()
NAintraShard_migration = df_NewAccounts['IntraShard Transactions'].max()
crossShard_without_migration = df_without_migration['CrossShard Transactions'].max()
intraShard_without_migration = df_without_migration['IntraShard Transactions'].max()



# Plotting
fig, ax = plt.subplots()

# Assuming these are your total migration counts
DataStructure_migration_count = df_DataStructure['Migration Count'].max()
MainShard_migration_count = df_MainShard['Migration Count'].max()
df_NewAccounts = df_NewAccounts['Migration Count'].max()

# Labels for your bars
labels = ['DataStructure Migration', 'Main Shard Migration', 'New Accounts Migration', 'Without Migration']

# Values for CrossShard Transactions
cross_shard_values = [DtcrossShard_migration, MscrossShard_migration, NAcrossShard_migration, crossShard_without_migration]

# Values for IntraShard Transactions, note the last value for "Without Migration" is not stacked
intra_shard_values = [DtintraShard_migration, MSintraShard_migration, NAintraShard_migration, intraShard_without_migration]

# Migration counts to be annotated
migration_counts = [DataStructure_migration_count, MainShard_migration_count, df_NewAccounts]


# Secondary axis for Intra-Shard Transactions
ax2 = ax.twinx()

# Plot Intra-Shard Transactions on secondary y-axis
# Note: For illustration, using the same bar positions with an offset. In practice, adjust as needed.
offset = 0.35  # Adjust offset as needed for clarity
ax2.bar('DataStructure Migration', intra_shard_values, width=offset, color='none')

# Plotting the bars
bars = ax.bar(labels, cross_shard_values, label='CrossShard Transactions', color='blue')
# Stacking IntraShard Transactions on top of the corresponding CrossShard Transaction bars
ax.bar(labels, intra_shard_values, bottom=cross_shard_values, label='IntraShard Transactions', color='red')
# Invert secondary y-axis to start from top
ax2.invert_yaxis()
ax2.legend(loc='upper right')
ax.set_ylabel('Number of Transactions')
ax.set_title('Transactions with and without Migrations: 1000Accounts 1.6exponent 6shards')
ax.legend()

# Function to annotate bars
def annotate_bars(bars, counts):
    for bar, count in zip(bars, counts):
        yval = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2, yval + 5, count, ha='center', va='bottom')

# Only pass the bars that correspond to migration scenarios (exclude "Without Migration")
annotate_bars(bars[:-1], migration_counts)

plt.show()