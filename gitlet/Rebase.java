package gitlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

// Note: still need to test this**
public class Rebase {

    // Note: find sp of current branch and given branch, snap off current branch, attach to given branch
    public static void apply(String branch) throws IOException {
        String targetSha1 = Branch.read(branch);
        if (targetSha1 == null) {
            System.out.print("A branch with that name does not exist.");
            return;
        } else if (branch.equals(Branch.getCurrent())) {
            System.out.print("Cannot rebase a branch onto itself.");
        } else if (targetSha1.equals(Commit.getCurrentID())) {
            System.out.print("Already up-to-date.");
        }
        Commit targetCom = Commit.getByID(targetSha1);
        Commit currentCom = Commit.getCurrent();

        String splitPoint = Merge.splitPoint(currentCom,targetCom);
        Commit splitPointCom = Commit.getByID(splitPoint);
        // Note: find the split right before splitpoint

        // Note: Check null cases??
        String replayHead = replayCommits(currentCom, targetCom, splitPointCom);
        // Note: reset to the front of replayed branch
        Checkout.reset(replayHead);
    }

    private static String replayCommits(Commit current, Commit target, Commit splitpoint) throws IOException {
        // Note: putting all branch commits into stack
        Stack<Commit> replayBranch = new Stack<>();
        while (target != null && target._parentSha1 != null &&
                !target._parentSha1.equals(splitpoint._parentSha1)) {
            replayBranch.push(target);
            target = Commit.getByID(target._parentSha1);
        }
        // Note: creating replayed commits
        String parent = current._sha1;
        HashMap<String,String> prev = Commit.getBlobs(current._tree);
        HashMap<String,String> split = Commit.getBlobs(splitpoint._tree);
        Commit replay = null;
        while (!replayBranch.isEmpty()) {
            Commit poppedCom = replayBranch.pop();
            // Note: if commit has been modified since splitpoint, changes propagate
            // 1. Iterate through replayed commit
            HashMap<String,String> tree = Commit.getBlobs(poppedCom._tree);
            if (tree != null) {
                Iterator iter = tree.entrySet().iterator();
                for (Iterator it = iter; it.hasNext(); ) {
                    Map.Entry obj = (Map.Entry) it.next();
                    String name = (String) obj.getKey();
                    String sha1 = (String) obj.getValue();
                    // Note: check for files that need to be updated
                    if (prev != null && prev.containsKey(name)) {
                        String prevSha1 = prev.get(name);
                        if (split != null && split.containsKey(name) &&
                                split.get(name).equals(prevSha1) && !prevSha1.equals(sha1)) {
                            prev.put(name, sha1);
                        }
                        // Note: if there is diff in splitpoint, current branch and new branch versions = do nothing
                    }
                }
            }
            // 2. Iterate through previous commit
            // Note: creating a new tree identifier for replayed commit
            String treeSha1;
            if (prev != null) {
                Tree newTree = new Tree(prev);
                newTree.write();
                treeSha1 = newTree._sha1;
            } else {
                treeSha1 = poppedCom._tree;
            }
            // 3. Check splitpoint files
            replay = new Commit(poppedCom, parent, treeSha1);
            replay.write();
            // Note: updated while loop variables
            parent = poppedCom._sha1;
            prev = Commit.getBlobs(treeSha1);
        }
        if (replay != null) {
            return replay._sha1;
        }
        return null;
    }
}
