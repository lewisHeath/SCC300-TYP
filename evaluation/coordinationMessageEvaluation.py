
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter

# client led


df_2_shard_clientled = pd.read_csv('output/tenNodesSimulations/clientled/with_Migration/exponent2.6/seed1/ClientledCoordinationMessageLogger-10s10n800c.csv')
df_10_shard_clientled = pd.read_csv('output/tenNodesSimulations/clientled/exponent2.6/seed1/ClientledCoordinationMessageLogger-10s10n800c.csv')

# client led
print("Intra-shard-committed messages for 2 shard clientled: " + str(df_2_shard_clientled[df_2_shard_clientled['MessageType'] == 'intra-shard-committed'].shape[0]))
#print("Intra-shard-committed messages for 10 shard clientled: " + str(df_10_shard_clientled[df_10_shard_clientled['MessageType'] == 'intra-shard-committed'].shape[1]))


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

dataframesClientled = [ df_10_shard_clientled]



labels = [ '2']

plot_message_counts(dataframesClientled, labels)