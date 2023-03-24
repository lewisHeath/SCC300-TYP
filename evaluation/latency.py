import pandas as pd
import matplotlib.pyplot as plt

def process_csv(df, nameOfFile):
    # Time, Tx Hash, Time Tx Created
    # Latency = Time - Time Tx Created
    print("this is for file: ", nameOfFile)
    df['Latency'] = df['Time'] - df['TxCreationTime']
    average_latency = df['Latency'].mean()
    print('Average Latency: ', round(average_latency, 2) ,'seconds')

    # print the length of the dataframe
    print('Committed Txs: ', len(df))

    crossShardOrNot = df["CrossShard"]
    # get the amount that are True
    crossShard = crossShardOrNot[crossShardOrNot == True]
    # get the amount that are False
    intraShard = crossShardOrNot[crossShardOrNot == False]
    print("Cross Shard Txs: ", len(crossShard))
    print("Intra Shard Txs: ", len(intraShard))

    # print the time of the last tx
    print('Last Tx Time: ', df['Time'].iloc[-1])

    # create a cumulative committed transactions
    df['CumulativeCommittedTxs'] = df.index + 1

    # create cumulative committed cross shard transactions
    df['CumulativeCommittedCrossShardTxs'] = df['CrossShard'].cumsum()
    # create cumulative committed intra shard transactions
    df['CumulativeCommittedIntraShardTxs'] = df['CumulativeCommittedTxs'] - df['CumulativeCommittedCrossShardTxs']
    # print(df)
    return df

# import the data
df_1_shard = pd.read_csv('../output/GlobalStats-CommittedLogger-1s48n100c.csv')
# cut off at Time > 60
df_1_shard = df_1_shard[df_1_shard['Time'] < 60]
# client led
df_2_shard_clientled = pd.read_csv('../output/GlobalStats-CommittedLogger-Clientled-2s24n100c.csv')
df_3_shard_clientled = pd.read_csv('../output/GlobalStats-CommittedLogger-Clientled-3s16n100c.csv')
df_4_shard_clientled = pd.read_csv('../output/GlobalStats-CommittedLogger-Clientled-4s12n100c.csv')
df_6_shard_clientled = pd.read_csv('../output/GlobalStats-CommittedLogger-Clientled-6s8n100c.csv')
df_8_shard_clientled = pd.read_csv('../output/GlobalStats-CommittedLogger-Clientled-8s6n100c.csv')
# shard led
df_2_shard_shardled = pd.read_csv('../output/GlobalStats-CommittedLogger-Shardled-2s24n100c.csv')
df_3_shard_shardled = pd.read_csv('../output/GlobalStats-CommittedLogger-Shardled-3s16n100c.csv')
df_4_shard_shardled = pd.read_csv('../output/GlobalStats-CommittedLogger-Shardled-4s12n100c.csv')
df_6_shard_shardled = pd.read_csv('../output/GlobalStats-CommittedLogger-Shardled-6s8n100c.csv')
df_8_shard_shardled = pd.read_csv('../output/GlobalStats-CommittedLogger-Shardled-8s6n100c.csv')

# process the data
df_1_shard = process_csv(df_1_shard, '1 Shard')
df_2_shard_clientled = process_csv(df_2_shard_clientled , '2 Shard Clientled')
df_3_shard_clientled = process_csv(df_3_shard_clientled , '3 Shard Clientled')
df_4_shard_clientled = process_csv(df_4_shard_clientled , '4 Shard Clientled')
df_6_shard_clientled = process_csv(df_6_shard_clientled , '6 Shard Clientled')
df_8_shard_clientled = process_csv(df_8_shard_clientled , '8 Shard Clientled')
df_2_shard_shardled = process_csv(df_2_shard_shardled , '2 Shard Shardled')
df_3_shard_shardled = process_csv(df_3_shard_shardled , '3 Shard Shardled')
df_4_shard_shardled = process_csv(df_4_shard_shardled , '4 Shard Shardled')
df_6_shard_shardled = process_csv(df_6_shard_shardled , '6 Shard Shardled')
df_8_shard_shardled = process_csv(df_8_shard_shardled , '8 Shard Shardled')

# X axis is "Time", Y axis is "CumulativeCommittedTxs"
# plot the cumulative committed transactions on the same graph with different colors

# Create a figure and axis object
fig, ax = plt.subplots()

# Plot the cumulative committed transactions for each file on the same axis
ax.plot(df_1_shard['Time'], df_1_shard['CumulativeCommittedTxs'], label='1 Shard')
ax.plot(df_2_shard_clientled['Time'], df_2_shard_clientled['CumulativeCommittedTxs'], label='2 Shard Clientled')
ax.plot(df_3_shard_clientled['Time'], df_3_shard_clientled['CumulativeCommittedTxs'], label='3 Shard Clientled')
ax.plot(df_4_shard_clientled['Time'], df_4_shard_clientled['CumulativeCommittedTxs'], label='4 Shard Clientled')
ax.plot(df_6_shard_clientled['Time'], df_6_shard_clientled['CumulativeCommittedTxs'], label='6 Shard Clientled')
ax.plot(df_8_shard_clientled['Time'], df_8_shard_clientled['CumulativeCommittedTxs'], label='8 Shard Clientled')
ax.plot(df_2_shard_shardled['Time'], df_2_shard_shardled['CumulativeCommittedTxs'], label='2 Shard Shardled')
ax.plot(df_3_shard_shardled['Time'], df_3_shard_shardled['CumulativeCommittedTxs'], label='3 Shard Shardled')
ax.plot(df_4_shard_shardled['Time'], df_4_shard_shardled['CumulativeCommittedTxs'], label='4 Shard Shardled')
ax.plot(df_6_shard_shardled['Time'], df_6_shard_shardled['CumulativeCommittedTxs'], label='6 Shard Shardled')
ax.plot(df_8_shard_shardled['Time'], df_8_shard_shardled['CumulativeCommittedTxs'], label='8 Shard Shardled')

# Set the x-label and y-label
ax.set_xlabel('Time')
ax.set_ylabel('Cumulative Committed Transactions')

# Set the title of the plot
ax.set_title('Cumulative Committed Transactions for Different Configurations')

# Add a legend to the plot
ax.legend()

# Show the plot
plt.show()

















# # plot the cumulative committed transactions on the same graph with different colors
# plt.plot(df['CumulativeCommittedCrossShardTxs'], label='Committed Cross Shard Txs')
# plt.plot(df['CumulativeCommittedIntraShardTxs'], label='Committed Intra Shard Txs')
# plt.xlabel('Time (seconds)')
# plt.ylabel('Number of Transactions')
# plt.title('Cumulative Committed Transactions')
# plt.legend()
# plt.show()

# # plot the latency of the transactions, with the average latency as a horizontal line, cross shard in red, intra shard in blue
# plt.plot(df['Latency'], label='Latency')
# plt.axhline(y=average_latency, color='r', linestyle='-', label='Average Latency')
# plt.xlabel('Time (seconds)')
# plt.ylabel('Latency (seconds)')
# plt.title('Latency of Transactions')
# plt.legend()
# plt.show()
