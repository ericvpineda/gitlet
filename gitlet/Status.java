package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

/* Class for status command */
public class Status {
    static Stage stage = Stage.read();

    /**
     * Constructor for Status command
     */
    public static void print() throws IOException {
        // Print all branches created, but not removed
        printBranches();
        // Print all files staged for addition
        printStagedFiles();
        // Print all files staged for deletion
        printRemovedFiles();
        // Print all files modified since current commit
        printModifications();
        // Print all files untracked in current commit
        printUntracked();
    }

    /**
     * Print the current branches.
     */
    private static void printBranches() {
        System.out.println("=== Branches ===");
        String curr = Branch.getCurrentName();

        // Note: Branch directory never null since created during initialization
        for (File file : Main.BRANCH.listFiles()) {
            // Add symbol if branch name is current branch
            if (file.getName().equals(curr)) {
                System.out.println("*" + file.getName());
            } else {
                System.out.println(file.getName());
            }
        }
        System.out.println();
    }

    /**
     * Print staged files in current branch.
     */
    private static void printStagedFiles() {
        System.out.println("=== Staged Files ===");
        if (!stage._additions.isEmpty()) {
            Iterator iter = stage._additions.entrySet().iterator();
            for (Iterator it = iter; it.hasNext(); ) {
                Map.Entry obj = (Map.Entry) it.next();
                System.out.println(obj.getKey());
            }
        }
        System.out.println();
    }

    /**
     * Print files that are currently staged for removal.
     */
    private static void printRemovedFiles() {
        System.out.println("=== Removed Files ===");
        if (!stage._deletions.isEmpty()) {
            Iterator iter = stage._deletions.entrySet().iterator();
            for (Iterator it = iter; it.hasNext(); ) {
                Map.Entry obj = (Map.Entry) it.next();
                if (!Utils.existsInCWD((String) obj.getKey())) {
                    System.out.println(obj.getKey());
                }
            }
        }
        System.out.println();
    }

    /**
     * Print files that have been modified.
     */
    private static void printModifications() {
        System.out.println("=== Modifications Not Staged For Commit ===");

        // Get all files from current commmit
        HashMap<String,String> currBlobList = Commit.getCurrentBlobs();

        // Iterate through files from current commit
        Iterator iter = currBlobList.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            String fileName = (String) obj.getKey();
            String committedFileHash = (String) obj.getValue();
            String workingDirectoryFileHash = Utils.findByFileName(fileName);
            // 1. Tracked in current commit, changed in current working directory, but not staged
            if (workingDirectoryFileHash != null && !committedFileHash.equals(workingDirectoryFileHash) &&
                !stage._additions.containsValue(workingDirectoryFileHash)) {
                System.out.println(fileName + " (modified)");
            }
            // 2. tracked current commit, not staged for removal, deleted in CWD
            if (workingDirectoryFileHash == null && !stage._deletions.containsKey(fileName)){
                System.out.println(fileName + " (deleted)");
            }
        }
        // Iterate through files stage for addition
        iter = stage._additions.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            String fileName = (String) obj.getKey();
            String committedFileHash = (String) obj.getValue();
            String workingDirectoryFileHash = Utils.findByFileName(fileName);
            // 2. File staged, but different contents in current working directory
            if (workingDirectoryFileHash != null && !workingDirectoryFileHash.equals(committedFileHash)) {
                System.out.println(fileName + " (modified)");
            }
            // 3. File staged, but deleted in current working directory
            if (workingDirectoryFileHash == null) {
                System.out.println(fileName + " (deleted)");
            }
        }
        System.out.println();
    }

    /**
     * Print files in current working directory that are not tracked.
     */
    static void printUntracked() {
        System.out.print("=== Untracked Files ===");
        ArrayList<String> filesToIgnore = Utils.IGNORE_FILES;
        // Get all files from current commit
        HashMap<String, String> trackedFiles = Commit.getCurrentBlobs();
        // Note: iterating over user directory
        for (File file : Main.USERDIR.listFiles()) {
            String fileName = file.getName();
            // Ignore files from files to ignore
            if (filesToIgnore.contains(fileName) ||
                file.isDirectory()) {
                continue;
            }
            // Create temporary file to extract name and hash
            Blob temp = new Blob(file);
            // Check if file name exists in tracked files
            if (trackedFiles.containsKey(temp._name)) {
                continue;
            }
            // Check if file is already staged for addition
            if (stage._additions.containsValue(temp._sha1)) {
                continue;
            }
            System.out.println();
            System.out.print(fileName);
        }
        System.out.println();
    }
}