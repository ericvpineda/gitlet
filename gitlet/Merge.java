package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

/* Class for merge command */
public class Merge {
    String _currentBranch;
    Boolean _conflict;

    /** Constructor */
    public Merge() {
        _currentBranch = Branch.getCurrentName(); // Current branch
        _conflict = false;                 // Boolean for any conflicts
    }

    /**
     * Merge current branch with given branch
     */
    public void apply(String givenBranch) throws IOException {

        // Check for stage errors
        Stage stage = Stage.read();
        if (Utils.checkUntrackedCwd()) {
            System.out.print("There is an untracked file in the way; delete it, " +
                    "or add and commit it first.");
            return;
        } else if (!stage.isAdditionsEmpty() || !stage.isDeletionsEmpty()) {
            System.out.print("You have uncommitted changes.");
            return;
        } else if (!Utils.join(Main.BRANCH, givenBranch).exists()) {
            System.out.print("A branch with that name does not exist.");
            return;
        } else if (givenBranch.equals(_currentBranch)) {
            System.out.print("Cannot merge a branch with itself.");
            return;
        }

        // Retrieve both branch names and HEAD commits
        String currentBranchSHA1 = Branch.read(_currentBranch);
        String givenBranchSHA1 = Branch.read(givenBranch);
        Commit currentCommit = Commit.getByID(currentBranchSHA1);
        Commit givenCommit = Commit.getByID(givenBranchSHA1);

        // Find split point SHA1
        String splitPointSHA1 = splitPoint(currentCommit, givenCommit);

        // Get split point commit
        Commit splitPoint = Commit.getByID(splitPointSHA1);

        // Check if given branch is ancestor (split-point is same commit as given branch)
        if (givenBranchSHA1.equals(splitPointSHA1)) {
            System.out.print("Given branch is an ancestor of the current branch.");
            return;
        // Check if current branch is split-point
        } else if (currentBranchSHA1.equals(splitPointSHA1)) {
            Checkout.reset(givenBranchSHA1);
            System.out.print("Current branch fast-forwarded.");
            return;
        }

        // Note: check modifications/removals between splitpoint,current branch, target branch history files
        HashMap<String, String> splitPointCommitHistory = Commit.getBlobs(splitPoint._tree);
        HashMap<String, String> givenCommitHistory = Commit.getBlobs(givenCommit._tree);
        HashMap<String, String> currentCommitHistory = Commit.getBlobs(currentCommit._tree);

        // 1. files modified in given branch since splitPoint, not modified in currB since splitPoint
        // -> changed to versions in branch
        checkModified(givenCommitHistory, currentCommitHistory, splitPointCommitHistory);
        // 5. files present at splitPoint, unmodified in currB, absent in givenbranch
        checkAbsentCurrentBranch(givenCommitHistory, currentCommitHistory, splitPointCommitHistory);
        // Check any merge conflicts
        Commit commit = new Commit("Merged " + givenBranch + " into " + _currentBranch + ".", currentCommit._sha1);
        commit._mergedId = currentBranchSHA1.substring(0, 7) + " " + givenBranchSHA1.substring(0, 7);
        commit.write();

        // Note: Will still create commit even if there is merge conflict (in documentation)
        if (_conflict) {
            System.out.print("Encountered a merge conflict.");
        }
    }

