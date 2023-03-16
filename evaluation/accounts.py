import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

lockedAccounts = pd.read_csv('../output/accountLockingLog-shardled-4shards.csv')
unlockedAccounts = pd.read_csv('../output/accountUnlockingLog-shardled-4shards.csv')
# Time, Account

# the time is seconds since the start of the simulation

print(lockedAccounts)
print(unlockedAccounts)