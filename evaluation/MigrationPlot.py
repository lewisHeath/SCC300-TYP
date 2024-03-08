import pandas as pd
import matplotlib.pyplot as plt

# Load the without migration data
df_without_migration = pd.read_csv('output/tenNodesSimulations/clientled/exponent2.1/seed1/Clientled-WithoutMigrations-10s10n800c.csv')

# Load the migration data
df_DataStructure = pd.read_csv('output/tenNodesSimulations/clientled/DataStructure/exponent1.8/seed1/Clientled-Migrations-8s10n800c.csv')

df_MainShard =  pd.read_csv('output/tenNodesSimulations/clientled/MainShard/exponent1.8/seed1/Clientled-Migrations-8s10n800c.csv')

df_NewAccounts =  pd.read_csv('output/tenNodesSimulations/clientled/NewAccounts/exponent2.1/seed1/Clientled-Migrations-8s10n800c.csv')
# Convert columns to numeric, handling non-numeric values
numeric_cols = ['CrossShard Transactions', 'IntraShard Transactions', 'Simulation Time']
df_DataStructure[numeric_cols] = df_DataStructure[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_without_migration[numeric_cols] = df_without_migration[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_MainShard[numeric_cols] = df_MainShard[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_NewAccounts[numeric_cols] = df_NewAccounts[numeric_cols].apply(pd.to_numeric, errors='coerce')
# Calculate the total transactions for each scenario
DtcrossShard_migration = df_DataStructure['CrossShard Transactions'].sum()
DtintraShard_migration = df_DataStructure['IntraShard Transactions'].sum()
MscrossShard_migration = df_MainShard['CrossShard Transactions'].sum()
MSintraShard_migration = df_MainShard['IntraShard Transactions'].sum()
NAcrossShard_migration = df_NewAccounts['CrossShard Transactions'].sum()
NAintraShard_migration = df_NewAccounts['IntraShard Transactions'].sum()
crossShard_without_migration = df_without_migration['CrossShard Transactions'].sum()
intraShard_without_migration = df_without_migration['IntraShard Transactions'].sum()

# Plotting
fig, ax = plt.subplots()

# Stacked bars for with and without migrations
ax.bar('DataStructure Migration', DtcrossShard_migration, label='CrossShard Transactions', color='blue')
ax.bar('DataStructure Migration', DtintraShard_migration, bottom=DtcrossShard_migration, label='IntraShard Transactions', color='red')
ax.bar('Main Shard Migration', MscrossShard_migration, color='blue')
ax.bar('Main Shard Migration', MSintraShard_migration, bottom=MscrossShard_migration,color='red')
ax.bar('New accounts Migration', NAcrossShard_migration, color='blue')
ax.bar('New accounts Migration', NAintraShard_migration, bottom=NAcrossShard_migration, color='red')
ax.bar('Without Migration', crossShard_without_migration, color='blue')
ax.bar('Without Migration', intraShard_without_migration, bottom=crossShard_without_migration, color='red')

ax.set_ylabel('Number of Transactions')
ax.set_title('Transaction Distribution with and without Migrations')
ax.legend()

plt.show()
