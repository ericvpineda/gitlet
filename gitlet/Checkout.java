package gitlet;

import java.io.IOException;
import java.util.HashMap;

/* Class for checkout command */
public class Checkout {

    /**
     * Reset command. Checkout all files tracked by given commit id and removed tracked files not present in commit.
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
     * Replace file contents with version in HEAD commit.
     */
    public static void overwriteFile(String fileName) throws IOException {
        // Get all blobs in current comment
        HashMap<String,String> currentCommit = Commit.getCurrentBlobs();

        // Check if file does not exist in current commit
        if (!currentCommit.containsKey(fileName)) {
            System.out.print("File does not exist in that commit.");
            return;
        }

        Utils.overwriteHelper(fileName, currentCommit.get(fileName));
    }

    /**
     * Replace file contents with version in GIVEN commit.
     */
    public static void overwriteCommit(String fileName, String commitID) throws IOException {
        // Check if commit id exists
        Commit commit = Commit.getByID(commitID);
        if (commit == null) {
            System.out.print("No commit with that id exists.");
            return;
        }
        // Check if file exist in given commit
        HashMap<String, String> current = Commit.getBlobs(commit._tree);
        if (current.containsKey(fileName)) {
            Utils.overwriteHelper(fileName, current.get(fileName));
        } else {
            System.out.print("File does not exist in that commit.");
        }
    }
//
    /**
     * Replace file contents with version in GIVEN branch.
     */
    public static void overwriteBranch(String branchName) throws IOException {
        // 1. Checks if there exists untracked file
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
        // 1. Check if branch exists
        if (branchSha1 == null) {
            System.out.print("No such branch exists.");
            return;
        // 2. Checks if TARGET branch equal to HEAD branch
        } else if (currBranchName.equals(branchName)) {
            System.out.print("No need to checkout the current branch.");
            return;
        }

        Commit branchCom = Commit.getByID(branchSha1);
        HashMap<String, String> branchHistory = Commit.getBlobs(branchCom._tree);
        // 3. Takes HEAD commit in given branch and replaces files in CWD
        // 4. Files tracked in current branch but not in given branch -> delete
        Utils.replaceCwdFiles(branchHistory);
        Stage.clear();
        Branch.writeHead(branchName, Main.HEAD);
    }
}

