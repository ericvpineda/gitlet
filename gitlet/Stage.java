package gitlet;

import java.io.*;
import java.util.HashMap;


/* Class for add command and rm (remove) command */
public class Stage implements Serializable {
    HashMap<String, String> _additions;  // Files staged for addition
    HashMap<String, String> _deletions;  // Files staged for deletion

    /**
     * Constructor
     */
    public Stage() {
        // Note: Hashmap is configured as: {Name of file: SHA1}
        _additions = new HashMap<>();    // Files staged for addition
        _deletions = new HashMap<>();    // Files staged for deletion
    }

    /**
     * Adds file to stage if exists. If applicable, removed file staged for deletion. Does not add file is not contents has not changed.
     */
    public static void add(String fileName) throws IOException {

        // Check if file deleted or does not exist
        File file = Utils.join(Main.USERDIR, fileName);
        if (!file.exists()) {

            // Remove unknown file from stage and deletion stage if exists
            if (restore(fileName)) {
                return;
            }
            System.out.print("File does not exist.");
            return;
        }

        // Create new file blob
        Blob newBlob = new Blob(file);

        // Note: Checks if there is identical version of file in current commit
        if (isIdenticalBlob(newBlob)) {
            removeFromIndex(newBlob._name, Stage.read());
            return;
        }

        // Write file blob to disk
        newBlob.write();

        // Create new stage and add blob
        HashMap<String, String> newStage = new HashMap<>();
        newStage.put(newBlob._name, newBlob._sha1);

        // Check if previous stage contains same copy of added file
        Stage previousStage = Stage.read();
        if (previousStage._additions.containsKey(newBlob._name) &&
            previousStage._additions.get(newBlob._name).equals(newBlob._sha1)) {
            return;
        }

        // Check if file staged for deletion, remove form deletion hashmap
        if (previousStage._deletions.containsValue(newBlob._sha1)) {
            restore(newBlob._name);
        }

        // Update previous stage with current stage contents
        previousStage._additions.putAll(newStage);

        // Write update stage to disk
        write(previousStage);
    }

    /**
     * rm (remove) file command
     */
    public static void remove(String name) throws IOException {
        // Remove file from previous stage and deletion stage
        Stage stage = Stage.read();
        boolean isFileRemoved = removeFromIndex(name, stage);

        // Mark file as deleted
        boolean isFileMarkDeleted = markDeleted(name, stage);

        // Check if file is removed and deleted successfully
        if (!isFileRemoved && !isFileMarkDeleted) {
            System.out.print("No reason to remove the file.");
        }
    }

    /** Helper method to remove file from Stage */
    private static boolean removeFromIndex(String name, Stage stage) {

        // If stage and deletion stage contains file, remove the file and update stage to disk.
        if (stage._additions.containsKey(name) ||
                 stage._deletions.containsKey(name)) {
            stage._additions.remove(name);
            stage._deletions.remove(name);
            Utils.writeObject(Main.STAGE, stage);
            return true;
        }
        return false;
    }

    /**
     * Helper method to mark file as deleted
     */
    private static boolean markDeleted(String fileName, Stage stage) {

        // Get list of blobs from current comment
        HashMap<String,String> currentBlobList = Commit.getCurrentBlobs();

        // If commit contains file, stage file for deletion and write stage to disk.
        if (currentBlobList.containsKey(fileName)) {

            // Remove file from current working directory (CWD)
            Utils.join(Main.USERDIR,fileName).delete();

            // Stage file for deletion
            stage._deletions.put(fileName,currentBlobList.get(fileName));
            write(stage);
            return true;
        }
        return false;
    }

    /** Remove file from being staged for deletion */
    public static boolean restore(String name) {
        Stage stage = read();
        if (stage._deletions.containsKey(name)) {
            stage._deletions.remove(name);
            write(stage);
            return true;
        }
        return false;
    }

    /**
     * Checks if given Blob is identical to that in current commit
     */
    public static boolean isIdenticalBlob(Blob blob) {
        HashMap<String, String> curr = Commit.getCurrentBlobs();
        return curr.containsKey(blob._name) && curr.get(blob._name).equals(blob._sha1);
    }

    /** Read Stage from disk */
    public static Stage read() {
        // Note: Never returns null since stage saved do disk during initialization
        return Utils.readObject(Main.STAGE, Stage.class);

    }

    /**
     * Write Stage to disk
     */
    public static void write(Stage stage) {
        Utils.writeObject(Main.STAGE, stage);
    }

    /**
     * Clear current Stage and write new stage to disk.
     */
    public static void clear() {
        write(new Stage());
    }

    // Check if preStage hashmap is empty
    public boolean isAdditionsEmpty() {
        return _additions.isEmpty();
    }

    // Check if deletion stage hashmap is empty
    public boolean isDeletionsEmpty() {
        return _deletions.isEmpty();
    }
}


