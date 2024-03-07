import pandas as pd
import matplotlib.pyplot as plt

# Load the without migration data
df_without_migration = pd.read_csv('output/tenNodesSimulations/clientled/exponent2.6/seed1/Clientled-WithoutMigrations-10s10n800c.csv')

# Load the migration data
df_migration = pd.read_csv('output/tenNodesSimulations/clientled/exponent2.6/seed1/Clientled-Migrations-10s10n800c.csv')

# Convert columns to numeric, handling non-numeric values
numeric_cols = ['CrossShard Transactions', 'IntraShard Transactions', 'Simulation Time']
df_migration[numeric_cols] = df_migration[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_without_migration[numeric_cols] = df_without_migration[numeric_cols].apply(pd.to_numeric, errors='coerce')

# Calculate the total transactions for each scenario
crossShard_migration = df_migration['CrossShard Transactions'].sum()
intraShard_migration = df_migration['IntraShard Transactions'].sum()
crossShard_without_migration = df_without_migration['CrossShard Transactions'].sum()
intraShard_without_migration = df_without_migration['IntraShard Transactions'].sum()

# Plotting
fig, ax = plt.subplots()

# Stacked bars for with and without migrations
ax.bar('With Migration', crossShard_migration, label='CrossShard Transactions', color='blue')
ax.bar('With Migration', intraShard_migration, bottom=crossShard_migration, label='IntraShard Transactions', color='red')

ax.bar('Without Migration', crossShard_without_migration, label='CrossShard Transactions', color='blue')
ax.bar('Without Migration', intraShard_without_migration, bottom=crossShard_without_migration, label='IntraShard Transactions', color='red')

ax.set_ylabel('Number of Transactions')
ax.set_xlabel('Migration Scenario')
ax.set_title('Transaction Distribution with and without Migrations')
ax.legend()

plt.show()
