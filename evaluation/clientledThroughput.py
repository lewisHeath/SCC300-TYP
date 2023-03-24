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

df_1_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-1s10n50c.csv')
# import the data
df_2_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-2s10n50c.csv')
df_4_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-4s10n100c.csv')
df_8_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-8s10n100c.csv')
df_16_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-16s10n200c.csv')
df_32_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-32s10n400c.csv')
df_64_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-64s10n800c.csv')
df_128_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-128s10n800c.csv')
df_192_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-192s10n800c.csv')
df_256_shard_clientled = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-256s10n800c.csv')




# process the data
df_1_shard_clientled = process_csv(df_1_shard_clientled, '1 Shard Clientled')
df_2_shard_clientled = process_csv(df_2_shard_clientled, '2 Shard Clientled')
df_4_shard_clientled = process_csv(df_4_shard_clientled, '4 Shard Clientled')
df_8_shard_clientled = process_csv(df_8_shard_clientled, '8 Shard Clientled')
df_16_shard_clientled = process_csv(df_16_shard_clientled, '16 Shard Clientled')
df_32_shard_clientled = process_csv(df_32_shard_clientled, '32 Shard Clientled')
df_64_shard_clientled = process_csv(df_64_shard_clientled, '64 Shard Clientled')
df_128_shard_clientled = process_csv(df_128_shard_clientled, '128 Shard Clientled')
df_192_shard_clientled = process_csv(df_192_shard_clientled, '192 Shard Clientled')
df_256_shard_clientled = process_csv(df_256_shard_clientled, '256 Shard Clientled')


# Create a figure and axis object
fig, ax = plt.subplots()

# Plot the cumulative committed transactions for each file on the same axis
ax.plot(df_1_shard_clientled['Time'], df_1_shard_clientled['CumulativeCommittedTxs'], label='1 Shard Clientled')
ax.plot(df_2_shard_clientled['Time'], df_2_shard_clientled['CumulativeCommittedTxs'], label='2 Shards Clientled')
ax.plot(df_4_shard_clientled['Time'], df_4_shard_clientled['CumulativeCommittedTxs'], label='4 Shards Clientled')
ax.plot(df_8_shard_clientled['Time'], df_8_shard_clientled['CumulativeCommittedTxs'], label='8 Shards Clientled')
ax.plot(df_16_shard_clientled['Time'], df_16_shard_clientled['CumulativeCommittedTxs'], label='16 Shards Clientled')
ax.plot(df_32_shard_clientled['Time'], df_32_shard_clientled['CumulativeCommittedTxs'], label='32 Shards Clientled')
ax.plot(df_64_shard_clientled['Time'], df_64_shard_clientled['CumulativeCommittedTxs'], label='64 Shards Clientled')
ax.plot(df_128_shard_clientled['Time'], df_128_shard_clientled['CumulativeCommittedTxs'], label='128 Shards Clientled')
ax.plot(df_192_shard_clientled['Time'], df_192_shard_clientled['CumulativeCommittedTxs'], label='192 Shards Clientled')
ax.plot(df_256_shard_clientled['Time'], df_256_shard_clientled['CumulativeCommittedTxs'], label='256 Shards Clientled')
# ax.plot(df_128_shard_clientled['Time'], df_128_shard_clientled['CumulativeCommittedTxs'], label='128 Shard clientled')

# ax.plot(df_128_shard_clientled['Time'], df_128_shard_clientled['CumulativeCommittedTxs'], label='128 Shard Clientled')
# ax.plot(df_256_shard_clientled['Time'], df_256_shard_clientled['CumulativeCommittedTxs'], label='256 Shard Clientled')



# Set the x-label and y-label
ax.set_xlabel('Time')
ax.set_ylabel('Cumulative Committed Transactions')

# Set the title of the plot
ax.set_title('Cumulative Committed Transactions for Different Configurations')

# Add a legend to the plot
ax.legend()

