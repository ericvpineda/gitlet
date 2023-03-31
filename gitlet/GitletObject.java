package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/* Interface to save objects to disk */
public interface GitletObject extends Serializable {

    /** Helper function that will call overloaded write call in GitletObject */
    void write() throws IOException;

    /** Create SHA1 for file or folder */
    String createSha1();

    /** Writes content into Main.objects file */
    default void write(String sha1, Object contents, File location) throws IOException {

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
