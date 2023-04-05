package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/* Class for Tree object. This object is saved with to-be-created Commit. */
public class Tree implements GitletObject, Serializable {

    HashMap<String, String> _blobList;  // List of blobs that tree currently holds
    String _sha1;                       // Tree identifier

    /** Constructor for general Tree objects */
    public Tree() {
        _blobList = getBlobList();      // List of blobs that tree currently holds
        _sha1 = createHash();           // Tree identifier
    }

    /** Get updated list of blobs that are staged for addition. Remove blobs that are staged for deletion. */
    public HashMap<String, String> getBlobList() {
        HashMap<String,String> blobList = Commit.getCurrentBlobs();
        // Note: Stage never null since created in initialization
        Stage stage = Stage.read();
        blobList.putAll(stage._additions);
        blobList.keySet().removeAll(stage._deletions.keySet());
        return blobList;
    }

    /** Get list of blobs from given tree SHA1 */
    public static HashMap<String,String> getBlobs(String treeID) {
        File file = Utils.createFilePath(treeID, Main.TREE);
        if (file != null) {
            Tree tree = Utils.readObject(file, Tree.class);
            return tree._blobList;
        }
        return new HashMap<>();
    }

    /** Write Tree object to disk */
    public void write() throws IOException {
        writeToDisk(_sha1, this, Main.TREE);
    }

    /** Create Tree identifier */
    public String createHash() {
        String branchName = Branch.getCurrentName();
        return Utils.sha1(_blobList.toString(), branchName);
    }
}


