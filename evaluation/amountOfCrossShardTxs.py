import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Import the data client led
df_32_shard_clientled = pd.read_csv('output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-Migrations-32s10n800c.csv')

def count_cross_shard_transactions(df):
    cross_shard_count = df['CrossShard Transactions'].value_counts().sort_index().reset_index()
    non_cross_shard_count = df['IntraShard Transactions'].value_counts().sort_index().reset_index()
    return cross_shard_count, non_cross_shard_count

all_dataframes = [df_32_shard_clientled]

cross_shard_counts = []
non_cross_shard_counts = []

for df in all_dataframes:
    cross_shard_count, non_cross_shard_count = count_cross_shard_transactions(df)
    cross_shard_counts.append(cross_shard_count)
    non_cross_shard_counts.append(non_cross_shard_count)

labels = ['32']
x = np.arange(len(labels))
width = 0.50

fig, ax = plt.subplots()

# Bar plots for CrossShard Transactions
rects1 = ax.bar(range(len(cross_shard_counts[0])), cross_shard_counts[0]['CrossShard Transactions'], width, label='CrossShard Transactions')

# Bar plots for IntraShard Transactions
rects2 = ax.bar(np.arange(len(non_cross_shard_counts[0])) + width, non_cross_shard_counts[0]['IntraShard Transactions'], width, label='IntraShard Transactions')

ax.set_ylabel('Transactions')
ax.set_xlabel('Simulation Time')
ax.set_title('Cross Shard vs Non-Cross Shard Transactions ( client led )')
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.legend()

# Remove the numbers on top of each bar
def autolabel(rects):
    pass

autolabel(rects1)
autolabel(rects2)

fig.tight_layout()
plt.show()
