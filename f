[33mcommit 9052c2e7041257eb252fadb31b3671c174dd398e[m[33m ([m[1;36mHEAD -> [m[1;32mfirst[m[33m, [m[1;31morigin/first[m[33m, [m[1;32mmain[m[33m)[m
Author: amrani350 <ali.amrani350@gmail.com>
Date:   Fri Dec 29 17:28:39 2023 +0100

    Added a new migration event class, also added an account migration mechanism that migrates accounts to a random shard using a threshold, will be adding a more efficient policy for this soon

[33mcommit be184373cd3cb66a206d7ddf5516e66926d98bc3[m[33m ([m[1;31morigin/main[m[33m, [m[1;31morigin/HEAD[m[33m)[m
Merge: f6c431f e0bd0cf
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Tue May 16 17:14:54 2023 +0100

    Merge pull request #7 from lewisHeath/Final
    
    Submission version of code

[33mcommit e0bd0cfcc162bb39ed6ba5fdaaad3234727cfeca[m[33m ([m[1;31morigin/Final[m[33m)[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Tue May 16 17:14:09 2023 +0100

    Submission version of code

[33mcommit f6c431fd543bd6fb2f8c5467f7371155c3c2f2d3[m
Merge: 2f6d7df 32af58d
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Fri Mar 24 12:01:36 2023 +0000

    Merge pull request #6 from lewisHeath/fix
    
    DONE

[33mcommit 2f6d7df35adfa69c5bc0012f3b7d00a0cb9ed61d[m
Merge: a3299fa ad3a6f6
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Fri Mar 24 12:01:03 2023 +0000

    Merge pull request #5 from lewisHeath/sharding
    
    Sharding

[33mcommit 32af58da70d4eee9bdafd68ab79983045d8ce6da[m[33m ([m[1;31morigin/fix[m[33m)[m
Author: lewisHeath <email@lewisheath.co.uk>
Date:   Fri Mar 24 11:47:53 2023 +0000

    DONE

[33mcommit a1cabcb44b80247ccd24267cd5df3f1940012425[m
Merge: 524c2d3 40863da
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Thu Mar 16 10:52:26 2023 +0000

    Merge pull request #4 from lewisHeath/workaround
    
    Workaround

[33mcommit 40863da57a777ba16a1e00da8b38ccde184635a7[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Thu Mar 16 10:50:28 2023 +0000

    fixed shard led bug and generated more results

[33mcommit 26e1e678ffe3525dd3757d1985e42553e660e0cd[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Mon Mar 6 16:59:34 2023 +0000

    account locking event

[33mcommit ceafd853ecd623e4ddd7a8ee3ef9c7a41e273dbf[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Thu Mar 2 16:02:53 2023 +0000

    different distribution for getting random account

[33mcommit 338869052aad166b9023cf592cc112fd0f80bd50[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Sat Feb 25 20:54:25 2023 +0000

    created a workaround for client led, shard led soon

[33mcommit 524c2d379131760509a8aaa8cb984ab8aa7cbed7[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Sun Feb 19 17:51:27 2023 +0000

    upload from pc

[33mcommit 1df6357a10a00df0d9aa2f04af6ae01f9638288b[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Sat Feb 18 16:49:52 2023 +0000

    added Txs committed logger and corresponding python code

[33mcommit 3517906931b5c4ac7fe00c70175a040f87decfdb[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Sat Feb 18 14:42:28 2023 +0000

    more cross shard txs committed
    
    more cross shard txs are committed now that i only send the txs to the first node in a shard and let them relay each one, also i changed the network to lan to see if it had any impact on the performance for testing

[33mcommit dd2f5f28b52d6dc54d917e16b917c546a90136e7[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Fri Feb 17 16:35:50 2023 +0000

    view numbers consistent, still does not commit many txs

[33mcommit f6ac44e54aa5267e7bb1e2a1a6fb2e5c09e2e619[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Sun Feb 12 17:45:44 2023 +0000

    pc upload

[33mcommit 890a8044335164b46c4511c03205255fbce5db08[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Wed Feb 8 19:03:56 2023 +0000

    started on ordering of pre-prepares
    
    I am having a problem where the view numbers are getting messed up by what i think is nodes using the same transactions twice

[33mcommit d25c02253ac5ef6566df0178e4a5ca0c4d844093[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Wed Feb 8 13:28:17 2023 +0000

    started on fix

[33mcommit ad3a6f65c89b039d6413a4ea5a4a9a840a251b8c[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Mon Feb 6 18:43:48 2023 +0000

    changed 2f to f + 1, added from node to coordination message
    
    I need to implement the leader in the cross shard tx protocol, because right now, hardly any go through, also the block height in the python code is bugged

[33mcommit 1a0ff18f2109cf95f81dd107de1aa65efbc969b7[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Sun Feb 5 14:37:21 2023 +0000

    added ShardedBlockConfirmationEvent and added python code

[33mcommit 44138d2ef671070d90a7731be2ac9a935a9cc433[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Thu Feb 2 14:19:35 2023 +0000

    added block confirmation event

[33mcommit 5065e96ea5a404431d1ec82c89ad2ebac91821cb[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Fri Jan 27 15:22:42 2023 +0000

    Txs with more than 2 accounts
    
    I think Txs are now working with more than 2 accounts, also I added some python code to plot the amount of each coordination message, these look correct so far

[33mcommit fafc1de90204c735898d69f9207d3467ff4d42b5[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Sat Jan 21 15:14:51 2023 +0000

    Intra shard txs can be committed

[33mcommit b77a86bef44080525ceaa55aaaa38c05cfd59f9c[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Fri Jan 13 14:00:22 2023 +0000

    Txs generated at intervals
    
    shard-led approach seems to work as well

[33mcommit 3cdc08f4a1fe93e5704baa7a77e4d3bc8c064f44[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Thu Jan 12 17:09:24 2023 +0000

    Shard led node side written
    
    This still needs to be tested to see if it works

[33mcommit 55ee9b79fbdad2055be3758954afdf3bd7a1f784[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Wed Jan 11 16:46:24 2023 +0000

    Shard led client side written

[33mcommit 0f5933ec263d8d626dee0806e083685bdad8969f[m
Merge: 49493ab 07f308e
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Wed Jan 11 15:12:02 2023 +0000

    Merge pull request #3 from lewisHeath/debug
    
    new protocol implemented and starting on shard led

[33mcommit 07f308eca213a0615aed1615657646f345317fbb[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Wed Jan 11 15:10:23 2023 +0000

    cleanup and refactor before starting on shard led

[33mcommit dd914c6a98334169bd3353083040b5e75a3a7bbb[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Tue Jan 10 17:28:58 2023 +0000

    refactored cross shard consensus into an interface
    
    this is to prepare for starting to develop the shard-led consensus protocol

[33mcommit cbf84422bfac7f3b07ec590f7835b4be3460c75d[m
Merge: 50ce891 c57b890
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Tue Jan 10 15:41:31 2023 +0000

    Merge pull request #2 from lewisHeath/ClientLed
    
    Client led transactions work

[33mcommit c57b890a5d82b1824d61b357faa49f32a7348439[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Tue Jan 10 15:39:22 2023 +0000

    Client led seems to work

[33mcommit 74ccaced3790b88a17721ddf015058c4417d25ba[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Mon Jan 9 17:52:16 2023 +0000

    Transactions are being committed

[33mcommit 3620d61412f4c58fd9ba1e8008879910d2b96f50[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Mon Jan 9 17:31:01 2023 +0000

    No more duplicate txs in blocks

[33mcommit 50ce891ca716565c660bdb740b26baf19d9a3f4a[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Sat Jan 7 17:48:33 2023 +0000

    Client led transactions can be committed
    
    client led consensus can be reached using the new protocol. The clients must keep sending the aborted transactions which has not yet been implemented, this is the next step

[33mcommit 2aa8deba5e699cb270025fce1621db9eac161740[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Fri Jan 6 16:14:51 2023 +0000

    added node implementation of the new protocol for client led

[33mcommit 9c08bc03ea2c5a3009283a09ce2d0cffa97d870a[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Thu Jan 5 16:39:11 2023 +0000

    added a logger for coordination messages

[33mcommit 740b15276e8ee8b9bf598c3440e021adde15a79c[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Thu Jan 5 16:19:33 2023 +0000

    started on new protocol
    
    i have made a start on the new protocol which is more like the one i saw in a paper called towards scaling blockchains. this is a change from what i was doing before

[33mcommit 4d0e3115781a84114e9ab4ca912ccadfe08b2cdd[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Thu Jan 5 15:36:38 2023 +0000

    initial commit for debug branch

[33mcommit 49493abeddfb67bf41be3730b8d938ac8cb6304d[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Wed Jan 4 15:52:41 2023 +0000

    each node in a shard has the same mempool
    
    this does not affect the simulation but makes development easier

[33mcommit b7bfda3f24afed392890f93956d48e3b1e10ca56[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Tue Jan 3 15:44:20 2023 +0000

    node bandwidth sampled from ethereum global distribution

[33mcommit 6b51c94292b4525cbffbb6de86ef53217a60ed63[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Thu Dec 15 15:29:35 2022 +0000

    clients and client-led communication
    
    added clients and allowed the shards to communicate using a client-led approach.

[33mcommit a48c06118ecfc0d6387e19c5c622ed830c657159[m
Merge: aedeb47 8b4f64e
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Mon Nov 21 16:01:53 2022 +0000

    Merge pull request #1 from lewisHeath/Temporary
    
    Merge of temporary branch

[33mcommit 8b4f64e136f9979d9a58e0b3aa6552574291c20b[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Mon Nov 21 16:00:49 2022 +0000

    created accounts, and account to shard mappings
    
    created accounts and a mapping of accounts to shards which is just random for now. Then for each transaction I randomly add a sender and receiver account. Then determine if it is a cross shard or intra shard transaction. If it is then proceed normally and if not then call the handle cross shard transaction function. Which still needs to be implemented

[33mcommit ef89649dc41cb7fb4c6fc1c7f1c62eef1b16e7b0[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Thu Nov 17 17:44:08 2022 +0000

    ethereum transactions in PBFT intra shard consensus

[33mcommit aedeb472c98f4cde038c57542889411d04a65a32[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Wed Nov 9 22:58:21 2022 +0000

    Added VoteMessage logger
    
    Added a vote message logger and a method in the sharded network to find the index of a node in a shard

[33mcommit 2d99cc1b59c3bba2ab47a5c046cc2b94ee43a8b5[m
Author: Lewis Heath <email@lewisheath.co.uk>
Date:   Mon Nov 7 15:24:53 2022 +0000

    The basics of sharding implemented with PBFT intra-shard consensus protocol
    
    The number of shards and nodes per shard can be adjusted. The block delivery logger has a bug where it is not logging anything, this needs looking at. PBFT nodes do not have a mempool, each node uses the BlockFactory to create blocks.

[33mcommit a3299fa24468b06a08a7848a9671c80f1712e5e7[m
Author: habib <habib.yajam@gmail.com>
Date:   Thu Sep 8 22:59:59 2022 +0430

    [FIX] some name changes

[33mcommit e5b622b40b24fb28f3ce8e9a0e55c8473aaee656[m
Author: habib <habib.yajam@gmail.com>
Date:   Sun Aug 28 01:39:15 2022 +0430

    [ADD] added new RoundRobinConsensus algorithm
    - new getter method for Node.java

[33mcommit 9672bc0a4ee16db668946f41dd205c31c4777ea3[m
Author: habib <habib.yajam@gmail.com>
Date:   Mon Aug 22 01:12:08 2022 +0430

    [ADD] better AbstractCSVLogger
    - Add new BlockchainReorgLogger that can work with any other PoW blockchain without the need for any special events
    - Improve PoW networks and nodes model
    - Add new factory methods to BitcoinBlock
    - Add genesis block to all consensus algorithms configs
    - Add new logger for uncle block counting
    - Add new class for PoW single parent blocks

[33mcommit 638d1c5dba866f2d6cfdd1dc62398ab12d92b5ee[m
Author: habib <habib.yajam@gmail.com>
Date:   Thu Jul 14 03:14:49 2022 +0430

    [ADD] More updates in README.md file.

[33mcommit d18817017c0ae55c3a1505eaffb996cf419c7deb[m
Author: habib <habib.yajam@gmail.com>
Date:   Tue Jul 5 01:59:38 2022 +0430

    [ADD] Updates about docker compose in the README.md file.

[33mcommit 834a66a1cfcd3bef3172e097fba78b19998ab9e1[m
Author: habib <habib.yajam@gmail.com>
Date:   Tue Jul 5 01:43:16 2022 +0430

    [FIX] renamed output folder to have better consistency with other directories of the project since it is going to be a part of the project folder structure.

[33mcommit 1c61333475f15c2e82365858755dd23e347bafa0[m
Merge: eb2433f 94476e9
Author: habib <habib.yajam@gmail.com>
Date:   Tue Jul 5 01:38:58 2022 +0430

    Merge branch 'BlockWeight'

[33mcommit eb2433f28c20b3f11a1a33b08e93bcbf2bfe3466[m
Author: habib <habib.yajam@gmail.com>
Date:   Tue Jul 5 01:37:31 2022 +0430

    [FIX] gitignore updated to support docker compose

[33mcommit 48227f08d320d1a6affea0542107d64d2523939d[m
Merge: de711a7 1d423d9
Author: habib <habib.yajam@gmail.com>
Date:   Tue Jul 5 01:34:29 2022 +0430

    Merge remote-tracking branch 'origin/main'

[33mcommit 94476e9ee33558b86285b9e73c64aa9fae8dbf2d[m
Author: habib <habib.yajam@gmail.com>
Date:   Tue Jul 5 01:33:57 2022 +0430

    [ADD] add new weight property to all blocks.
    - Add NakamotoHeaviestChainConsensus

[33mcommit 1d423d995872ccabf306f3b7fef1b138a0759433[m
Merge: 334bf9c ebe0c5d
Author: hyajam <habib.yajam@gmail.com>
Date:   Tue Jul 5 01:19:27 2022 +0430

    Merge pull request #3 from seyyedarashazimi/docker
    
    [ADD] Added docker compose and fixed root owned outputs issue in docker.

[33mcommit de711a7633909a0b0ea3b6312d71a571b74c2ce6[m
Author: habib <habib.yajam@gmail.com>
Date:   Mon Jul 4 13:56:19 2022 +0430

    Revert "[FIX] fix"
    
    This reverts commit b8d2269a8f48b0a869ac3da951defd3273f44e6c.

[33mcommit ebe0c5dddc4aad6c5ab0cd78cc287cd2d360c911[m
Author: seyyedarashazimi <arashazimi7@yahoo.com>
Date:   Mon Jul 4 04:05:32 2022 +0430

    [ADD] Added docker compose and fixed root owned outputs issue in docker.

[33mcommit 6cc5390feb75287ea8cecf8ee9b91c356e9c582c[m
Author: seyyedarashazimi <arashazimi7@yahoo.com>
Date:   Mon Jul 4 03:52:18 2022 +0430

    [ADD] Added docker compose and fixed root owned outputs issue in docker

[33mcommit 334bf9ca02e9fe758939559df1ff1ae43156c86a[m
Author: habib <habib.yajam@gmail.com>
Date:   Sat Jul 2 14:44:15 2022 +0430

    [FEAT] Add some features and fix few bugs:
    - Now multiple loggers can be used for a single scenario run
    - Some refactoring for names
    - Added new compact block for bitcoin according to BIP158
    - Newer bitcoin nodes support compact blocks
    - Lowered the number of default nodes in Bitcoin network to a number close to 8000 (reported by https://www.dsn.kastel.kit.edu/bitcoin/) which is seems to be closer to reality than 16000 reported by https://bitnodes.io/
    - Added new logging event: BlockchainReorgEvent
    - Added new logger for block propagation delay

[33mcommit b8d2269a8f48b0a869ac3da951defd3273f44e6c[m
Author: habib <habib.yajam@gmail.com>
Date:   Wed Jun 29 15:37:06 2022 +0430

    [FIX] fix

[33mcommit bd5a631972c0a01560c4b2e044d7956adcbbe326[m
Author: habib <habib.yajam@gmail.com>
Date:   Wed Jun 29 15:00:43 2022 +0430

    [FIX] fix

[33mcommit 7fa66362d093354e6336544b2dc9d542c10a04b2[m
Merge: 85ccc5d dce2ab7
Author: habib <habib.yajam@gmail.com>
Date:   Sat Jun 25 00:55:33 2022 +0430

    [FIX] Fixing some bugs
    - fixed a bug in NormalEthereumNetworkScenario
    - added some explanation in main.java
    - improved README.md
    - better format for elapsed time in scenarios

[33mcommit 85ccc5d8f8a520b1484b338f2127b2540a1d643f[m
Author: habib <habib.yajam@gmail.com>
Date:   Fri Jun 24 21:58:35 2022 +0430

    [FIX] Compressed pings data in pings-2020-07-19-2020-07-20.7z

[33mcommit 0d9092a72e19182ec1ed7f6