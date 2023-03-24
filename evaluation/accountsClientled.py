import pandas as pd
import matplotlib.pyplot as plt


# locked accoun log
lockedAccountsClientled_2shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountLockingLogger-2s10n50c.csv')
lockedAccountsClientled_4shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountLockingLogger-4s10n100c.csv')
lockedAccountsClientled_8shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountLockingLogger-8s10n100c.csv')
lockedAccountsClientled_16shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountLockingLogger-16s10n200c.csv')
lockedAccountsClientled_32shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountLockingLogger-32s10n400c.csv')
lockedAccountsClientled_64shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountLockingLogger-64s10n800c.csv')
lockedAccountsClientled_128shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountLockingLogger-128s10n800c.csv')
lockedAccountsClientled_192shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountLockingLogger-192s10n800c.csv')
lockedAccountsClientled_256shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountLockingLogger-256s10n800c.csv')

# unlocked account log
unlockedAccountsClientled_2shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountUnlockingLogger-2s10n50c.csv')
unlockedAccountsClientled_4shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountUnlockingLogger-4s10n100c.csv')
unlockedAccountsClientled_8shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountUnlockingLogger-8s10n100c.csv')
unlockedAccountsClientled_16shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountUnlockingLogger-16s10n200c.csv')
unlockedAccountsClientled_32shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountUnlockingLogger-32s10n400c.csv')
unlockedAccountsClientled_64shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountUnlockingLogger-64s10n800c.csv')
unlockedAccountsClientled_128shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountUnlockingLogger-128s10n800c.csv')
unlockedAccountsClientled_192shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountUnlockingLogger-192s10n800c.csv')
unlockedAccountsClientled_256shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/ClientledAccountUnlockingLogger-256s10n800c.csv')

# aborted logs to filter the results
abortedTransactionsClientled_2shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-AbortedLogger-2s10n50c.csv')
abortedTransactionsClientled_4shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-AbortedLogger-4s10n100c.csv')
abortedTransactionsClientled_8shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-AbortedLogger-8s10n100c.csv')
abortedTransactionsClientled_16shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-AbortedLogger-16s10n200c.csv')
abortedTransactionsClientled_32shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-AbortedLogger-32s10n400c.csv')
abortedTransactionsClientled_64shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-AbortedLogger-64s10n800c.csv')
abortedTransactionsClientled_128shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-AbortedLogger-128s10n800c.csv')
abortedTransactionsClientled_192shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-AbortedLogger-192s10n800c.csv')
abortedTransactionsClientled_256shards = pd.read_csv('../output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-AbortedLogger-256s10n800c.csv')



def get_lock_stats(locked_df, unlocked_df):
    locked_df = locked_df.sort_values('Time').reset_index(drop=True)
    unlocked_df = unlocked_df.sort_values('Time').reset_index(drop=True)

    lock_durations = pd.DataFrame(columns=['Account', 'Lock_duration'])

    # Iterate through the locked accounts
    for index, row in locked_df.iterrows():
        account = row['Account']
        lock_time = row['Time']

        unlock_rows = unlocked_df[(unlocked_df['Account'] == account) & (unlocked_df['Time'] > lock_time)]

        if not unlock_rows.empty:
            unlock_row = unlock_rows.iloc[0]
            unlock_time = unlock_row['Time']

            # Remove the used unlock event from the unlocked_df
            unlocked_df = unlocked_df.drop(unlock_row.name)

            # Calculate the lock duration and append it to the lock_durations dataframe
            lock_duration = unlock_time - lock_time
            new_row = pd.DataFrame({'Account': [account], 'Lock_duration': [lock_duration]})
            lock_durations = pd.concat([lock_durations, new_row], ignore_index=True)


    # Group by account and calculate the total and average lock duration
    lock_stats = lock_durations.groupby('Account')['Lock_duration'].agg(['sum', 'mean']).reset_index()
    lock_stats.rename(columns={'sum': 'Total_time_locked', 'mean': 'Average_time_locked'}, inplace=True)

    return lock_stats

