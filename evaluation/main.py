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
# line graph with time on the x axis and the number of blocks created on the y axis
ndf.plot(x='Time', y='BlockHeight', kind='line')
plt.show()