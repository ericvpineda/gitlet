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

    /**
     * Get the HEAD commit of the current branch
     */
    public static Commit getCurrent()  {
        return getByID(getCurrentID());
    }

    /**
     * Get the current commit SHA1
     */
    public static String getCurrentID() {

        // Get current branch name and head commit branch is pointing to
        String branch = Utils.readContentsAsString(Main.HEAD);

        // Edge case: no commits created yet
        if (branch.length() == 0) {
            return zeroSha1;
        }
        File commitID = Utils.join(Main.BRANCH, branch);
        return Utils.readContentsAsString(commitID);
    }

    /** Get list of blobs from current commit */
    public static HashMap<String,String> getCurrentBlobs() {
        Commit currentCommit = getCurrent();
        if (currentCommit != null && currentCommit._tree != null) {
            return Tree.getBlobs(currentCommit._tree);
        }
        return new HashMap<>();
    }

    /** Get commits based on SHA1 identifier */
    static Commit getByID(String id) {
        if (id != null) {
            File file = Utils.createFilePath(id, Main.COMMITS);
            if (file != null) {
                return Utils.readObject(file, Commit.class);
            }
        }
        return null;
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
        Branch.update(_sha1, Branch.getCurrentName(), Main.BRANCH);

        // Clear stage of any file additions or deletions
        Stage.clear();
    }

    /** Create commit identifier */
    public String createHash() {
        if (_parentSha1 == null) {
            return zeroSha1;
        }
        String branchName = Branch.getCurrentName();
        return Utils.sha1(_tree, _logMessage, _time, branchName);
    }
}