# Get the lock stats for the clientled simulation
lock_stats_clientled_2shards = get_lock_stats(lockedAccountsClientled_2shards, unlockedAccountsClientled_2shards)
lock_stats_clientled_4shards = get_lock_stats(lockedAccountsClientled_4shards, unlockedAccountsClientled_4shards)
lock_stats_clientled_8shards = get_lock_stats(lockedAccountsClientled_8shards, unlockedAccountsClientled_8shards)
lock_stats_clientled_16shards = get_lock_stats(lockedAccountsClientled_16shards, unlockedAccountsClientled_16shards)
lock_stats_clientled_32shards = get_lock_stats(lockedAccountsClientled_32shards, unlockedAccountsClientled_32shards)
lock_stats_clientled_64shards = get_lock_stats(lockedAccountsClientled_64shards, unlockedAccountsClientled_64shards)
lock_stats_clientled_128shards = get_lock_stats(lockedAccountsClientled_128shards, unlockedAccountsClientled_128shards)
lock_stats_clientled_192shards = get_lock_stats(lockedAccountsClientled_192shards, unlockedAccountsClientled_192shards)
lock_stats_clientled_256shards = get_lock_stats(lockedAccountsClientled_256shards, unlockedAccountsClientled_256shards)

average_lock_time_2shards = lock_stats_clientled_2shards['Average_time_locked'].mean()
average_lock_time_4shards = lock_stats_clientled_4shards['Average_time_locked'].mean()
average_lock_time_8shards = lock_stats_clientled_8shards['Average_time_locked'].mean()
average_lock_time_16shards = lock_stats_clientled_16shards['Average_time_locked'].mean()
average_lock_time_32shards = lock_stats_clientled_32shards['Average_time_locked'].mean()
average_lock_time_64shards = lock_stats_clientled_64shards['Average_time_locked'].mean()
average_lock_time_128shards = lock_stats_clientled_128shards['Average_time_locked'].mean()
average_lock_time_192shards = lock_stats_clientled_192shards['Average_time_locked'].mean()
average_lock_time_256shards = lock_stats_clientled_256shards['Average_time_locked'].mean()

# Plot the average lock time
plt.figure(figsize=(10, 6))
plt.bar(range(1, 10), [average_lock_time_2shards, average_lock_time_4shards, average_lock_time_8shards, average_lock_time_16shards, average_lock_time_32shards, average_lock_time_64shards, average_lock_time_128shards, average_lock_time_192shards, average_lock_time_256shards])
plt.xticks(range(1, 10), [2, 4, 8, 16, 32, 64, 128, 192, 256], rotation=90)
plt.xlabel('Number of shards')
plt.ylabel('Average lock time (s)')
plt.title('Average lock time for different number of shards ( client led )')
plt.tight_layout()
# plt.savefig('clientled/exponent1.2/seed1/Clientled-AverageLockTime.png')
plt.show()

def plot_graph(x, y, xlabel, ylabel, title, filename, n=10):
    plt.figure(figsize=(10, 6))
    plt.bar(range(1, len(x) + 1), y)
    
    num_xticks = min(n, len(x))
    xtick_spacing = len(x) // num_xticks
    xticks = range(1, len(x) + 1, xtick_spacing)
    
    plt.xticks(xticks, rotation=90)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.title(title)
    plt.tight_layout()
    plt.savefig(filename)
    plt.show()

# # Plotting total time locked
# plot_graph(
#     lock_stats['Account'],
#     lock_stats['Total_time_locked'],
#     'Account',
#     'Total Time Locked',
#     'Total Time Locked per Account',
#     'total_time_locked.png'
# )

# # Plotting average time locked
# plot_graph(
#     lock_stats['Account'],
#     lock_stats['Average_time_locked'],
#     'Account',
#     'Average Time Locked',
#     'Average Time Locked per Account',
#     'average_time_locked.png'
# )