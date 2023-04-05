package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/* Class for Tree object. This object is saved with to-be-created Commit. */
public class Tree implements GitletObject, Serializable {

    HashMap<String, String> _blobList;  // List of blobs that tree currently holds
    String _sha1;                       // Tree identifier

    /** Constructor for general Tree objects */
    public Tree() {
        _blobList = getBlobList();
        _sha1 = createHash();
    }

    /** Constructor for given list of blobs */
    public Tree(HashMap<String, String> blobList) {
        _blobList = blobList;
        _sha1 = createHash();
    }

    /** Create Tree identifier */
    public String createHash() {
        String branchName = Branch.getCurrentName();
        return Utils.sha1(_blobList.toString(), branchName);
    }

    /** Get updated list of blobs that are staged for addition. Remove blobs that are staged for deletion. */
    public HashMap<String, String> getBlobList() {
        HashMap<String,String> blobList = Commit.getCurrentBlobs();
        Stage stage = Stage.read();
        if (stage != null) {
            blobList.putAll(stage._additions);
            blobList.keySet().removeAll(stage._deletions.keySet());
        }
        return blobList;
    }

    /** Write Tree object to disk */
    public void write() throws IOException {
        writeToDisk(_sha1, this, Main.TREE);
    }
}


