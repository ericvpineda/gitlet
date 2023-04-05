package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

/* Class for merge command */
public class Merge {
    String _currentBranch; // Current branch
    Boolean _conflict;     // Boolean for conflicts

    /** Constructor */
    public Merge() {
        _currentBranch = Branch.getCurrentName(); // Current branch
        _conflict = false;                        // Boolean for conflicts
    }

    /**
     * Merge current branch with GIVEN branch
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

        // Note: check modifications/removals between split-point,current branch, target branch history files
        HashMap<String, String> splitPointCommitFiles = Tree.getBlobs(splitPoint._tree);
        // Note: tree hash never null since initializing commits always creates tree
        HashMap<String, String> givenCommitFiles = Tree.getBlobs(givenCommit._tree);
        HashMap<String, String> currentCommitFiles = Tree.getBlobs(currentCommit._tree);

        // 1. Files modified in given branch since splitPoint, not modified in currB since splitPoint
        // -> changed to versions in given branch
        checkModified(givenCommitFiles, currentCommitFiles, splitPointCommitFiles);
        // 5. Files present at splitPoint, unmodified in current branch, but absent in given branch
        checkAbsentCurrentBranch(givenCommitFiles, currentCommitFiles, splitPointCommitFiles);
        // Check merge conflicts
        Commit commit = new Commit("Merged " + givenBranch + " into " + _currentBranch + ".", currentCommit._sha1);
        commit._mergedId = currentBranchSHA1.substring(0, 7) + " " + givenBranchSHA1.substring(0, 7);
        commit.write();

        // Note: Will still create commit even if there is merge conflict (in documentation)
        if (_conflict) {
            System.out.print("Encountered a merge conflict.");
        }
    }

    /** Check modified files in given branch and current branch since split-point commit */
    private void checkModified(HashMap<String, String> givenCommitFiles, HashMap<String, String> currentCommitFiles,
                               HashMap<String, String> splitPointCommitFiles) throws IOException {
        // Note: iterate over given commit file history
        Iterator iter = givenCommitFiles.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) iter.next();
            String name = (String) obj.getKey();
            String sha1 = (String) obj.getValue();
            // Note: If split point does not contain file from given commit
            if (!splitPointCommitFiles.containsKey(name)) {
                // Conflict 3. Checks if file absent at splitPoint and has different contents in given & current branch
                if (currentCommitFiles.containsKey(name) && !currentCommitFiles.get(name).equals(sha1)) {
                    _conflict = true;
                    createConflictFile(name, currentCommitFiles.get(name), sha1);
                    Stage.add(name);
                }
                // 2. Files not present at splitPoint and only present in given branch are checkouted and staged
                if (!currentCommitFiles.containsKey(name)) {
                    Utils.overwriteHelper(name, sha1);
                    Stage.add(name);
                    continue;
                }
            }
            // 1. files modified in given branch, not modified in current branch since splitPoint will stage files
            if (currentCommitFiles.containsKey(name) && splitPointCommitFiles.containsKey(name)) {
                if (currentCommitFiles.get(name).equals(splitPointCommitFiles.get(name)) && !currentCommitFiles.get(name).equals(sha1)) {
                    Utils.overwriteHelper(name, sha1);
                    Stage.add(name);
                    // 2. modified in curr branch, not modified in given branch since splitPoint
                    // 3. files modified in given and curr branch = unchanged
                }
            }
        }
    }

    /**
     * Check files present at split-point, unmodified in current branch, absent in given branch.
     */
    private void checkAbsentCurrentBranch(HashMap<String, String> givenCommitFiles, HashMap<String, String> currentCommitFiles,
                                          HashMap<String, String> splitPointCommitFiles) throws IOException {
        // Iterate through split-point file history
        Iterator iterSplitPoint = splitPointCommitFiles.entrySet().iterator();
        for (Iterator it = iterSplitPoint; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            String name = (String) obj.getKey();    // Name of file
            String sha1 = (String) obj.getValue();  // Hash of file

            // Conflict 1: File present in splitPoint, file changed in both branch (different from splitPoint)
            if (currentCommitFiles.containsKey(name) && givenCommitFiles.containsKey(name)) {
                if (!currentCommitFiles.get(name).equals(givenCommitFiles.get(name)) &&
                        !currentCommitFiles.get(name).equals(sha1) && !givenCommitFiles.get(name).equals(sha1)) {
                    _conflict = true;
                    createConflictFile(name, currentCommitFiles.get(name), givenCommitFiles.get(name));
                    Stage.add(name);
                    continue;
                }
            }
            // Conflict 2: Contents of one branch change, deleted in other branch and is different in splitPoint
            if (currentCommitFiles.containsKey(name) && !currentCommitFiles.get(name).equals(sha1) && !givenCommitFiles.containsKey(name)) {
                _conflict = true;
                createConflictFile(name, currentCommitFiles.get(name), null);
                Stage.add(name);
            }
            if (givenCommitFiles.containsKey(name) && !givenCommitFiles.get(name).equals(sha1) && !currentCommitFiles.containsKey(name)) {
                _conflict = true;
                createConflictFile(name, null, givenCommitFiles.get(name));
                Stage.add(name);
            }
            // 5. File present at splitPoint, unmodified in current branch, absent in given branch are removed and untracked
            if (currentCommitFiles.containsKey(name) && currentCommitFiles.get(name).equals(sha1)) {
                if (!givenCommitFiles.containsKey(name)) {
                    Stage.remove(name);
                }
            }
            // 4. File present at split-point, but current branch and given branch have file removed = do nothing
            // - Note: if file removed in both but in cwd = do nothing
        }
    }

    /** Get split-point between two different commits */
    static String splitPoint(Commit currentCommit, Commit givenCommit) {

        // Get given commit history files
        ArrayList<String> givenCommitHistory = Utils.getAllCommitHistory(givenCommit, new ArrayList<>());

        // Get possible split points between current commit and given commit
        HashMap<String, Integer> possible = splitPointHelper(currentCommit, givenCommitHistory, new HashMap<>(), 0);

        // Get shortest distance split-point
        return Collections.min(possible.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /** Get all possible split-points between current branch commit and given branch commit history.
     * Distance variable is the length from the first commit to be called on and any possible split-points.
     * Note: This is dfs recursive function. */
    private static HashMap<String, Integer> splitPointHelper(Commit currentCommit, ArrayList<String> givenCommitHistory,
                                           HashMap<String, Integer> possibleSplitPoints, int distance) {
        // Traversed past initial commit, return possible split points saved during recursion.
        if (currentCommit == null) {
            return possibleSplitPoints;
        }
        // Check if current commit is split-point in given commit history
        if (givenCommitHistory.contains(currentCommit._sha1)) {
            // if list of possible split points already contains current commit
            if (possibleSplitPoints.containsKey(currentCommit._sha1)) {
                // If distance is shorter, replace previous distance with new distannce
                if (distance < possibleSplitPoints.get(currentCommit._sha1)) {
                    possibleSplitPoints.put(currentCommit._sha1, distance);
                }
            } else {
                // Else, add current commit and its distance
                possibleSplitPoints.put(currentCommit._sha1, distance);
            }
        }
        // Check if commit is merge commit
        if (currentCommit._mergedId != null) {
            String merge = currentCommit._mergedId;
            File c1 = Utils.createFilePath(merge.substring(0, 7), Main.COMMITS);
            File c2 = Utils.createFilePath(merge.substring(8, 15), Main.COMMITS);
            // Recurse on each parent of merge commit
            splitPointHelper(Utils.readObject(c1, Commit.class), givenCommitHistory, possibleSplitPoints, distance + 1);
            splitPointHelper(Utils.readObject(c2, Commit.class), givenCommitHistory, possibleSplitPoints, distance + 1);
        } else {
            // Else, continue up commit tree via parent commit
            splitPointHelper(Commit.getByID(currentCommit._parentSha1), givenCommitHistory, possibleSplitPoints, distance + 1);
        }
        return possibleSplitPoints;
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

