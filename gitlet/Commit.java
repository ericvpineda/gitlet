package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/* Class for commit command */
public class Commit implements GitletObject, Serializable {
    public static String zeroSha1 = "0000000000000000000000000000000000000000"; // Initial gitlet commit identifier
    String _parentSha1;     // Parent commit identifier
    String _logMessage;     // Commit message
    String _tree;           // Tree object identifier
    String _sha1;           // Commit identifier
    String _time;           // Timestamp in -- [Day] [Month] [Date] 00:00:00 [Year] format
    String _mergedId;       // Merge commit identifier

    /**
     * Constructor
     */
    public Commit(String msg, String parentSha1) throws IOException {
        _parentSha1 = parentSha1;   // Parent identifier
        _logMessage = msg;          // Commit message
        _time = Utils.createTime(); // Time commit created
        _tree = createTree();       // Commit tree identifier
        _sha1 = createSha1();       // Commit identifier
        _mergedId = null;           // Merge commit identifier
    }

    /** Constructor for Rebase command */
    public Commit(Commit deepCopy, String parentSha1, String tree) {
        _parentSha1 = parentSha1;            // Parent identifier
        _logMessage = deepCopy._logMessage;  // Commit message
        _time = Utils.createTime();          // Time commit created
        _tree = tree;                        // Commit tree identifier
        _sha1 = createSha1();                // Commit identifier
        _mergedId = null;                    // Merge commit identifier
    }

    /** Constructor for Remote command) */
    public Commit(Commit deepCopy, String parent) {
        _parentSha1 = parent;                // Parent identifier
        _logMessage = deepCopy._logMessage;  // Commit message
        _time = deepCopy._time;              // Time commit created
        _tree = deepCopy._tree;              // Commit tree identifier
        _sha1 = createSha1();                // Commits identifier
        _mergedId = deepCopy._mergedId;      // Merge commit identifier
    }

    /** Constructor for file directory input */
    public Commit(String msg, String parentSha1, File dir) throws IOException {
        _parentSha1 = parentSha1;       // Parent identifier
        _logMessage = msg;              // Commit message
        _time = Utils.createTime();     // Time commit created
        _tree = createTree(dir);        // Commit tree identifier
        _sha1 = createSha1();           // Commits identifier
        _mergedId = null;               // Merge commit identifier
    }

    /** Create empty Tree object */
    public String createTree() throws IOException {
        Tree tree = new Tree();
        return tree._sha1;
    }

    /** Create Tree object based on file directory */
    public String createTree(File dir) throws IOException {
        Tree tree = new Tree(dir);
        return tree._sha1;
    }

    /**
     * Create commit SHA1
     */
    public String createSha1() {
        if (_parentSha1 == null) {
            return zeroSha1;
        }
        return Utils.sha1(_tree, _parentSha1, _logMessage, _time);

    }

    /**
     * Serialize and update the branch based on the commit SHA1
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

    /**
     * Get the HEAD commit of the current branch
     */
    public static Commit getCurrent()  {
        String commitName = Commit.getCurrentSha1();
        return Utils.deserializeCommit(commitName);
    }

    /**
     * Get the current commit SHA1
     */
    public static String getCurrentSha1() {
        if (Main.INDEX.length() == 0) {
            return Commit.zeroSha1;
        }
        String branch = Utils.deserialize(Main.HEAD, String.class);
        return Utils.deserialize(Utils.join(Main.BRANCH, branch), String.class);
    }

    /** Get list of blobs from current commit */
    public static HashMap<String,String> getCurrentBlobs() {
        Commit commit = getCurrent();
        return getBlobs(commit._tree);
    }

    /** Get list of blobs from given tree SHA1 */
    public static HashMap<String,String> getBlobs(String treeSha1) {
        File sectreeFile = Utils.findFile(treeSha1, Main.TREE);
        Tree tree = Utils.deserialize(sectreeFile, Tree.class);
        return tree._blobList;
    }

}
