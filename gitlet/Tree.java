package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/* Class for tree object that saved with Commit */
public class Tree implements GitletObject, Serializable {
    HashMap<String, String> _blobList;
    String _sha1;

    /** Constructor for general tree objects*/
    public Tree() {
        _blobList = getBlobList();
        _sha1 = createHash();
    }

    /** Constructor for given list of blobs */
    public Tree(HashMap<String, String> blobList) {
        _blobList = blobList;
        _sha1 = createHash();
    }

    /** Constuctor for remote command implementation */
    public Tree(File remote) throws IOException {
        _blobList = new HashMap<>();
        _sha1 = createHash();
        writeRemote(remote);
    }

    /** Create Tree identifier */
    public String createHash() {
        String branchName = Branch.getCurrent();
        return Utils.sha1(_blobList.toString(), branchName);
    }

    /** Get current files of index.txt */
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

    public void writeRemote(File remote) throws IOException {
        File tree = Utils.join(remote, "objects","trees");
        writeToDisk(_sha1, this, tree);
    }
}


