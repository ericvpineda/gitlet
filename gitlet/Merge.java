package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

// Note:
// - look over checkout reset
// - look for bugs
// - making conflict files?

/* Class for merge command */
public class Merge {
    String _currBranch;
    Boolean _conflict;

    public Merge() {
        _currBranch = Branch.getCurrent();
        _conflict = false;
    }

    /**
     * Method call for merge
     */
    public void apply(String branch) throws IOException {
        Stage stage = Stage.read();
        if (Utils.checkUntrackedCwd()) {
            System.out.print("There is an untracked file in the way; delete it, " +
                    "or add and commit it first.");
            return;
        } else if (!stage._preStage.isEmpty() || !stage._deletion.isEmpty()) {
            System.out.print("You have uncommitted changes.");
            return;
        } else if (!Utils.join(Main.BRANCH, branch).exists()) {
            System.out.print("A branch with that name does not exist.");
            return;
        } else if (branch.equals(_currBranch)) {
            System.out.print("Cannot merge a branch with itself.");
            return;
        }
        // Note: check for untracked files error
        String currName = Branch.read(_currBranch);
        String tarName = Branch.read(branch);
        Commit currCom = Utils.deserializeCommit(currName);
        Commit tarCom = Utils.deserializeCommit(tarName);

        String splitPoint = splitPoint(currCom, tarCom);
        Commit spCom = Utils.deserializeCommit(splitPoint);
        // Note: check if branch is ancestor or fast forward merge
        if (tarName.equals(splitPoint)) {
            System.out.print("Given branch is an ancestor of the current branch.");
            return;
            // Note: If split point is current branch
        } else if (currName.equals(splitPoint)) {
            Checkout.reset(tarName);
            System.out.print("Current branch fast-forwarded.");
            return;
        }

        // Note: check modifications/removals between splitpoint,current branch, target branch history files
        HashMap<String, String> spHM = Commit.getBlobs(spCom._tree);
        HashMap<String, String> tarHM = Commit.getBlobs(tarCom._tree);
        HashMap<String, String> currHM = Commit.getBlobs(currCom._tree);

        // 1. files modified in given branch since SP, not modified in currB since SP
        // -> changed to versions in branch
        checkModified(tarHM, currHM, spHM);
        // 5. files present @ SP, unmodified in currB, absent in givenbranch
        // -> removed??
        checkAbsentCurrentBranch(tarHM, currHM, spHM);
        // Check any merge conflicts
        Commit mergeCommit = new Commit("Merged " + branch + " into " + _currBranch + ".", currCom._sha1);
        mergeCommit._mergedId = currName.substring(0, 7) + " " + tarName.substring(0, 7);
        mergeCommit.write();
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

    // Note: maybe use the face that merge commits differ ?
    static String splitPoint(Commit currCom, Commit tarCom) {
        ArrayList<String> tarHistory = Utils.getTotalSha1History(tarCom, new ArrayList<>());
//        System.out.println("Given branch history = " + tarHistory);
        HashMap<String, Integer> possible = splitPointHelper(currCom, tarHistory, new HashMap<>(), 0);
//        System.out.println("Possible splitpoints = " + possible);
        String lca = Collections.min(possible.entrySet(), Map.Entry.comparingByValue()).getKey();
//        System.out.println(lca);
        return lca;
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
            File c1 = Utils.findFile(merge.substring(0, 7), Main.COMMITS);
            File c2 = Utils.findFile(merge.substring(8, 15), Main.COMMITS);
            splitPointHelper(Utils.deserialize(c1, Commit.class), tarHistory, sp, distance + 1);
            splitPointHelper(Utils.deserialize(c2, Commit.class), tarHistory, sp, distance + 1);
        } else {
            splitPointHelper(Utils.deserializeCommit(commit._parentSha1), tarHistory, sp, distance + 1);
        }
        return sp;
    }
}

