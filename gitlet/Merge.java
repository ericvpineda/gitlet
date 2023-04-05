package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

/* Class for merge command */
public class Merge {
    String _currBranch;
    Boolean _conflict;

    // Constructor
    public Merge() {
        _currBranch = Branch.getCurrent(); // Current branch
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
        } else if (!stage.isPreStageEmpty() || !stage.isDeleteStageEmpty()) {
            System.out.print("You have uncommitted changes.");
            return;
        } else if (!Utils.join(Main.BRANCH, givenBranch).exists()) {
            System.out.print("A branch with that name does not exist.");
            return;
        } else if (givenBranch.equals(_currBranch)) {
            System.out.print("Cannot merge a branch with itself.");
            return;
        }

        // Retrieve both branch names and HEAD commits
        String currentBranchSHA1 = Branch.read(_currBranch);
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
        HashMap<String, String> spHM = Commit.getBlobs(splitPoint._tree);
        HashMap<String, String> tarHM = Commit.getBlobs(givenCommit._tree);
        HashMap<String, String> currHM = Commit.getBlobs(currentCommit._tree);

        // 1. files modified in given branch since SP, not modified in currB since SP
        // -> changed to versions in branch
        checkModified(tarHM, currHM, spHM);
        // 5. files present @ SP, unmodified in currB, absent in givenbranch
        // -> removed??
        checkAbsentCurrentBranch(tarHM, currHM, spHM);
        // Check any merge conflicts
        Commit commit = new Commit("Merged " + givenBranch + " into " + _currBranch + ".", currentCommit._sha1);
        commit._mergedId = currentBranchSHA1.substring(0, 7) + " " + givenBranchSHA1.substring(0, 7);
        commit.write();

        // Note: Will still create commit even if there is merge conflict (in documentation)
        if (_conflict) {
            System.out.print("Encountered a merge conflict.");
        }
    }

    private void checkModified(HashMap<String, String> tarHM, HashMap<String, String> currHM,
                               HashMap<String, String> spHM) throws IOException {
        // Note: iterate over target hm
        Iterator iter = tarHM.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) iter.next();
            String name = (String) obj.getKey();
            String sha1 = (String) obj.getValue();
            if (!spHM.containsKey(name)) {
                // Conflict 3. checks if file absent at sp and diff contents in target & current branch
                if (currHM.containsKey(name) && !currHM.get(name).equals(sha1)) {
                    _conflict = true;
                    Utils.createConflictFile(name, currHM.get(name), sha1);
                    Stage.add(name);
                }
                // 2. files not present at sp and only present in given branch -> checkout, staged
                if (!currHM.containsKey(name)) {
                    Utils.overwriteHelper(name, sha1);
                    Stage.add(name);
                    continue;
                }
            }
            // 1. files modified in given branch, not modified in current branch since sp -> files staged
            if (currHM.containsKey(name) && spHM.containsKey(name)) {
                if (currHM.get(name).equals(spHM.get(name)) && !currHM.get(name).equals(sha1)) {
                    Utils.overwriteHelper(name, sha1);
                    Stage.add(name);
                    // 2. modified in curr branch, not mod in given branch since sp
                    // 3. files modified in body given and curr branch = unchanged
                }
            }
        }
    }


    /**
     * Helper method to check files present at SP, unmodified in current branch, absent in given branch
     */
    private void checkAbsentCurrentBranch(HashMap<String, String> tarHM, HashMap<String, String> currHM,
                                          HashMap<String, String> spHM) throws IOException {
        Iterator iterSP = spHM.entrySet().iterator();
        for (Iterator it = iterSP; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            String name = (String) obj.getKey();
            String sha1 = (String) obj.getValue();
            // Conflict 1: present in sp, both are changed (diff from sp)
            if (currHM.containsKey(name) && tarHM.containsKey(name)) {
                if (!currHM.get(name).equals(tarHM.get(name)) &&
                        !currHM.get(name).equals(sha1) && !tarHM.get(name).equals(sha1)) {
                    _conflict = true;
                    Utils.createConflictFile(name, currHM.get(name), tarHM.get(name));
                    Stage.add(name);
                    continue;
                }
            }
            // Conflict 2: contents of one branch change, deleted in other & in diff in sp
            if (currHM.containsKey(name) && !currHM.get(name).equals(sha1) && !tarHM.containsKey(name)) {
                _conflict = true;
                Utils.createConflictFile(name, currHM.get(name), null);
                Stage.add(name);
            }
            if (tarHM.containsKey(name) && !tarHM.get(name).equals(sha1) && !currHM.containsKey(name)) {
                _conflict = true;
                Utils.createConflictFile(name, null, tarHM.get(name));
                Stage.add(name);
            }
            // 5. present at sp, unmodified in current branch, absent in given -> removed and untracked
            if (currHM.containsKey(name) && currHM.get(name).equals(sha1)) {
                if (!tarHM.containsKey(name)) {
                    Stage.remove(name);
                }
            }
            // Note: present at sp && contents in one branch is change and other branch deleted  (conflict 3)
            // 4. Curr and given branch have file removed = do nothing
            // - if file removed in both but in cwd = do nothing
        }
    }

    // Get split-point between two different commits
    static String splitPoint(Commit currentCommit, Commit givenCommit) {

        // Get given commit history
        ArrayList<String> givenCommitHistory = Utils.getTotalSha1History(givenCommit, new ArrayList<>());
//        System.out.println("Given branch history = " + givenCommitHistory);
//
//        ArrayList<String> currentCommitHistory = Utils.getTotalSha1History(currentCommit, new ArrayList<>());
//        System.out.println("Current branch history = " + currentCommitHistory);

        // Get possible split points between current commit and given commit
        HashMap<String, Integer> possible = splitPointHelper(currentCommit, givenCommitHistory, new HashMap<>(), 0);

        // Get shortest distance split-point
        return Collections.min(possible.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private static HashMap<String, Integer> splitPointHelper(Commit commit, ArrayList<String> tarHistory,
                                           HashMap<String, Integer> sp, int distance) {
        if (commit == null) {
            return sp;
        }
        if (tarHistory.contains(commit._sha1)) {
            if (sp.containsKey(commit._sha1)) {
                if (distance < sp.get(commit._sha1)) {
                    sp.put(commit._sha1, distance);
                }
            } else {
                sp.put(commit._sha1, distance);
            }
        }
        if (commit._mergedId != null) {
            String merge = commit._mergedId;
            File c1 = Utils.createFilePath(merge.substring(0, 7), Main.COMMITS);
            File c2 = Utils.createFilePath(merge.substring(8, 15), Main.COMMITS);
            splitPointHelper(Utils.readObject(c1, Commit.class), tarHistory, sp, distance + 1);
            splitPointHelper(Utils.readObject(c2, Commit.class), tarHistory, sp, distance + 1);
        } else {
            splitPointHelper(Commit.getByID(commit._parentSha1), tarHistory, sp, distance + 1);
        }
        return sp;
    }
}

