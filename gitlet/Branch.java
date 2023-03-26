package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

// Note: difference between write object vs content
/**
 * Class for Branch pointer
 */
public class Branch implements Serializable {

    /**
     * Saves a new branch file with headCommit SHA1
     */
    public static void save(String name) throws IOException {
        File newBranchFile = Utils.join(Main.BRANCH, name);
        if (newBranchFile.exists()) {
            System.out.print("A branch with that name already exists.");
            return;
        }
        newBranchFile.createNewFile();
        Utils.writeObject(newBranchFile, name);
        update(Commit.getCurrentSha1(), name, Main.BRANCH);
    }

    /**
     * Update current branch with sha1
     */
    public static void update(String sha1, String branchName, File branch) {
        File branchFile = Utils.join(branch, branchName);
        Utils.writeObject(branchFile, sha1);
    }

    public static void saveRemote(String name, File file, String sha1) throws IOException {
        File newBranchFile = Utils.join(file,name);
        if (newBranchFile.exists()) {
            System.out.print("A file with that name already exists.");
            return;
        }
        newBranchFile.createNewFile();
        Utils.writeObject(newBranchFile, name);
        update(sha1, name, file);
    }

    /**
     * Saves the current branch name at Main.HEAD
     */
    public static void savePointer(String name, File file)  {
        File newBranchFile = Utils.join(file);
        if (name.contains("/")) {
            name = name.substring(name.indexOf("/") + 1);
        }
        Utils.writeObject(newBranchFile, name);
    }

    /**
     * Get current branch name
     */
    public static String getCurrent() {
        return Utils.deserialize(Main.HEAD, String.class);
    }

    /**
     * Custom method to get the current commit the branch is pointing at
     */
    public static String read(String branchName) {
        File branchFile = Utils.findFile(branchName, Main.BRANCH);
        if (branchFile.exists()) {
            return Utils.deserialize(branchFile, String.class);
        }
        return null;

    }

    /**
     * Removes branch from Main.HISTORY & branch history from Main.STAGE
     */
    public static void remove(String branchName) {
        File branchFile = Utils.findFile(branchName, Main.BRANCH);
        String currBranch = Utils.deserialize(Main.HEAD, String.class);
        if (!branchFile.exists()) {
            System.out.print("A branch with that name does not exist.");
            return;
        } else if (currBranch.equals(branchName)) {
            System.out.print("Cannot remove the current branch.");
            return;
        }
        branchFile.delete();
    }

    /**
     * Method to list all current branches
     */
    public static void listBranches() {
        File refFile = Main.BRANCH;
        String branchHead = Branch.getCurrent();
        for (File file : refFile.listFiles()) {
            if (file.getName().equals(branchHead)) {
                System.out.println(file.getName() + "*");
                continue;
            }
            System.out.println(file.getName());

        }
        System.out.println();
    }
}
