import pandas as pd
import matplotlib.pyplot as plt
import numpy as np


# import the data client led
df_1_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-1s10n50c.csv')
df_2_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-2s10n50c.csv')
df_4_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-4s10n100c.csv')
df_8_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-8s10n100c.csv')
df_16_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-16s10n200c.csv')
df_32_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-32s10n400c.csv')
df_64_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-64s10n800c.csv')
df_128_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-128s10n800c.csv')
df_192_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-192s10n800c.csv')
df_256_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-256s10n800c.csv')

def count_cross_shard_transactions(df):
    cross_shard_count = df[df['CrossShard'] == True].shape[0]
    non_cross_shard_count = df[df['CrossShard'] == False].shape[0]
    return cross_shard_count, non_cross_shard_count

# all_dataframes = [df_1_shard_shardled, df_2_shard_shardled, df_4_shard_shardled, df_8_shard_shardled, df_16_shard_shardled, df_32_shard_shardled, df_64_shard_shardled, df_128_shard_shardled, df_192_shard_shardled, df_256_shard_shardled]
all_dataframes = [df_1_shard_clientled, df_2_shard_clientled, df_4_shard_clientled, df_8_shard_clientled, df_16_shard_clientled, df_32_shard_clientled, df_64_shard_clientled, df_128_shard_clientled, df_192_shard_clientled, df_256_shard_clientled]

cross_shard_counts = []
non_cross_shard_counts = []

for df in all_dataframes:
    cross_shard_count, non_cross_shard_count = count_cross_shard_transactions(df)
    cross_shard_counts.append(cross_shard_count)
    non_cross_shard_counts.append(non_cross_shard_count)

labels = ['1', '2', '4', '8', '16', '32', '64', '128', '192', '256']
x = np.arange(len(labels))
width = 0.35

fig, ax = plt.subplots()
rects1 = ax.bar(x - width/2, cross_shard_counts, width, label='Cross Shard')
rects2 = ax.bar(x + width/2, non_cross_shard_counts, width, label='Non-Cross Shard')

ax.set_ylabel('Number of Transactions')
ax.set_xlabel('Number of Shards')
ax.set_title('Cross Shard vs Non-Cross Shard Transactions ( client led )')
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.legend()

def autolabel(rects):
    for rect in rects:
        height = rect.get_height()
        ax.annotate('{}'.format(height),
                    xy=(rect.get_x() + rect.get_width() / 2, height),
                    xytext=(0, 3),
                    textcoords="offset points",
                    ha='center', va='bottom')

autolabel(rects1)
autolabel(rects2)

fig.tight_layout()
plt.show()