    /** Check modified files in given branch and current branch since split-point commit */
    private void checkModified(HashMap<String, String> givenCommitHistory, HashMap<String, String> currentCommitHistory,
                               HashMap<String, String> splitPointCommitHistory) throws IOException {
        // Note: iterate over target hm
        Iterator iter = givenCommitHistory.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) iter.next();
            String name = (String) obj.getKey();
            String sha1 = (String) obj.getValue();
            if (!splitPointCommitHistory.containsKey(name)) {
                // Conflict 3. checks if file absent at splitPoint and diff contents in target & current branch
                if (currentCommitHistory.containsKey(name) && !currentCommitHistory.get(name).equals(sha1)) {
                    _conflict = true;
                    createConflictFile(name, currentCommitHistory.get(name), sha1);
                    Stage.add(name);
                }
                // 2. files not present at splitPoint and only present in given branch -> checkout, staged
                if (!currentCommitHistory.containsKey(name)) {
                    Utils.overwriteHelper(name, sha1);
                    Stage.add(name);
                    continue;
                }
            }
            // 1. files modified in given branch, not modified in current branch since splitPoint -> files staged
            if (currentCommitHistory.containsKey(name) && splitPointCommitHistory.containsKey(name)) {
                if (currentCommitHistory.get(name).equals(splitPointCommitHistory.get(name)) && !currentCommitHistory.get(name).equals(sha1)) {
                    Utils.overwriteHelper(name, sha1);
                    Stage.add(name);
                    // 2. modified in curr branch, not mod in given branch since splitPoint
                    // 3. files modified in body given and curr branch = unchanged
                }
            }
        }
    }


    /**
     * Check files present at split-point, unmodified in current branch, absent in given branch.
     */
    private void checkAbsentCurrentBranch(HashMap<String, String> givenCommitHistory, HashMap<String, String> currentCommitHistory,
                                          HashMap<String, String> splitPointCommitHistory) throws IOException {
        Iterator iterSP = splitPointCommitHistory.entrySet().iterator();
        for (Iterator it = iterSP; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            String name = (String) obj.getKey();
            String sha1 = (String) obj.getValue();
            // Conflict 1: present in splitPoint, both are changed (diff from splitPoint)
            if (currentCommitHistory.containsKey(name) && givenCommitHistory.containsKey(name)) {
                if (!currentCommitHistory.get(name).equals(givenCommitHistory.get(name)) &&
                        !currentCommitHistory.get(name).equals(sha1) && !givenCommitHistory.get(name).equals(sha1)) {
                    _conflict = true;
                    createConflictFile(name, currentCommitHistory.get(name), givenCommitHistory.get(name));
                    Stage.add(name);
                    continue;
                }
            }
            // Conflict 2: contents of one branch change, deleted in other & in diff in splitPoint
            if (currentCommitHistory.containsKey(name) && !currentCommitHistory.get(name).equals(sha1) && !givenCommitHistory.containsKey(name)) {
                _conflict = true;
                createConflictFile(name, currentCommitHistory.get(name), null);
                Stage.add(name);
            }
            if (givenCommitHistory.containsKey(name) && !givenCommitHistory.get(name).equals(sha1) && !currentCommitHistory.containsKey(name)) {
                _conflict = true;
                createConflictFile(name, null, givenCommitHistory.get(name));
                Stage.add(name);
            }
            // 5. present at splitPoint, unmodified in current branch, absent in given -> removed and untracked
            if (currentCommitHistory.containsKey(name) && currentCommitHistory.get(name).equals(sha1)) {
                if (!givenCommitHistory.containsKey(name)) {
                    Stage.remove(name);
                }
            }
            // Note: present at splitPoint && contents in one branch is change and other branch deleted  (conflict 3)
            // 4. Curr and given branch have file removed = do nothing
            // - if file removed in both but in cwd = do nothing
        }
    }

    /** Get split-point between two different commits */
    static String splitPoint(Commit currentCommit, Commit givenCommit) {

        // Get given commit history
        ArrayList<String> givenCommitHistory = Utils.getAllCommitHistory(givenCommit, new ArrayList<>());

        // Get possible split points between current commit and given commit
        HashMap<String, Integer> possible = splitPointHelper(currentCommit, givenCommitHistory, new HashMap<>(), 0);

        // Get shortest distance split-point
        return Collections.min(possible.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /** Get all possible split-points between commit and given branch commit history */
    private static HashMap<String, Integer> splitPointHelper(Commit commit, ArrayList<String> givenCommitHistory,
                                           HashMap<String, Integer> splitPoint, int distance) {
        if (commit == null) {
            return splitPoint;
        }
        if (givenCommitHistory.contains(commit._sha1)) {
            if (splitPoint.containsKey(commit._sha1)) {
                if (distance < splitPoint.get(commit._sha1)) {
                    splitPoint.put(commit._sha1, distance);
                }
            } else {
                splitPoint.put(commit._sha1, distance);
            }
        }
        if (commit._mergedId != null) {
            String merge = commit._mergedId;
            File c1 = Utils.createFilePath(merge.substring(0, 7), Main.COMMITS);
            File c2 = Utils.createFilePath(merge.substring(8, 15), Main.COMMITS);
            splitPointHelper(Utils.readObject(c1, Commit.class), givenCommitHistory, splitPoint, distance + 1);
            splitPointHelper(Utils.readObject(c2, Commit.class), givenCommitHistory, splitPoint, distance + 1);
        } else {
            splitPointHelper(Commit.getByID(commit._parentSha1), givenCommitHistory, splitPoint, distance + 1);
        }
        return splitPoint;
    }


    /** Replace contents of a file with conflict message */
    public static void createConflictFile(String fileName, String current, String target) {
        String header = "<<<<<<< HEAD\n";
        String middle = "=======\n";
        String end = ">>>>>>>\n";
        String curr = "";
        String tar = "";
        if (current != null) {
            Blob currBlob = Utils.readObject(Utils.createFilePath(current, Main.BLOB), Blob.class);
            curr = currBlob._fileContent;
        }
        if (target != null) {
            Blob tarBlob = Utils.readObject(Utils.createFilePath(target, Main.BLOB), Blob.class);
            tar = tarBlob._fileContent;
        }
        byte[] conflict = (header + curr + middle + tar + end).getBytes();
        Utils.writeContents(Utils.join(Main.USERDIR, fileName), conflict);
    }
}

