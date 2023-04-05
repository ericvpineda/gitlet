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
     * Write new branch file to disk using branch name (if applicable).
     */
    public static void write(String name) throws IOException {

        // Verify that given name does not already exist
        File file = Utils.join(Main.BRANCH, name);
        if (file.exists()) {
            System.out.print("A branch with that name already exists.");
            return;
        }

        // Create new branch file and write to disk
        file.createNewFile();

        // Update current commit with new branch file
        String currentCommitID = Commit.getCurrentID();
        update(currentCommitID, name, Main.BRANCH);
    }

    /**
     * Update current branch with commit id
     */
    public static void update(String commitID, String branch, File file) {
        Utils.writeContents(Utils.join(file, branch), commitID);
    }

    /**
     * Saves the current branch name as HEAD pointer
     */
    public static void writeHead(String name, File file)  {
        File branch = Utils.join(file);
        if (name.contains("/")) {
            name = name.substring(name.indexOf("/") + 1);
        }
        Utils.writeContents(branch, name);
    }

    /**
     * Get current branch name
     */
    public static String getCurrentName() {
        return Utils.readContentsAsString(Main.HEAD);
    }

    /**
     * Get HEAD commit identifier of given branch
     */
    public static String read(String branchName) {
        File branchFile = Utils.createFilePath(branchName, Main.BRANCH);
        if (branchFile != null) {
            return Utils.readContentsAsString(branchFile);
        }
        return null;

    }

    /**
     * Removes branch from repository
     */
    public static void remove(String branchName) {
        File branchFile = Utils.createFilePath(branchName, Main.BRANCH);
        String currBranch = Utils.readContentsAsString(Main.HEAD);
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
     * List all available branches
     */
    public static void listBranches() {
        File refFile = Main.BRANCH;
        String branchHead = getCurrentName();
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
