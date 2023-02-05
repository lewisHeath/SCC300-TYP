import pandas as pd
import matplotlib.pyplot as plt

# import the data
# df = pd.read_csv('../output/sharded-pbft-coordination-messages-log.csv')
# # count the different MessageTypes
# df = df.groupby(['MessageType']).count()
# # plot the data
# df.plot(kind='bar')
# # make the x axis labels horizontal
# plt.xticks(rotation=0)
# plt.show()

# import ../output/sharded-pbft-block-log.csv
ndf = pd.read_csv('../output/sharded-pbft-block-log.csv')
# only keep data which NodeID is the same as BlockCreator
ndf = ndf[ndf['NodeID'] == ndf['BlockCreator']]
# separate the data by the shard column
ndf = ndf.groupby(['Shard']).count()
# shard_block_count = ndf['BlockCreator']

df = pd.read_csv('../output/sharded-pbft-block-log.csv')
# this data contains the event from where a block was added to a chain, it has the node id that is emitting the event and the block creator and shard number
# we only want the data where the node id is the same as the block creator
df = df[df['NodeID'] == df['BlockCreator']]
# i want to plot a line graph where the x axis is the Time and the y axis is the block height, i want to separate the data by the shard number and have a different line for each shard,  i want to plot the data for each shard in a different color

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