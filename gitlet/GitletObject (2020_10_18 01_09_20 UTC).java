package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/* Interface to save objects to disk */
public interface GitletObject extends Serializable {

    void write() throws IOException;

    String createSha1();

    /** Writes content into Main.objects file */
    default void write(String sha1, Object contents, File location) throws IOException {
        File innerFile = Utils.join(location,sha1.substring(0,2));
        innerFile.mkdir();
        File loc = Utils.join(location,sha1.substring(0,2),sha1.substring(2));
        loc.createNewFile();
        Utils.writeObject(loc, (Serializable) contents);
    }
}
