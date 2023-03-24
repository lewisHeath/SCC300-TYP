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

# df_1_shard = pd.read_csv('../output/GlobalStats-CommittedLogger-Clientled-1s10n100c.csv')
# client led
df_2_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/Clientled-CommittedLogger-2s10n50c.csv')
df_4_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/Clientled-CommittedLogger-4s10n100c.csv')
df_8_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/Clientled-CommittedLogger-8s10n100c.csv')
df_16_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/Clientled-CommittedLogger-16s10n200c.csv')
df_32_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/Clientled-CommittedLogger-32s10n400c.csv')
df_64_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/Clientled-CommittedLogger-64s10n800c.csv')
df_128_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/Clientled-CommittedLogger-128s10n2000c.csv')
df_256_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/Clientled-CommittedLogger-256s10n4000c.csv')




# process the data
df_2_shard_clientled = process_csv(df_2_shard_clientled, '2_shard_clientled')
df_4_shard_clientled = process_csv(df_4_shard_clientled, '4_shard_clientled')
df_8_shard_clientled = process_csv(df_8_shard_clientled, '8_shard_clientled')
df_16_shard_clientled = process_csv(df_16_shard_clientled, '16_shard_clientled')
df_32_shard_clientled = process_csv(df_32_shard_clientled, '32_shard_clientled')
df_64_shard_clientled = process_csv(df_64_shard_clientled, '64_shard_clientled')
df_128_shard_clientled = process_csv(df_128_shard_clientled, '128_shard_clientled')
df_256_shard_clientled = process_csv(df_256_shard_clientled, '256_shard_clientled')

# X axis is "Time", Y axis is "CumulativeCommittedTxs"
fig, ax = plt.subplots()

ax.plot(df_2_shard_clientled['Time'], df_2_shard_clientled['CumulativeCommittedTxs'], label='2 Shard Clientled')
ax.plot(df_4_shard_clientled['Time'], df_4_shard_clientled['CumulativeCommittedTxs'], label='4 Shard Clientled')
ax.plot(df_8_shard_clientled['Time'], df_8_shard_clientled['CumulativeCommittedTxs'], label='8 Shard Clientled')
ax.plot(df_16_shard_clientled['Time'], df_16_shard_clientled['CumulativeCommittedTxs'], label='16 Shard Clientled')
ax.plot(df_32_shard_clientled['Time'], df_32_shard_clientled['CumulativeCommittedTxs'], label='32 Shard Clientled')
ax.plot(df_64_shard_clientled['Time'], df_64_shard_clientled['CumulativeCommittedTxs'], label='64 Shard Clientled')
ax.plot(df_128_shard_clientled['Time'], df_128_shard_clientled['CumulativeCommittedTxs'], label='128 Shard Clientled')
ax.plot(df_256_shard_clientled['Time'], df_256_shard_clientled['CumulativeCommittedTxs'], label='256 Shard Clientled')


ax.set_xlabel('Time')
ax.set_ylabel('Cumulative Committed Transactions')


ax.set_title('Cumulative Committed Transactions for Different Configurations')


ax.legend()


plt.show()


fig, ax = plt.subplots()

ax.bar('2 Shard Clientled', df_2_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='2 Shard Clientled')
ax.bar('4 Shard Clientled', df_4_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='4 Shard Clientled')
ax.bar('8 Shard Clientled', df_8_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='8 Shard Clientled')
ax.bar('16 Shard Clientled', df_16_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='16 Shard Clientled')
ax.bar('32 Shard Clientled', df_32_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='32 Shard Clientled')
ax.bar('64 Shard Clientled', df_64_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='64 Shard Clientled')
ax.bar('128 Shard Clientled', df_128_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='128 Shard Clientled')
ax.bar('256 Shard Clientled', df_256_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='256 Shard Clientled')


ax.set_xlabel('Configuration')
ax.set_ylabel('Total Committed Transactions')

ax.set_title('Total Committed Transactions for Different Configurations')

ax.legend()

plt.show()
