package gitlet;

import java.io.*;
import java.util.HashMap;


/* Class for add command and remove command */
public class Stage implements Serializable {
    HashMap<String, String> _preStage;
    HashMap<String, String> _deletion;

    /**
     * Constructor
     */
    public Stage() {
        _preStage = new HashMap<>(); // Note: map name,sha1
        _deletion = new HashMap<>(); // Note: map name,sha1
    }

    /**
     * Adds files to Main.INDEX.txt by name
     */
    public static void add(String fileName) throws IOException {
        File temp = Utils.join(Main.USERDIR, fileName);
//      Note: check if file has been deleted cwd
        if (!temp.exists()) {
            if (restore(fileName)) {
                // ---------- Check more info on this?? -----------
//                Checkout.overwriteFile(fileName);
                return;
            }
            System.out.print("File does not exist.");
            return;
        }
        Blob newBlob = new Blob(temp);
        // Note: Checks if there is identical version of file in current commit
        if (checkIdenticalVer(newBlob)) {
            removeFromIndex(newBlob._name, Stage.read());
            return;
        }
        newBlob.write();
        // Note: Need to manually write blobs to file
        HashMap<String, String> newStage = new HashMap<>();
        newStage.put(newBlob._name, newBlob._sha1);
        // Note: reads Index and adds items to new HM
        Stage prev = Stage.read();
        if (prev._preStage.containsKey(newBlob._name) &&
            prev._preStage.get(newBlob._name).equals(newBlob._sha1)) {
            // Note: if the stage already contains the file
            return;
        }
        if (prev._deletion.containsValue(newBlob._sha1)) {
            restore(newBlob._name);
        }
        prev._preStage.putAll(newStage);
        write(prev);
    }

    /**
     * Remove file command
     */
    public static void remove(String name) throws IOException {
        // Note: Remove requirement #1
        Stage stage = Stage.read();
        boolean removedOutput = removeFromIndex(name, stage);
        // Note: Remove requirement #2
        boolean delatedOutput = markDeleted(name, stage);
        if (!removedOutput && !delatedOutput) {
            System.out.print("No reason to remove the file.");
        }
    }

    /** Helper method to remove file from Index */
    private static boolean removeFromIndex(String name, Stage stage) {
        if (stage._preStage.containsKey(name) ||
                 stage._deletion.containsKey(name)) {
            stage._preStage.remove(name);
            stage._deletion.remove(name);
            Utils.writeObject(Main.INDEX, stage);
            return true;
        }
        return false;
    }

    /**
     * Helper method to mark file as deleted
     */
    private static boolean markDeleted(String fileName, Stage stage) {
        HashMap<String,String> currentBlobList = Commit.getCurrentBlobs();
        // Note: only removes the file if in in the commit
        if (currentBlobList.containsKey(fileName)) {
            // Note: remove from cwd
            Utils.join(Main.USERDIR,fileName).delete();
            stage._deletion.put(fileName,currentBlobList.get(fileName));
            write(stage);
            return true;
        }
        return false;
    }

    /** Remove file from deleted if file is restored */
    public static boolean restore(String name) {
        Stage stage = read();
        if (stage._deletion.containsKey(name)) {
            stage._deletion.remove(name);
            write(stage);
            return true;
        }
        return false;
    }

    /**
     * Checks if identical blob exists
     */
    public static boolean checkIdenticalVer(Blob blob) {
        HashMap<String, String> curr = Commit.getCurrentBlobs();
        if (curr.containsKey(blob._name) &&
                curr.get(blob._name).equals(blob._sha1)) {
            return true;
        }
        return false;
    }

    /**
     * Read Main.INDEX.txt
     */
    public static Stage read() {
        return Utils.deserialize(Main.INDEX, Stage.class);
    }

    /**
     * Write object to Main.INDEX.txt
     */
    public static void write(Stage stage) {
        Utils.writeObject(Main.INDEX, stage);
    }

    /**
     * Clear what is currently on Main.INDEX
     */
    public static void clear() {
        Stage stage = new Stage();
        write(stage);
    }
}


