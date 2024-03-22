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

    # print the transactions per second
    print('Committed Txs per second: ', round(len(df)/df['Time'].iloc[-1], 2))

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
    print("")
    return df


# import the data
df_mainshard = pd.read_csv('output/tenNodesSimulations/clientled/MainShard/WithConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-CommittedLogger-32s6n300c.csv')
df_DataStructure = pd.read_csv('output/tenNodesSimulations/clientled/DataStructure/withoutConsensus/DynamicShardAssignment/exponent1.4/seed1/Clientled-CommittedLogger-32s6n300c.csv')
df_regular = pd.read_csv('output/tenNodesSimulations/clientled/withoutConsensus/regular/exponent1.4/seed1/Clientled-CommittedLogger-32s6n300c.csv')





# process the data

df_mainshard = process_csv(df_mainshard, 'Main Shard Policy')
df_DataStructure = process_csv(df_DataStructure, 'Transaction based policy')
df_regular = process_csv(df_regular, 'regular')


# Create a figure and axis object
fig, ax = plt.subplots()

# Plot the cumulative committed transactions for each file on the same axis
ax.plot(['Time'], ['CumulativeCommittedTxs'])
ax.plot(df_mainshard['Time'], df_mainshard['CumulativeCommittedTxs'], label='Main Shard')
ax.plot(df_DataStructure['Time'], df_DataStructure['CumulativeCommittedTxs'], label='Transaction Based')
ax.plot(df_regular['Time'], df_regular['CumulativeCommittedTxs'], label='regular')

# ax.plot(df_128_shard_clientled['Time'], df_128_shard_clientled['CumulativeCommittedTxs'], label='128 Shard Clientled')
# ax.plot(df_256_shard_clientled['Time'], df_256_shard_clientled['CumulativeCommittedTxs'], label='256 Shard Clientled')



# Set the x-label and y-label
ax.set_xlabel('Time')
ax.set_ylabel('Cumulative Committed Transactions')

# Set the title of the plot
ax.set_title('Throughput for different Policies (with Dynamic shard assignment and Consensus) compared to regular')

# Add a legend to the plot
ax.legend()

# Show the plot
plt.show()

# plot the total committed transactions as a bar chart 
# Create a figure and axis object
fig, ax = plt.subplots()

# Plot the cumulative committed transactions for each file on the same axis
ax.bar('1 Shard', ['CumulativeCommittedTxs'].iloc[-1], label='1 Shard')
ax.bar('2 Shards', df_mainshard['CumulativeCommittedTxs'].iloc[-1], label='2 Shards')
ax.bar('4 Shards', df_DataStructure['CumulativeCommittedTxs'].iloc[-1], label='4 Shards')
ax.bar('8 Shards', df_regular['CumulativeCommittedTxs'].iloc[-1], label='8 Shards')

# Set the x-label and y-label
ax.set_xlabel('Configuration')
ax.set_ylabel('Total Committed Transactions')

# Set the title of the plot
ax.set_title('Total Committed Transactions for Different Configurations ( Clientled )')

# Add a legend to the plot
ax.legend()

# Show the plot
plt.show()


# latencies

fig, ax = plt.subplots()

ax.bar('1 Shard', ['Latency'].mean(), label='1 Shard')
ax.bar('2 Shards', df_mainshard['Latency'].mean(), label='2 Shards')
ax.bar('4 Shards', df_DataStructure['Latency'].mean(), label='4 Shards')
ax.bar('8 Shards', df_regular['Latency'].mean(), label='8 Shards')

ax.set_xlabel('Configuration')
ax.set_ylabel('Latency')

ax.set_title('Latency for Different Configurations')

plt.show()


import numpy as np

data = np.array([
    [['Latency'].mean(), ['CumulativeCommittedTxs'].iloc[-1], len() / ['Time'].iloc[-1]],
    [df_mainshard['Latency'].mean(), df_mainshard['CumulativeCommittedTxs'].iloc[-1], len(df_mainshard) / df_mainshard['Time'].iloc[-1]],
    [df_DataStructure['Latency'].mean(), df_DataStructure['CumulativeCommittedTxs'].iloc[-1], len(df_DataStructure) / df_DataStructure['Time'].iloc[-1]],
    [df_regular['Latency'].mean(), df_regular['CumulativeCommittedTxs'].iloc[-1], len(df_regular) / df_regular['Time'].iloc[-1]],

])

fig, ax = plt.subplots()

ax.set_axis_off()
ax.set_xlim(0, 1)
ax.set_ylim(0, 1)

columns = ['Average Latency', 'Total Committed Txs', 'Txs per Second']
rows = ['1 Shard', '2 Shards', '4 Shards', '8 Shards', '16 Shards', '32 Shards', '64 Shards', '128 Shards', '192 Shards', '256 Shards']

table = ax.table(cellText=data, colLabels=columns, rowLabels=rows, cellLoc='center', loc='center')

table.auto_set_font_size(False)
table.set_fontsize(14)
table.scale(1, 1.5)

plt.title('Summary Table Client Led')
plt.savefig('summary_table.png', bbox_inches='tight')

plt.show()
