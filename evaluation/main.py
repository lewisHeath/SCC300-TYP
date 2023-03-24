import pandas as pd
import matplotlib.pyplot as plt


df = pd.read_csv('../output/sharded-pbft-block-log.csv')
# this data contains the event from where a block was added to a chain, it has the node id that is emitting the event and the block creator and shard number
# we only want the data where the node id is the same as the block creator
df = df[df['NodeID'] == df['BlockCreator']]
# i want to plot a line graph where the x axis is the Time and the y axis is the block height, i want to separate the data by the shard number and have a different line for each shard,  i want to plot the data for each shard in a different color


# get the average transactions in each block, the Feild is called TransactionsInBlock
transactions_in_block = df['TransactionsInBlock'].mean()
print('Average Transactions in Block: ', transactions_in_block)
# get the amount of blocks
block_count = df['BlockCreator'].count()
print('Block Count: ', block_count)
# get the average time between blocks
average_time_between_blocks = df['Time'].diff().mean()
print('Average Time Between Blocks: ', average_time_between_blocks)


# separate the data by the shard column
df = df.groupby(['Shard'])
# plot the data
for name, group in df:
    plt.plot(group['Time'], group['BlockHeight'], label=name)
# label the axes
plt.xlabel('Time')
plt.ylabel('Block Height')
plt.title('Block Height over Time In Each Shard')
plt.legend()
plt.show()
