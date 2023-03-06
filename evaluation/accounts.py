import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

lockedAccounts = pd.read_csv('../output/accountLockingLog.csv')
# Time, Account
# get the total amount of time each account was locked