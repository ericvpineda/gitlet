package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

// Note:
// - commit can be null, but not its contents

/* Class for commit command */
public class Commit implements GitletObject, Serializable {
    public static String zeroSha1 = "0000000000000000000000000000000000000000";
    String _parentSha1;
    String _logMessage;
    String _tree;
    String _sha1;
    String _time;
    String _mergedId;

    /**
     * Commit constructor
     */
    public Commit(String msg, String parentSha1) throws IOException {
        _parentSha1 = parentSha1;
        _logMessage = msg;
        _time = Utils.createTime();
        _tree = createTree();
        _sha1 = createSha1();
        _mergedId = null;
    }

    // Note: Create a deep copy of commit (Rebase command)
    public Commit(Commit deepCopy, String parentSha1, String tree) {
        _parentSha1 = parentSha1;
        _logMessage = deepCopy._logMessage;
        _time = Utils.createTime();
        _tree = tree;
        _sha1 = createSha1();
        _mergedId = null;
    }

    // Note: Create a deep copy of commit for remote
    public Commit(Commit deepCopy, String parent) {
        _parentSha1 = parent;
        _logMessage = deepCopy._logMessage;
        _time = deepCopy._time;
        _tree = deepCopy._tree;
        _sha1 = createSha1();
        _mergedId = deepCopy._mergedId;
    }

    public Commit(String msg, String parentSha1, File dir) throws IOException {
        _parentSha1 = parentSha1;
        _logMessage = msg;
        _time = Utils.createTime();
        _tree = createTree(dir);
        _sha1 = createSha1();
        _mergedId = null;
    }

    /** Create Tree object */
    public String createTree() throws IOException {
        Tree tree = new Tree();
        return tree._sha1;
    }

    public String createTree(File dir) throws IOException {
        Tree tree = new Tree(dir);
        return tree._sha1;
    }

    /**
     * Create commit sha1
     */
    public String createSha1() {
        if (_parentSha1 == null) {
            return zeroSha1;
        }
        return Utils.sha1(_tree, _parentSha1, _logMessage, _time);

    }

    /**
     * Serialize and update the branch based on the commit sha1
     */
    public void write() throws IOException {
        Commit curr = Commit.getCurrent();
        if (curr != null && curr._tree.equals(_tree)) {
            System.out.print("No changes added to the commit.");
            return;
        }
        write(_sha1, this, Main.COMMITS);
        String currBranch = Branch.getCurrent();
        Branch.update(_sha1, currBranch, Main.BRANCH);
        Stage.clear();
    }

    /** Write to remote branch */
    public void writeRemote(File rPath) throws IOException {
        File commitFile = Utils.join(rPath, "objects", "commits");
        write(_sha1, this, commitFile);
    }

    // Note: will never be null
    /**
     * Get the current commit
     */
    public static Commit getCurrent()  {
        String commitName = Commit.getCurrentSha1();
        return Utils.deserializeCommit(commitName);
    }

    /**
     * Get the current commit
     */
    public static String getCurrentSha1() {
        if (Main.INDEX.length() == 0) {
            return Commit.zeroSha1;
        }
        String branch = Utils.deserialize(Main.HEAD, String.class);
        return Utils.deserialize(Utils.join(Main.BRANCH, branch), String.class);
    }

    public static HashMap<String,String> getCurrentBlobs() {
        Commit commit = getCurrent();
        return getBlobs(commit._tree);
    }

    public static HashMap<String,String> getBlobs(String treeSha1) {
        File sectreeFile = Utils.findFile(treeSha1, Main.TREE);
        Tree tree = Utils.deserialize(sectreeFile, Tree.class);
        return tree._blobList;
    }

}
