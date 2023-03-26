package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/* Class for tree object that saved with Commit */
public class Tree implements GitletObject, Serializable {
    HashMap<String, String> _blobList;
    String _sha1;

    /**
     * Constructor
     */
    public Tree() throws IOException {
        _blobList = getPreviousFiles();
        _sha1 = createSha1();
        write();
    }

    public Tree(File remote) throws IOException {
        _blobList = new HashMap<>();
        _sha1 = createSha1();
        writeRemote(remote);
    }

    public Tree(HashMap<String, String> blobList) throws IOException {
        _blobList = blobList;
        _sha1 = createSha1();
        write();
    }

    // Note: will only create sha when there is something modified
    // - already has files added to _bloblist from stage**
    /**
     * Create Tree sha1
     */
    public String createSha1() {
        return Utils.sha1(_blobList.toString());
    }

    /**
     * Get current files of index.txt
     */
    public HashMap<String, String> getPreviousFiles() {
        if (Commit.getCurrent() == null) {
            return new HashMap<>();
        }
        HashMap<String,String> currBlobFiles = Commit.getCurrentBlobs();
        Stage index = Stage.read();
        if (index != null) {
            currBlobFiles.putAll(index._preStage); // Note: map name, sha1
            currBlobFiles.keySet().removeAll(index._deletion.keySet());
        }
        return currBlobFiles;
    }

    /**
     * Writes Tree object to disk
     */
    public void write() throws IOException {
        Commit current = Commit.getCurrent();
        if (current != null && current._tree.equals(_sha1)) {
            return;
        }
        write(_sha1, this, Main.TREE);
        }

    public void writeRemote(File remote) throws IOException {
        File tree = Utils.join(remote, "objects","trees");
        write(_sha1, this, tree);
    }
}


