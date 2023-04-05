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

    /** Writes content into objects file in .gitlet folder */
    default void writeToDisk(String sha1, Object contents, File location) throws IOException {

        // Create folder based on first two characters of file identifier
        File innerFile = Utils.join(location,sha1.substring(0,2));
        innerFile.mkdir();

        // Create file inside of previously created folder
        File loc = Utils.join(location,sha1.substring(0,2),sha1.substring(2));
        loc.createNewFile();

        // Write file contents to disk
        Utils.writeObject(loc, (Serializable) contents);
    }
}
