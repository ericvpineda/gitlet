## Gitlet [Class Project]

## Description
Version Control System (VCS) for user files. The main functionality includes:

- Committing contents of entire directories
- Checking out, aka restoring versions, of 1+ files or entire commits
- View log history
- Branching sequence of commits
- Merging branches

Note: Project was completed in Summer 2020 and received 100% test completion on class testing server. 

## Commands
init
- Description: Creates new gitlet VCS in current directory
- Runtime: O(1)
- Terminal command: java gitlet.Main

add
- Description: Stages files for addition
- Runtime: O(n) to size of file added, O(log(N)) for number files in commit
- Terminal command: java gitlet.Main add [file name]

commit
- Description: Saves snapshot of files in current commit and staging area. A commit will update contents of file tracked, saved stage but untracked files, and untrack files staged for removal.
- Runtime: O(n) with respect to total size of files commit tracking
- Memory: committing increases size of .gitlet directory [= size of files staged during commit
- Terminal command: java gitlet.Main commit [message]

rm
- Description: Unstage file if currently staged. If file tracked in current commit, stage for removal and remove file from current working directory.
- Runtime: O(1)
- Terminal command: java gitlet.Main rm [file name]

log
- Description: Shows commit history, starting from current head commit. Ignores second parents found in merge commits.
- Runtime: O(n) w.r.t commit history
- Terminal command: java gitlet.Main log

global-log
- Description: Like log command, but displays all commits ever made. Not specific ordering.
- Runtime: O(n)
- Terminal command: global-log

find
- Description: Prints all commit ids for given commit message.
- Runtime: O(n) w.r.t number of commits
- Terminal command: java gitlet.Main find [commit message]

status
- Description: Displays current existing branch names, and marks current branch with '*'. Also displays files staged for addition, removal, and untracked files.
- Runtime: Relative to amount of data in working directory and O(n) to number of branches, files staged to be added, deleted.
- Terminal command: java gitlet.Main status

checkout
- Description: Three separate uses:
    1. Revert file to version in current commit
    2. Revert file to version in user selected commit
    3. Overwrites files in working directory with file versions in head commit of user selected branch.
- Runtime:
    1. O(n) w.r.t sizes of file checked out
    2. O(n) w.r.t total size of files in commit snapshot.
- Terminal command:
    1. java gitlet.Main checkout -- [file name]
    2. java gitlet.Main checkout [commit id] -- [file name]
    3. java gitlet.Main checkout [branch name]

branch
- Description: Create new branch with given name and points to current head node. Does not immediately switch to new branch.
- Runtime: O(1)
- Terminal command: java gitlet.Main branch [branch name]

rm-branch
- Description: Deletes branch with given name.
- Runtime: O(1)
- Memory:
- Terminal command: java gitlet.Main rm-branch [branch name]

reset
- Description: Check out all files tracked by given commit and removes tracked files not present in commit. Also, moves branch head to given commit node.
- Runtime: O(n) w.r.t total size of files tracked by given commit snapshot.
- Memory:
- Terminal command: java gitlet.Main reset [commit id]

merge
- Description: Merge files from given branch into current branch.
- Runtime: O(n log(n) + D) s.t. N is total number ancestors for merging 2 branches, D is total amount data in all files under commit.
- Terminal command: java gitlet.Main merge [branch name]