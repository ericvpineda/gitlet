package gitlet;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
Test cases with greater-depth testing (i.e. failure/conflict cases) [Contains tests 1-22]
 Tests commands: init, commit, log, status, remove
 */
public class UnitTest1 {
    
    // Before each test, clear current working directory (CWD) and initialize gitlet repository
    @Before 
    public void initialize() throws IOException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
    }

    // 1. Test init command creates correct files
    @Test
    public void initTest() {

        // Check gitlet, commit, blob, tree, and head files exist
        assertTrue(Main.GITLET.exists());
        assertTrue(Main.COMMITS.exists());
        assertTrue(Main.BLOB.exists());
        assertTrue(Main.TREE.exists());
        assertTrue(Main.HEAD.exists());

        // Check zero SHA1 commit is created
        assertEquals(Commit.zeroSha1, Commit.getCurrentID());

        // Check master branch is current branch
        assertEquals("master", Branch.getCurrentName());

        // Check stage is clean
        assertNotNull(Stage.read());
    }

    // 2. Test init failure case: User attempts to create duplicate gitlet repository
    @Test
    public void initDuplicateTest() throws IOException {

        // Create duplicate repository (Note: @Before already creates a repository)
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("init");
        output.close();

        assertEquals("A Gitlet version-control system already exists in the current directory.", output.toString());
    }

    // 3. Test add, modify, and checkout back to current commit
    @Test
    public void checkoutTest() throws IOException {

        // Create and commit new file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit", "added wug");

        // Update contents of file
        Utils.randomChangeFileContents("wug.txt");

        // Verify updated contents files and previous version different
        Commit currentCommit = Commit.getCurrent();
        HashMap<String, String> currentCommitBlobList = Tree.getBlobs(currentCommit._tree);
        String previousFileSHA1 = currentCommitBlobList.get("wug.txt");

        File file = Utils.join(Main.USERDIR, "wug.txt");
        String currentFileSHA1 = new Blob(file)._sha1;

        assertNotEquals(previousFileSHA1, currentFileSHA1);

        // Checkout file to version in current commit
        Main.main("checkout", "--", "wug.txt");

        // Check file is empty
        file = Utils.join(Main.USERDIR, "wug.txt");
        assertTrue(file.length() == 0);
    }

    // 4. Test set up chain of commits and check log
    @Test
    public void basicLogTest() throws IOException {

        // Create and commit file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit", "added wug");

        // Create and commit another file
        Utils.createEmptyFile("notwug.txt");
        Main.main("add", "notwug.txt");
        Main.main("commit", "added notwug");

        Log.printLog();
    }

    // 5. Test checkout to previous version of file in current commit
    @Test
    public void CheckoutPreviousFileTest() throws IOException {

        // Create and commit new file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Commit commit1 = new Commit("added wug", Commit.getCurrentID());
        commit1.write();

        // Update and commit previous file
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit", "updated wug");

        // Checkout file from commit1
        Main.main("checkout", commit1._sha1, "--", "wug.txt");
        File file = Utils.join(Main.USERDIR, "wug.txt");

        assertTrue(file.length() == 0);
    }

    // ----- Note: Tests 5-10 not available -----

    // 11. Test basic status
    @Test
    public void statusTest() throws IOException {
        Status.print();
    }

    // 12. Test status command with 2 file adds
    @Test
    public void statusWithTwoAddedFilesTest() throws IOException, ClassNotFoundException {

        // Stage two new files
        Utils.createEmptyFile("wug.txt");
        Utils.createEmptyFile("baker.txt");
        Main.main("add", "wug.txt");
        Main.main("add", "baker.txt");

        // Print status of result
        Main.main("status");
    }

    // 13. Test status command when remove tracked file
    @Test
    public void statusWithRemovedFileTest() throws IOException {

        // Create and commit new file
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");
        Main.main("commit", "first commit");

        // Stage file for removal
        Main.main("rm", "beaver.txt");

        // Print status of result
        Main.main("status");
    }

    // 14. Test status command when add 2 files and remove one
    @Test
    public void addRemoveStatusTest() throws IOException {

        // Create and add two new files to stage
        Utils.createEmptyFile("beaver.txt");
        Utils.createEmptyFile("wug.txt");
        Main.main("add","beaver.txt");
        Main.main("add","wug.txt");

        // Stage one file for removal
        Main.main("rm","beaver.txt");

        // Check file still exists in current working directory (since not committed)
        assertTrue(Utils.join(Main.USERDIR, "beaver.txt").exists());

        // Check file is not staged for addition
        assertFalse(Stage.read()._additions.containsKey("beaver.txt"));

        // Check other file has not been altered
        assertTrue(Stage.read()._additions.containsKey("wug.txt"));

        // Check no files staged for removal
        assertTrue(Stage.read()._deletions.isEmpty());

        // Print status
        Main.main("status");
    }

    // 15. Test add and commit 2 files, remove 1, check status.
    //     Then, restore file and re-add file (should remove mark to remove file)
    @Test
    public void statusSeriesTest() throws IOException, ClassNotFoundException {

        // Create and commit two files
        Utils.createEmptyFile("beaver.txt");
        Utils.createEmptyFile("wug.txt");
        Main.main("add","beaver.txt");
        Main.main("add","wug.txt");
        Main.main("commit","added beaver and wug");

        // Remove one file
        Main.main("rm","beaver.txt");

        // Check status
        Main.main("status");

        // Restore and re-add file
        Main.main("checkout","--", "beaver.txt");
        Main.main("add","beaver.txt");

        assertFalse(Stage.read()._deletions.containsKey("beaver.txt"));
        assertFalse(Stage.read()._additions.containsKey("beaver.txt"));

        // Re-check status
        Main.main("status");
    }

    // 16. Test commit with no changes
    @Test
    public void emptyCommitTest() throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("commit", "first commit");
        output.close();

        assertEquals("No changes added to the commit.", output.toString());

    }

    // 17. Test commit with blank message
    @Test
    public void emptyCommit() throws IOException{

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("commit","  ");
        output.close();

        assertEquals("Please enter a commit message.", output.toString());
    }

    // 18. Test adding already tracked and unchanged file has not effect status
    @Test
    public void nopAddTest() throws IOException {

        // Create and stage new file
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");

        // Attempt to re-add same file
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("add", "beaver.txt");
        output.close();

        assertEquals("", output.toString());
    }

    // 19. Test non-existent file correct error
    @Test
    public void stagingFileDNETest() throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("add", "beaver.txt");
        output.close();

        assertEquals("File does not exist.", output.toString());
    }

    // Note: Q20 - check commit clears staging area
    @Test
    public void stageClearedAfterCommitTest() throws IOException {

        // Create and commit new file
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");
        Main.main("commit", "first commit");

        // Check stage and deletion stage are empty
        assertTrue(Stage.read()._additions.isEmpty());
        assertTrue(Stage.read()._deletions.isEmpty());
    }

    // 21. Test remove untracked file correct err
    @Test
    public void removeUntrackedFileTest() throws IOException {

        // Create and remove file
        Utils.createEmptyFile("beaver.txt");

        // Attempt to remove new file
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("rm", "beaver.txt");
        output.close();

        assertEquals("No reason to remove the file.", output.toString());
    }

    // 22. Test remove and unstage file thats manually deleted
    @Test
    public void removeDeletedTest() throws IOException {

        // Create and commit file
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");
        Main.main("commit", "first commit");

        // Delete file from current working directory
        Utils.join(Main.USERDIR,"beaver.txt").delete();

        // Remove file
        Main.main("rm", "beaver.txt");

        // Check file is staged for deletion
        HashMap<String, String> h = Stage.read()._deletions;
        assertTrue(h.containsKey("beaver.txt"));
    }
}


