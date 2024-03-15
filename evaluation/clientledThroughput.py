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
df_2_shard_clientled = pd.read_csv('output/tenNodesSimulations/clientled/exponent0.4/seed1/Clientled-CommittedLogger-6s10n800c.csv')
df_4_shard_clientled = pd.read_csv('output/tenNodesSimulations/clientled/MainShard/exponent0.4/seed1/Clientled-CommittedLogger-6s10n800c.csv')
df_8_shard_clientled = pd.read_csv('output/tenNodesSimulations/clientled/NewAccounts/exponent0.4/seed1/Clientled-CommittedLogger-6s10n800c.csv')





# process the data

df_2_shard_clientled = process_csv(df_2_shard_clientled, '6 Shard Clientled')
df_4_shard_clientled = process_csv(df_4_shard_clientled, '6 Shard Clientled')
df_8_shard_clientled = process_csv(df_8_shard_clientled, '6 Shard Clientled')


# Create a figure and axis object
fig, ax = plt.subplots()

# Plot the cumulative committed transactions for each file on the same axis
ax.plot(['Time'], ['CumulativeCommittedTxs'], label='1 Shard Clientled')
ax.plot(df_2_shard_clientled['Time'], df_2_shard_clientled['CumulativeCommittedTxs'], label='none')
ax.plot(df_4_shard_clientled['Time'], df_4_shard_clientled['CumulativeCommittedTxs'], label='Main shard')
ax.plot(df_8_shard_clientled['Time'], df_8_shard_clientled['CumulativeCommittedTxs'], label='New accounts')

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
ax.bar('1 Shard', ['CumulativeCommittedTxs'].iloc[-1], label='1 Shard')
ax.bar('2 Shards', df_2_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='2 Shards')
ax.bar('4 Shards', df_4_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='4 Shards')
ax.bar('8 Shards', df_8_shard_clientled['CumulativeCommittedTxs'].iloc[-1], label='8 Shards')

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
ax.bar('2 Shards', df_2_shard_clientled['Latency'].mean(), label='2 Shards')
ax.bar('4 Shards', df_4_shard_clientled['Latency'].mean(), label='4 Shards')
ax.bar('8 Shards', df_8_shard_clientled['Latency'].mean(), label='8 Shards')

ax.set_xlabel('Configuration')
ax.set_ylabel('Latency')

ax.set_title('Latency for Different Configurations')

plt.show()


import numpy as np

data = np.array([
    [['Latency'].mean(), ['CumulativeCommittedTxs'].iloc[-1], len() / ['Time'].iloc[-1]],
    [df_2_shard_clientled['Latency'].mean(), df_2_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_2_shard_clientled) / df_2_shard_clientled['Time'].iloc[-1]],
    [df_4_shard_clientled['Latency'].mean(), df_4_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_4_shard_clientled) / df_4_shard_clientled['Time'].iloc[-1]],
    [df_8_shard_clientled['Latency'].mean(), df_8_shard_clientled['CumulativeCommittedTxs'].iloc[-1], len(df_8_shard_clientled) / df_8_shard_clientled['Time'].iloc[-1]],

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