# Show the plot
plt.show()

# plot the total committed transactions as a bar chart 
# Create a figure and axis object
fig, ax = plt.subplots()

# Plot the cumulative committed transactions for each file on the same axis
ax.bar('1 Shard', df_1_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='1 Shard')
ax.bar('2 Shards', df_2_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='2 Shards')
ax.bar('4 Shards', df_4_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='4 Shards')
ax.bar('8 Shards', df_8_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='8 Shards')
ax.bar('16 Shards', df_16_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='16 Shards')
ax.bar('32 Shards', df_32_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='32 Shards')
ax.bar('64 Shards', df_64_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='64 Shards')
ax.bar('128 Shards', df_128_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='128 Shards')
ax.bar('192 Shards', df_192_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='192 Shards')
ax.bar('256 Shards', df_256_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='256 Shards')
# ax.bar('128 Shard clientled', df_128_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='128 Shard clientled')
# ax.bar('128 Shard Clientled', df_128_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='128 Shard Clientled')
# ax.bar('256 Shard Clientled', df_256_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='256 Shard Clientled')

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

ax.bar('1 Shard', df_1_shard_clientled['Latency'].mean(), label='1 Shard')
ax.bar('2 Shards', df_2_shard_clientled['Latency'].mean(), label='2 Shards')
ax.bar('4 Shards', df_4_shard_clientled['Latency'].mean(), label='4 Shards')
ax.bar('8 Shards', df_8_shard_clientled['Latency'].mean(), label='8 Shards')
ax.bar('16 Shards', df_16_shard_clientled['Latency'].mean(), label='16 Shards')
ax.bar('32 Shards', df_32_shard_clientled['Latency'].mean(), label='32 Shards')
ax.bar('64 Shards', df_64_shard_clientled['Latency'].mean(), label='64 Shards')
ax.bar('128 Shards', df_128_shard_clientled['Latency'].mean(), label='128 Shards')
ax.bar('192 Shards', df_192_shard_clientled['Latency'].mean(), label='192 Shards')
ax.bar('256 Shards', df_256_shard_clientled['Latency'].mean(), label='256 Shards')

ax.set_xlabel('Configuration')
ax.set_ylabel('Latency')

ax.set_title('Latency for Different Configurations')

plt.show()


import numpy as np

data = np.array([
    [df_1_shard_clientled['Latency'].mean(), df_1_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_1_shard_clientled) / df_1_shard_clientled['Time'].iloc[-1]],
    [df_2_shard_clientled['Latency'].mean(), df_2_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_2_shard_clientled) / df_2_shard_clientled['Time'].iloc[-1]],
    [df_4_shard_clientled['Latency'].mean(), df_4_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_4_shard_clientled) / df_4_shard_clientled['Time'].iloc[-1]],
    [df_8_shard_clientled['Latency'].mean(), df_8_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_8_shard_clientled) / df_8_shard_clientled['Time'].iloc[-1]],
    [df_16_shard_clientled['Latency'].mean(), df_16_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_16_shard_clientled) / df_16_shard_clientled['Time'].iloc[-1]],
    [df_32_shard_clientled['Latency'].mean(), df_32_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_32_shard_clientled) / df_32_shard_clientled['Time'].iloc[-1]],
    [df_64_shard_clientled['Latency'].mean(), df_64_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_64_shard_clientled) / df_64_shard_clientled['Time'].iloc[-1]],
    [df_128_shard_clientled['Latency'].mean(), df_128_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_128_shard_clientled) / df_128_shard_clientled['Time'].iloc[-1]],
    [df_192_shard_clientled['Latency'].mean(), df_192_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_192_shard_clientled) / df_192_shard_clientled['Time'].iloc[-1]],
    [df_256_shard_clientled['Latency'].mean(), df_256_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_256_shard_clientled) / df_256_shard_clientled['Time'].iloc[-1]]
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
