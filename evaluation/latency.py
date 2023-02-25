import pandas as pd
import matplotlib.pyplot as plt

# import the data
df = pd.read_csv('../output/TxCommittedLog4.csv')

# Time, Tx Hash, Time Tx Created
# Latency = Time - Time Tx Created 
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

# create a cumulative committed transactions
df['CumulativeCommittedTxs'] = df.index + 1

# create cumulative committed cross shard transactions
df['CumulativeCommittedCrossShardTxs'] = df['CrossShard'].cumsum()
# create cumulative committed intra shard transactions
df['CumulativeCommittedIntraShardTxs'] = df['CumulativeCommittedTxs'] - df['CumulativeCommittedCrossShardTxs']
print(df)

# plot the cumulative committed transactions on the same graph with different colors
plt.plot(df['CumulativeCommittedCrossShardTxs'], label='Committed Cross Shard Txs')
plt.plot(df['CumulativeCommittedIntraShardTxs'], label='Committed Intra Shard Txs')
plt.xlabel('Time (seconds)')
plt.ylabel('Number of Transactions')
plt.title('Cumulative Committed Transactions')
plt.legend()
plt.show()

# plot the latency of the transactions, with the average latency as a horizontal line, cross shard in red, intra shard in blue
plt.plot(df['Latency'], label='Latency')
plt.axhline(y=average_latency, color='r', linestyle='-', label='Average Latency')
plt.xlabel('Time (seconds)')
plt.ylabel('Latency (seconds)')
plt.title('Latency of Transactions')
plt.legend()
plt.show()