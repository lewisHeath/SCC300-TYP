import pandas as pd
import matplotlib.pyplot as plt

# import the data
df = pd.read_csv('../output/sharded-pbft-coordination-messages-log.csv')
# count the different MessageTypes
df = df.groupby(['MessageType']).count()
# plot the data
df.plot(kind='bar')
# make the x axis labels horizontal
plt.xticks(rotation=0)
plt.show()