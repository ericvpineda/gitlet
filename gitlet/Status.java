package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

// Note:
// - if file is removed and re added to stage -> shows up as deleted??

/* Class for status command */
public class Status {
    static Stage stage = Stage.read();

    /**
     * Constructor for Status
     */
    public static void print() throws IOException {
        printBranches();
        printStagedFiles();
        printRemovedFiles();
        printModifications();
        printUntracked();
    }

    /**
     * Print the current branches
     */
    private static void printBranches() {
        System.out.println("=== Branches ===");
        String curr = Branch.getCurrent();
        for (File file : Main.BRANCH.listFiles()) {
            if (file.getName().equals(curr)) {
                System.out.println("*" + file.getName());
            } else {
                System.out.println(file.getName());
            }
        }
        System.out.println();
    }

    /**
     * Print staged files of current branch
     */
    private static void printStagedFiles() {
        System.out.println("=== Staged Files ===");
        if (!stage._preStage.isEmpty()) {
            Iterator iter = stage._preStage.entrySet().iterator();
            for (Iterator it = iter; it.hasNext(); ) {
                Map.Entry obj = (Map.Entry) it.next();
                System.out.println(obj.getKey());
            }
        }
        System.out.println();
    }

    /**
     * Print files that are currently removed
     */
    private static void printRemovedFiles() {
        System.out.println("=== Removed Files ===");
        if (!stage._deletion.isEmpty()) {
            // Note: iterate through Main.HISTORY of branch
            Iterator iter = stage._deletion.entrySet().iterator();
            for (Iterator it = iter; it.hasNext(); ) {
                Map.Entry obj = (Map.Entry) it.next();
                if (!Utils.findInCwd((String) obj.getKey())) {
                    System.out.println(obj.getKey());
                }
            }
        }
        System.out.println();
    }

    /**
     * Print files that have been modified
     */
    private static void printModifications() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        // Note: curr branch history
        HashMap<String,String> currBlobList = Commit.getCurrentBlobs();
        Iterator iter = currBlobList.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            String blobName = (String) obj.getKey();
            String blobSHA1 = (String) obj.getValue();
            String cwdSha1 = Utils.findFileCWDsha1(blobName);
            // 1. Tracked currCom, changed in CWD, not staged
            if (cwdSha1 != null && !blobSHA1.equals(cwdSha1) &&
                !stage._preStage.containsValue(cwdSha1)) {
                System.out.println(blobName + " (modified)");
                // 2. tracked currCom, not staged for removal, deleted in CWD
            }
            if (cwdSha1 == null && !stage._deletion.containsKey(blobName)){
                System.out.println(blobName + " (deleted)");
            }
        }
        iter = stage._preStage.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            String blobName = (String) obj.getKey();
            String blobSha1 = (String) obj.getValue();
            String fileSHA1 = Utils.findFileCWDsha1(blobName);
            // 2. Staged, different content in CWD
            if (fileSHA1 != null && !fileSHA1.equals(blobSha1)) {
                System.out.println(blobName + " (modified)");
            // 3. staged, deleted in CWD
            }
            if (fileSHA1 == null) {
                System.out.println(blobName + " (deleted)");
            }
        }
        System.out.println();
    }

    /**
     * Print files in CWD that are not tracked
     */
    static void printUntracked() {
        System.out.print("=== Untracked Files ===");
        ArrayList<String> ignoreFiles = Utils.getIgnoreArray();
        HashMap<String, String> trackedFiles = Commit.getCurrentBlobs();
        // Note: iterating over user dir
        for (File file : Main.USERDIR.listFiles()) {
            String fileName = file.getName();
            if (ignoreFiles.contains(fileName) ||
                file.isDirectory()) {
                continue;
            }
            Blob temp = new Blob(file);
            // Note: file staged for removal by recreated without gitlet knowledge
            // && !stage._deletion.containsKey(fileName)
            if (trackedFiles.containsKey(temp._name)) {
                continue;
            }
            if (stage._preStage.containsValue(temp._sha1)) {
                continue;
            }
            System.out.println();
            System.out.print(fileName);
        }
        System.out.println();
    }
}