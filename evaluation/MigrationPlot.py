import pandas as pd
import matplotlib.pyplot as plt

# Load the without migration data to count its number of rows
df_without_migration = pd.read_csv('output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-WithoutMigrations-32s10n800c.csv')
num_rows_without_migration = len(df_without_migration)

# Load the migration data
df_migration = pd.read_csv('output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-Migrations-32s10n800c.csv')

# Add empty rows to the migration data to match the number of rows in the without migration data
num_rows_to_add = num_rows_without_migration - len(df_migration)
if num_rows_to_add > 0:
    empty_rows = pd.DataFrame([[None] * len(df_migration.columns)] * num_rows_to_add, columns=df_migration.columns)
    df_migration = pd.concat([df_migration, empty_rows], ignore_index=True)

# Convert columns to numeric, handling non-numeric values
numeric_cols = ['CrossShard Transactions', 'IntraShard Transactions', 'Simulation Time']
df_migration[numeric_cols] = df_migration[numeric_cols].apply(pd.to_numeric, errors='coerce')
df_without_migration[numeric_cols] = df_without_migration[numeric_cols].apply(pd.to_numeric, errors='coerce')

# Calculate the total transactions for each chunk
total_migration = df_migration[['CrossShard Transactions', 'IntraShard Transactions']].sum().values
total_without_migration = df_without_migration[['CrossShard Transactions', 'IntraShard Transactions']].sum().values

# Plotting
fig, ax = plt.subplots()

# Bar plots for chunk with migrations
ax.bar('With Migration - CrossShard', total_migration[0], label='CrossShard Transactions')
ax.bar('With Migration - IntraShard', total_migration[1], bottom=total_migration[0], label='IntraShard Transactions')

# Bar plots for chunk without migrations
ax.bar('Without Migration - CrossShard', total_without_migration[0], label='CrossShard Transactions')
ax.bar('Without Migration - IntraShard', total_without_migration[1], bottom=total_without_migration[0], label='IntraShard Transactions')

ax.set_ylabel('Number of Transactions')
ax.set_xlabel('Migration')
ax.set_title('CrossShard and IntraShard Transactions with and without Migrations')
ax.legend()

plt.show()
