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
    Tree _treeObject;       // Temporary holder for tree objets

    /** Constructor for general commits */
    public Commit(String message, String parentSha1) throws IOException {
        _parentSha1 = parentSha1;           // Parent identifier
        _logMessage = message;              // Commit message
        _time = Utils.createTime();         // Time commit created
        _treeObject = new Tree();           // Container for file blobs (Note: This field is deleted upon commit.)
        _tree = _treeObject._sha1;          // Tree object identifier
        _sha1 = createHash();               // Commit identifier
        _mergedId = null;                   // Merge commit identifier
    }

    /** Constructor for file directory input */
    // ISSUE: dont want to write tree until commit is written
    public Commit(String msg, String parentSha1, File dir) throws IOException {
        _parentSha1 = parentSha1;       // Parent identifier
        _logMessage = msg;              // Commit message
        _time = Utils.createTime();     // Time commit created
        _treeObject = createTree(dir);  // Container for file blobs (Note: This field is deleted upon commit.)
        _tree = _treeObject._sha1;      // Tree identifier
        _sha1 = createHash();           // Commit identifier
        _mergedId = null;               // Merge commit identifier
    }

    /** Constructor for Rebase command */
    public Commit(Commit deepCopy, String parentSha1, String tree) {
        _parentSha1 = parentSha1;            // Parent identifier
        _logMessage = deepCopy._logMessage;  // Commit message
        _time = Utils.createTime();          // Time commit created
        _tree = tree;                        // Commit tree identifier
        _sha1 = createHash();                // Commit identifier
        _mergedId = null;                    // Merge commit identifier
    }

    /** Constructor for deep copy commits */
    public Commit(Commit deepCopy, String parentSha1) {
        _parentSha1 = parentSha1;            // Parent identifier
        _logMessage = deepCopy._logMessage;  // Commit message
        _time = deepCopy._time;              // Time commit created
        _tree = deepCopy._tree;              // Commit tree identifier
        _sha1 = createHash();                // Commits identifier
        _mergedId = deepCopy._mergedId;      // Merge commit identifier
    }

    /** Create Tree object based on file directory */
    public Tree createTree(File dir) throws IOException {
        if (dir == null) {
            return new Tree();
        }
        return new Tree(dir);
    }

    /** Create commit identifier */
    public String createHash() {
        if (_parentSha1 == null) {
            return zeroSha1;
        }
        String currentBranchName = Branch.getCurrent();
        return Utils.sha1(_tree, _logMessage, _time, currentBranchName);

    }

    /** Write commit to disk and update the HEAD branch based on new commit id */
    public void write() throws IOException {

        // Check if new commit does not have any changes to files
        Commit currentCommit = getCurrent();
        boolean isUnchangedTree = currentCommit != null && currentCommit._tree != null && currentCommit._tree.equals(this._tree);

        if (!this._sha1.equals(Commit.zeroSha1) && isUnchangedTree) {
            System.out.print("No changes added to the commit.");
            return;
        }

        // Write Tree to disk
        this._treeObject.write();

        // Remove tree object to reduce memory overhead
        this._treeObject = null;

        // Write commit to disk
        // Note: This function is part of GitlitObject interface
        writeToDisk(_sha1, this, Main.COMMITS);

        // Update HEAD commit in current branch
        Branch.update(_sha1, Branch.getCurrent(), Main.BRANCH);
        Stage.clear();
    }

    /** Write to remote branch */
    public void writeRemote(File rPath) throws IOException {
        File commitFile = Utils.join(rPath, "objects", "commits");

        // Note: Write command part of GitletObject interface
        writeToDisk(_sha1, this, commitFile);
    }

    /**
     * Get the HEAD commit of the current branch
     */
    public static Commit getCurrent()  {
        String id = Commit.getCurrentID();
        return Utils.deserializeCommit(id);
    }

    /**
     * Get the current commit SHA1
     */
    public static String getCurrentID() {

        // Edge case: no commits created yet
        if (Main.STAGE.length() == 0) {
            return zeroSha1;
        }

        // Get current branch name and head commit branch is pointing to 
        String branch = Utils.readObject(Main.HEAD, String.class);
        File commitID = Utils.join(Main.BRANCH, branch);

        // Else, find commit pointed to by HEAD pointer
        if (branch != null && commitID.exists()) {
            return Utils.readContentsAsString(commitID);
        }
        return null;
    }

    /** Get list of blobs from current commit */
    public static HashMap<String,String> getCurrentBlobs() {
        Commit currentCommit = getCurrent();
        if (currentCommit != null && currentCommit._tree != null) {
            return getBlobs(currentCommit._tree);
        }
        return new HashMap<>();
    }

    /** Get list of blobs from given tree SHA1 */
    public static HashMap<String,String> getBlobs(String treeID) {
        File file = Utils.createFilePath(treeID, Main.TREE);
        if (file != null) {
            Tree tree = Utils.deserialize(file, Tree.class);
            return tree._blobList;
        }
        return new HashMap<>();
    }
}
