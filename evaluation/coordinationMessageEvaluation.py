
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter

# client led

df_1_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledCoordinationMessageLogger-1s10n50c.csv')
df_2_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledCoordinationMessageLogger-2s10n50c.csv')
df_4_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledCoordinationMessageLogger-4s10n100c.csv')
df_8_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledCoordinationMessageLogger-8s10n100c.csv')
df_16_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledCoordinationMessageLogger-16s10n200c.csv')
df_32_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledCoordinationMessageLogger-32s10n400c.csv')
df_64_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledCoordinationMessageLogger-64s10n800c.csv')
df_128_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledCoordinationMessageLogger-128s10n800c.csv')
df_192_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledCoordinationMessageLogger-192s10n800c.csv')
df_256_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledCoordinationMessageLogger-256s10n800c.csv')

# shard led

df_1_shard_shardled = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledCoordinationMessageLogger-1s10n50c.csv')
df_2_shard_shardled = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledCoordinationMessageLogger-2s10n50c.csv')
df_4_shard_shardled = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledCoordinationMessageLogger-4s10n100c.csv')
df_8_shard_shardled = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledCoordinationMessageLogger-8s10n100c.csv')
df_16_shard_shardled = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledCoordinationMessageLogger-16s10n200c.csv')
df_32_shard_shardled = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledCoordinationMessageLogger-32s10n400c.csv')
df_64_shard_shardled = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledCoordinationMessageLogger-64s10n800c.csv')
df_128_shard_shardled = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledCoordinationMessageLogger-128s10n800c.csv')
df_192_shard_shardled = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledCoordinationMessageLogger-192s10n800c.csv')
df_256_shard_shardled = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledCoordinationMessageLogger-256s10n800c.csv')

# client led
print("Intra-shard-committed messages for 1 shard clientled: " + str(df_1_shard_clientled[df_1_shard_clientled['MessageType'] == 'intra-shard-committed'].shape[0]))
# shard led
print("Intra-shard-committed messages for 1 shard shardled: " + str(df_1_shard_shardled[df_1_shard_shardled['MessageType'] == 'intra-shard-committed'].shape[0]))

def plot_message_counts(dataframes, labels):
    message_counts = []

    for df in dataframes:
        message_count = df['MessageType'].value_counts()
        message_counts.append(message_count)


    counts_df = pd.DataFrame(message_counts, index=labels)
    counts_df.fillna(0, inplace=True)


    ax = counts_df.plot.bar(rot=0, figsize=(10, 6))
    ax.set_xlabel("Number of Shards")
    ax.set_ylabel("Message Count")
    ax.set_title("Message Count by MessageType for each Shard ( client led )")
    ax.yaxis.set_major_formatter(FuncFormatter(lambda x, _: '{:,.0f}'.format(x)))
    plt.show()

dataframesClientled = [df_1_shard_clientled, df_2_shard_clientled, df_4_shard_clientled, df_8_shard_clientled,
              df_16_shard_clientled, df_32_shard_clientled, df_64_shard_clientled, df_128_shard_clientled,
              df_192_shard_clientled, df_256_shard_clientled]

dataframesShardled = [df_1_shard_shardled, df_2_shard_shardled, df_4_shard_shardled, df_8_shard_shardled,
                df_16_shard_shardled, df_32_shard_shardled, df_64_shard_shardled, df_128_shard_shardled,
                df_192_shard_shardled, df_256_shard_shardled]

labels = ['1', '2', '4', '8', '16', '32', '64', '128', '192', '256']

plot_message_counts(dataframesShardled, labels)