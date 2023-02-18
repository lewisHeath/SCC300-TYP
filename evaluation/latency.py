import pandas as pd
import matplotlib.pyplot as plt

# import the data
df = pd.read_csv('../output/TxCommittedLog.csv')

# Time, Tx Hash, Time Tx Created
# Latency = Time - Time Tx Created 
df['Latency'] = df['Time'] - df['TxCreationTime']
print(df)
average_latency = df['Latency'].mean()
print('Average Latency: ', round(average_latency, 2) ,'seconds')

# print the length of the dataframe
print('Committed Txs: ', len(df))