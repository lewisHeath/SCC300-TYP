import pandas as pd
import matplotlib.pyplot as plt

# Load the data
df = pd.read_csv('output/tenNodesSimulations/clientled/DataStructure/exponent1.8/seed1/Clientled-Migrations-8s10n800c.csv')


# Plotting
plt.figure(figsize=(10, 6))

# Plot cross-shard transactions
plt.plot(df['Simulation Time'], df['CrossShard Transactions'], label='Cross-Shard Transactions', color='blue')

# Plot intra-shard transactions
plt.plot(df['Simulation Time'], df['IntraShard Transactions'], label='Intra-Shard Transactions', color='green')

# Mark migration points based on migration count
migration_points = df[df['Migration Count'] > 0]
plt.scatter(migration_points['Simulation Time'], migration_points['CrossShard Transactions'], color='red', label='Migration Points')

# Set labels and title
plt.xlabel('Simulation Time')
plt.ylabel('Number of Transactions')
plt.title('Cross-Shard and Intra-Shard Transactions over Time, with Main Shard policy')
plt.legend()

# Show plot
plt.grid(True)
plt.xticks(rotation=45)  # Rotate x-axis labels for better visibility
plt.tight_layout()  # Adjust layout to prevent overlapping labels
plt.show()