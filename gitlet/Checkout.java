package gitlet;

import java.io.IOException;
import java.util.HashMap;

// Note:
// - changes all _history files

/* Class for checkout command */
public class Checkout {

    /**
     * Method that implements Reset command
     */
    public static void reset(String commitID) throws IOException {
        if (Utils.checkUntrackedCwd()) {
            System.out.print("There is an untracked file in the way; " +
                    "delete it, or add and commit it first.");
            return;
        }
        Commit givenCom = Commit.getByID(commitID);
        if (givenCom == null) {
            System.out.print("No commit with that id exists.");
            return;
        }
        // Note: given commit exists here
        HashMap<String, String> givenBlobList = Commit.getBlobs(givenCom._tree);
        // 1. checks out all files tracked by commit && remove tracked files not being tracked in given commit
        Utils.replaceCwdFiles(givenBlobList);
        // 3. Move current branch pointer && head pointer to commit
        Stage.clear();
        // Note: if the commits's blobs are empty (ex: initial commit) ?
        Branch.update(givenCom._sha1, Branch.getCurrentName(), Main.BRANCH);
        }

    /**
     * Takes version of file as exist in HEAD commit and puts it in CWD
     */
    public static void overwriteFile(String fileName) throws IOException {
        // Note: Get head commit based on Main.HEAD (personal check)
        HashMap<String,String> currCommit = Commit.getCurrentBlobs();
        // Notes: check if files does not exist in previous commit
        if (!currCommit.containsKey(fileName)) {
            System.out.print("File does not exist in that commit.");
            return;
        }
        // Note: takes version of file (as exists in head commit and puts CWD
        Utils.overwriteHelper(fileName, currCommit.get(fileName));
    }

    /**
     * Takes version of file as exists in commit ID and put into CWD
     */
    public static void overwriteCommit(String fileName, String commitID) throws IOException {
        // Note: Check if commit exists in given commit
        Commit commit = Commit.getByID(commitID);
        if (commit == null) {
            System.out.print("No commit with that id exists.");
            return;
        }
        // Notes: Check if file exist in given commit
        HashMap<String, String> current = Commit.getBlobs(commit._tree);
        if (current.containsKey(fileName)) {
            Utils.overwriteHelper(fileName, current.get(fileName));
        } else {
            System.out.print("File does not exist in that commit.");
        }
    }
//
    /**
     * Takes files of at head of given branch, put into CWD
     */
    public static void overwriteBranch(String branchName) throws IOException {
        // Note: given branch
        // 1. Checks if working file in current branch untracked
        if (Utils.checkUntrackedCwd()) {
            System.out.print("There is an untracked file in the way; " +
                    "delete it, or add and commit it first.");
            return;
        }
        String branchSha1;
        if (branchName.contains("/")) {
            int i = branchName.indexOf("/");
            branchName = branchName.substring(0,i) + "_" + branchName.substring(i+1);
        }
        branchSha1 = Branch.read(branchName);
        String currBranchName = Branch.getCurrentName();
        // 1. check if branch exists
        if (branchSha1 == null) {
            System.out.print("No such branch exists.");
            return;
            // 2. checks if TARGET branch == HEAD branch
        } else if (currBranchName.equals(branchName)) {
            System.out.print("No need to checkout the current branch.");
            return;
        }
        // Note: given branch
        Commit branchCom = Commit.getByID(branchSha1);
        HashMap<String, String> branchHistory = Commit.getBlobs(branchCom._tree);
        // 3. Takes HEAD commit in given branch and replaces files in CWD
        // 4. Files tracked in current branch but not in given branch -> delete
        Utils.replaceCwdFiles(branchHistory);
        Stage.clear();
        Branch.writeHead(branchName, Main.HEAD);
    }
}

