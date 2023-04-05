package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/* Interface to save objects to disk */
public interface GitletObject extends Serializable {

    /** Write data to disk */
    void write() throws IOException;

    /** Create SHA1 hash */
    String createHash();

    /** Writes content into objects file in .gitlet folder using first two character of SHA1 id as folder */
    default void writeToDisk(String id, Object contents, File location) throws IOException {

        // Create folder based on first two characters of file identifier
        File innerFile = Utils.join(location,id.substring(0,2));
        innerFile.mkdir();

        // Create file inside of previously created folder
        File loc = Utils.join(location,id.substring(0,2),id.substring(2));
        loc.createNewFile();

        // Write file contents to disk
        Utils.writeObject(loc, (Serializable) contents);
    }
}
