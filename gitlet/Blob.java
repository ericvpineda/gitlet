package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Class for Blob objects
 */
public class Blob implements GitletObject, Serializable {
    String _sha1;
    String _name;
    String _fileContent;

    /**
     * Blob constructor
     */
    public Blob(File file) {
        this._name = file.getName();
        this._fileContent = Utils.readContentsAsString(file);
        this._sha1 = createHash();
    }

    /**
     * Write blob to disk
     */
    @Override
    public void write() throws IOException {
        File innerFile = Utils.join(Main.BLOB, _sha1.substring(0,2));
        innerFile.mkdir();
        File loc = Utils.join(Main.BLOB, _sha1.substring(0,2), _sha1.substring(2));
        loc.createNewFile();
        Utils.writeObject(loc, this);
    }

    /**
     * Create sha1 of blob
     */
    public String createHash() {
        return Utils.sha1(_fileContent, _name);
    }
}





