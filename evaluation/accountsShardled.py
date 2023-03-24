import pandas as pd
import matplotlib.pyplot as plt

#clients go 50,100,100,200,400,800,800,800,800
#shards go 2,4,8,16,32,64,128,192,256

# locked accoun log
lockedAccountsShardled_2shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountLockingLogger-2s10n50c.csv')
lockedAccountsShardled_4shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountLockingLogger-4s10n100c.csv')
lockedAccountsShardled_8shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountLockingLogger-8s10n100c.csv')
lockedAccountsShardled_16shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountLockingLogger-16s10n200c.csv')
lockedAccountsShardled_32shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountLockingLogger-32s10n400c.csv')
lockedAccountsShardled_64shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountLockingLogger-64s10n800c.csv')
lockedAccountsShardled_128shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountLockingLogger-128s10n800c.csv')
lockedAccountsShardled_192shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountLockingLogger-192s10n800c.csv')
lockedAccountsShardled_256shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountLockingLogger-256s10n800c.csv')

# unlocked account log
unlockedAccountsShardled_2shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountUnlockingLogger-2s10n50c.csv')
unlockedAccountsShardled_4shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountUnlockingLogger-4s10n100c.csv')
unlockedAccountsShardled_8shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountUnlockingLogger-8s10n100c.csv')
unlockedAccountsShardled_16shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountUnlockingLogger-16s10n200c.csv')
unlockedAccountsShardled_32shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountUnlockingLogger-32s10n400c.csv')
unlockedAccountsShardled_64shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountUnlockingLogger-64s10n800c.csv')
unlockedAccountsShardled_128shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountUnlockingLogger-128s10n800c.csv')
unlockedAccountsShardled_192shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountUnlockingLogger-192s10n800c.csv')
unlockedAccountsShardled_256shards = pd.read_csv('../output/tenNodesSimulations/shardled/exponent1.2/seed1/ShardledAccountUnlockingLogger-256s10n800c.csv')


def get_lock_stats(locked_df, unlocked_df):
    locked_df = locked_df.sort_values('Time').reset_index(drop=True)
    unlocked_df = unlocked_df.sort_values('Time').reset_index(drop=True)

    lock_durations = pd.DataFrame(columns=['Account', 'Lock_duration'])

    for index, row in locked_df.iterrows():
        account = row['Account']
        lock_time = row['Time']

        unlock_rows = unlocked_df[(unlocked_df['Account'] == account) & (unlocked_df['Time'] > lock_time)]

        if not unlock_rows.empty:
            unlock_row = unlock_rows.iloc[0]
            unlock_time = unlock_row['Time']

            unlocked_df = unlocked_df.drop(unlock_row.name)
            lock_duration = unlock_time - lock_time
            new_row = pd.DataFrame({'Account': [account], 'Lock_duration': [lock_duration]})
            lock_durations = pd.concat([lock_durations, new_row], ignore_index=True)

    lock_stats = lock_durations.groupby('Account')['Lock_duration'].agg(['sum', 'mean']).reset_index()
    lock_stats.rename(columns={'sum': 'Total_time_locked', 'mean': 'Average_time_locked'}, inplace=True)

    return lock_stats

# Get the lock stats for the Shardled simulation
lock_stats_shardled_2shards = get_lock_stats(lockedAccountsShardled_2shards, unlockedAccountsShardled_2shards)
lock_stats_shardled_4shards = get_lock_stats(lockedAccountsShardled_4shards, unlockedAccountsShardled_4shards)
lock_stats_shardled_8shards = get_lock_stats(lockedAccountsShardled_8shards, unlockedAccountsShardled_8shards)
lock_stats_shardled_16shards = get_lock_stats(lockedAccountsShardled_16shards, unlockedAccountsShardled_16shards)
lock_stats_shardled_32shards = get_lock_stats(lockedAccountsShardled_32shards, unlockedAccountsShardled_32shards)
lock_stats_shardled_64shards = get_lock_stats(lockedAccountsShardled_64shards, unlockedAccountsShardled_64shards)
lock_stats_shardled_128shards = get_lock_stats(lockedAccountsShardled_128shards, unlockedAccountsShardled_128shards)
lock_stats_shardled_192shards = get_lock_stats(lockedAccountsShardled_192shards, unlockedAccountsShardled_192shards)
lock_stats_shardled_256shards = get_lock_stats(lockedAccountsShardled_256shards, unlockedAccountsShardled_256shards)

average_lock_time_2shards = lock_stats_shardled_2shards['Average_time_locked'].mean()
average_lock_time_4shards = lock_stats_shardled_4shards['Average_time_locked'].mean()
average_lock_time_8shards = lock_stats_shardled_8shards['Average_time_locked'].mean()
average_lock_time_16shards = lock_stats_shardled_16shards['Average_time_locked'].mean()
average_lock_time_32shards = lock_stats_shardled_32shards['Average_time_locked'].mean()
average_lock_time_64shards = lock_stats_shardled_64shards['Average_time_locked'].mean()
average_lock_time_128shards = lock_stats_shardled_128shards['Average_time_locked'].mean()
average_lock_time_192shards = lock_stats_shardled_192shards['Average_time_locked'].mean()
average_lock_time_256shards = lock_stats_shardled_256shards['Average_time_locked'].mean()

# Plot the average lock time
plt.figure(figsize=(10, 6))
plt.bar(range(1, 10), [average_lock_time_2shards, average_lock_time_4shards, average_lock_time_8shards, average_lock_time_16shards, average_lock_time_32shards, average_lock_time_64shards, average_lock_time_128shards, average_lock_time_192shards, average_lock_time_256shards])
plt.xticks(range(1, 10), [2, 4, 8, 16, 32, 64, 128, 192, 256], rotation=90)
plt.xlabel('Number of shards')
plt.ylabel('Average lock time (s)')
plt.title('Average lock time for different number of shards ( shard led )')
plt.tight_layout()
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