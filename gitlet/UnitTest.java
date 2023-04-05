package gitlet;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


// Test basic gitlet commands with shallow depth [32 Tests Total]
// Tests include: init, add, commit, remove, log, global-log, find, status, checkout, branch, rm-branch, merge
public class UnitTest {

    // Clear current working directory and initialize .gitlet repo
    @Before
    public void initialize() throws IOException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
    }

    // ----- INIT TESTS -----

    // Test initial commit valid and no blobs exist
    @Test
    public void initTest() {
        assertTrue(Main.GITLET.exists());
        assertTrue(Main.HEAD.exists());

        Commit currentCommit = Commit.getCurrent();
        assertNotNull(currentCommit);
        assertEquals(Commit.zeroSha1, Commit.getCurrentID());
        assertNotNull(currentCommit._tree);
        assertEquals(0, Commit.getCurrentBlobs().size());
    }

    // ----- ADD TESTS -----

    // Test different files types properly added to stage
    @Test
    public void addTest() throws IOException {

//        Utils.createEmptyFile("cube.txt");
//        Stage stage = Stage.read();
//        System.out.println("DEBUG: stage=" + stage);
//        assertTrue(stage._preStage.isEmpty());
//        assertTrue(stage._deletion.isEmpty());
//
//        // Check if news files added to stage
//        Main.main("add","cube.txt");
//        stage = Stage.read();
//        assertTrue(stage._preStage.containsKey("cube.txt"));

//        // Check adding file that DNE
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        Main.main("add","map.txt");
//        output.close();
//
//        assertEquals("File does not exist.", output.toString());
    }

//
//    // Test user input of staging file
//    @Test
//    public void addTest2() throws IOException {
//
//        Utils.createEmptyFile("cube.txt");
//        Stage stage = Stage.read();
//        assertTrue(stage._preStage.isEmpty());
//        assertTrue(stage._deletion.isEmpty());
//
//        // User manually adds file to stage
//        Main.main("add","cube.txt");
//
//        // Check file in staging
//        stage = Stage.read();
//        assertTrue(stage._preStage.containsKey("cube.txt"));
//    }
//
//
//    // ----- COMMIT TESTS -----
//
//    // Test basic commit flow
//    @Test
//    public void commitTest() throws IOException {
//
//        // Create commit of new file
//        Utils.createEmptyFile("wug.txt");
//        Main.main("add","wug.txt");
//        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentID());
//        firstCommit.write();
//
//        // Check if commit folder updated, commit tree contains files, head changes
//        assertEquals(2, Main.COMMITS.listFiles().length);
//        HashMap<String, String> commitBlobs = Commit.getCurrentBlobs();
//
//        assertTrue(commitBlobs.containsKey("wug.txt"));
//        assertTrue(Stage.read() != null);
//
//        // Test HEAD is pointing to master branch
//        assertEquals("master", Utils.deserialize(Main.HEAD, String.class));
//
//        // Check commit SHA1 matches current commit SHA1
//        assertEquals(firstCommit._sha1, Commit.getCurrentID());
//
//        // Check added files does not change commit blobs
//        Utils.createEmptyFile("notwug.txt");
//        commitBlobs = Commit.getBlobs(firstCommit._tree);
//        assertFalse(commitBlobs.containsKey("notwug.txt"));
//
//        // Check total SHA1's commits have
//        ArrayList<String> commitHistory = Utils.getTotalSha1History(firstCommit, new ArrayList<>());
//        assertTrue(commitHistory.contains(firstCommit._sha1));
//        assertTrue(commitHistory.size() == 2);
//    }
//
//    // Test having two committing actions
//    @Test
//    public void commitTest2() throws IOException {
//
//        // Create and commit random file
//        Utils.createEmptyFile("wug.txt");
//        Main.main("add", "wug.txt");
//        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentID());
//        firstCommit.write();
//
//        // Create and commit another random file
//        Utils.createEmptyFile("notwug.txt");
//        Main.main("add", "notwug.txt");
//
//        // Check wug.txt not in stage, notwug.txt in stage
//        assertFalse(Stage.read()._preStage.containsKey("wug.txt"));
//        assertTrue(Stage.read()._preStage.containsKey("notwug.txt"));
//
//        // Check if stage cleared, current commit pointer updated
//        Commit currentCommit = new Commit("added not wug.txt", Commit.getCurrentID());
//
//        // Check new commit has not been committed yet
//        assertNotEquals(currentCommit._sha1, Commit.getCurrentID());
//
//        // Commit new commit
//        currentCommit.write();
//
//        // Check new commit is current ocmmit
//        assertEquals(currentCommit._sha1, Commit.getCurrentID());
//
//        // Check valid parent commit of new commit
//        assertEquals(currentCommit._parentSha1, firstCommit._sha1);
//
//        // Check commit metadata is correct
//        assertEquals("added not wug.txt", currentCommit._logMessage);
//        assertNotEquals(firstCommit._tree, currentCommit._tree);
//    }
//
//    // Test staging unmodified file that has already been committed
//    @Test
//    public void commitTest3() throws IOException {
//
//        // Stage and commit random file
//        Utils.createEmptyFile("cube.txt");
//        Main.main("add","cube.txt");
//        Main.main("commit","added cube");
//        Stage stage = Stage.read();
//
//        // Check stage does not have file
//        assertFalse(stage._preStage.containsKey("cube.txt"));
//
//        // Re-stage same file again
//        Main.main("add","cube.txt");
//        stage = Stage.read();
//
//        // Check file is not staged since not changes have been made
//        assertFalse(stage._preStage.containsKey("cube.txt"));
//    }
//
//    // Test commit failure cases
//    @Test
//    public void commitTest4() throws IOException {
//
//        // Create random file
//        Utils.createEmptyFile("wug.txt");
//
//        // 1. Failure case: no file added
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        Main.main("commit", "added wug.txt");
//        output.close();
//
//        assertEquals("No changes added to the commit.", output.toString());
//
//        // 2. Failure case: Empty commit message
//        output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        Main.main("commit");
//        output.close();
//
//        assertEquals("Please enter a commit message.", output.toString());
//    }
//
//    // ----- REMOVE TESTS -----
//
//    // Test successfully removing committed file from current commit
//    @Test
//    public void removeTest() throws IOException {
//
//        // Create random file and commit
//        Utils.createEmptyFile("wug.txt");
//        Main.main("add","wug.txt");
//        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentID());
//        firstCommit.write();
//
//        // Check file is removed from stage and is in deletion hashmap
//        Stage.remove("wug.txt");
//        Stage stage = Stage.read();
//        assertTrue(stage._preStage.isEmpty());
//        assertTrue(stage._deletion.containsKey("wug.txt"));
//
//        // Commit removal of file
//        Commit second = new Commit("removed wug.txt", Commit.getCurrentID());
//        second.write();
//
//        // Check stage is empty and deletion hashmap does not have file
//        stage = Stage.read();
//        assertTrue(stage._preStage.isEmpty());
//        assertFalse(stage._deletion.containsKey("wug.txt"));
//
//        // Check file untracked in current commit
//        HashMap<String, String> secondCommit = Commit.getBlobs(second._tree);
//        assertFalse(secondCommit.containsKey("wug.txt"));
//        assertTrue(secondCommit.isEmpty());
//    }
//
//    // ----- LOG TESTS -----
//
//    // Test log run successfully
//    @Test
//    public void logTest() throws IOException {
//
//        Main.main("log");
//    }
//
//    // ----- GLOBAL LOG TESTS -----
//
//    // Test global log runs successfully
//    @Test
//    public void globalLogTest() throws IOException {
//
//        Main.main("global-log");
//    }
//
//    // ----- FIND TESTS -----
//
//    // Test find command to get commit ids of given commit message
//    @Test
//    public void findTest() throws IOException {
//
//        // Create and commit random file
//        Utils.createEmptyFile("wug.txt");
//        Main.main("add", "wug.txt");
//        Main.main("commit","added wug.txt");
//
//        // Use find command and check if retrieved commit is correct
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        Main.main("find", "added wug.txt");
//        output.close();
//
//        assertEquals(Commit.getCurrentID(), output.toString());
//    }
//
//    // ----- STATUS TESTS -----
//
//    // Test status of branches and files with different stagings
//    @Test
//    public void statusTest() throws IOException {
//
//        // Create and commit random files
//        Utils.createEmptyFile("goodbye.txt");
//        Utils.createEmptyFile("junk.txt");
//        Utils.createEmptyFile("wug3.txt");
//        Main.main("add", "goodbye.txt");
//        Main.main("add", "junk.txt");
//        Main.main("add", "wug3.txt");
//        Main.main("commit", "added wug");
//        HashMap<String, String> currentBlobs = Commit.getCurrentBlobs();
//
//        // Check current commit has all staged files
//        assertTrue(currentBlobs.containsKey("goodbye.txt"));
//        assertTrue(currentBlobs.containsKey("junk.txt"));
//        assertTrue(currentBlobs.containsKey("wug3.txt"));
//
//        // Create new branch
//        Main.main("branch", "other-branch");
//
//        // Add new files
//        Utils.createEmptyFile("wug.txt");
//        Utils.createEmptyFile("wug2.txt");
//        Main.main("add", "wug.txt");
//        Main.main("add", "wug2.txt");
//        Main.main("rm", "goodbye.txt");
//
//        // Check stage has new added files
//        Stage stage = Stage.read();
//        assertTrue(stage._preStage.containsKey("wug.txt"));
//        assertTrue(stage._preStage.containsKey("wug2.txt"));
//        assertTrue(stage._deletion.containsKey("goodbye.txt"));
//
//        // Delete previously committed file
//        Utils.join(Main.USERDIR, "junk.txt").delete();
//        assertFalse(Utils.join(Main.USERDIR, "junk.txt").exists());
//
//        // Check stage has deleted file in correct mapping
//        stage = Stage.read();
//        assertFalse(stage._deletion.containsKey("junk.txt"));
//
//        // Initiate status command
//        Status.print();
//    }
//
//    // Test status for file that was committed, removed but not committed, then re-added
//    @Test
//    public void statusTest1() throws IOException {
//
//        // Create and commit random file
//        Utils.createEmptyFile("goodbye.txt");
//        Main.main("add", "goodbye.txt");
//        Main.main("commit", "added wug");
//
//        // Remove random file
//        Stage.remove("goodbye.txt");
//        Stage stage = Stage.read();
//        assertTrue(stage._deletion.containsKey("goodbye.txt"));
//
//        // Re-add random file
//        Main.main("add","goodbye.txt");
//
//        // Random file should be in staged hashmap and should not exist in deletion hashmap
//        stage = Stage.read();
//        assertFalse(stage._deletion.containsKey("goodbye.txt"));
//        assertFalse(stage._preStage.containsKey("goodbye.txt"));
//
//        // Print status command
//        Status.print();
//    }
//
//    // ----- CHECKOUT TESTS -----
//
//    // Test checkout of file that had contents changed, file that does not exist (DNE), and file removed
//    @Test
//    public void checkoutTest() throws IOException {
//
//        // Create and commit random file
//        Utils.createEmptyFile("wug.txt");
//        Main.main("add","wug.txt");
//        Main.main("commit", "added dog");
//
//        // Change contents of random file
//        Utils.randomChangeFileContents("wug.txt");
//
//        // Checkout random file
//        Main.main("checkout", "--", "wug.txt");
//        assertTrue(Utils.join(Main.USERDIR, "wug.txt").length() == 0);
//
//        // Checkout failure case: file that DNE
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//
//        // Checkout file DNE
//        Main.main("checkout", "--", "wugz.txt");
//        output.close();
//
//        assertEquals("File does not exist in that commit.", output.toString());
//
//        // Checkout file that has been removed
//        Main.main("rm", "wug.txt");
//        Main.main("checkout", "--", "wug.txt");
//    }
//
//    // Test checkout failure cases of checkout to commit that DNE, and checkout file that DNE
//    @Test
//    public void checkoutTest2() throws IOException {
//
//        // Create and commit random file
//        Utils.createEmptyFile("wug.txt");
//        Main.main("add", "wug.txt");
//        Commit commit1 = new Commit("version 1 of wug.txt", Commit.getCurrentID());
//        commit1.write();
//
//        // Update random file contents
//        Utils.randomChangeFileContents("wug.txt");
//        Main.main("add", "wug.txt");
//        Main.main("add", "version 2 of wug.txt");
//
//        // Checkout random file
//        Main.main("checkout", Commit.zeroSha1, "--", "wug.txt");
//        Main.main("checkout", "--", "wugz.txt");
//        assertTrue(Utils.join(Main.GITLET, "wug.txt").length() == 0);
//
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//
//        // Failure case: Checkout to commit that DNE
//        Main.main("checkout", "wugz.txt", "--", "0023");
//        output.close();
//
//        assertEquals("No commit with that id exists.", output.toString());
//
//        // Failure case: checkout to file that DNE in commit
//        output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//
//        Main.main("checkout", commit1._sha1.substring(0,7), "--", "wugz.txt");
//
//        output.close();
//        assertEquals("File does not exist in that commit.", output.toString());
//    }
//
//    // Test checkout branch
//    @Test
//    public void checkoutTest3() throws IOException {
//
//        // Create and commit random files
//        Utils.createEmptyFile("wug.txt");
//        Utils.createEmptyFile("wug2.txt");
//        Main.main("add", "wug.txt");
//        Main.main("add", "wug2.txt");
//        Commit commit1 = new Commit("added wug and wug2", Commit.getCurrentID());
//        commit1.write();
//
//        // Create and checkout new branch
//        Main.main("branch", "Serf");
//        Main.main("checkout", "Serf");
//
//        // Check branch preserves previous files and commits
//        HashMap<String, String> blobList = Commit.getCurrentBlobs();
//        assertTrue(blobList.containsKey("wug.txt"));
//        assertTrue(blobList.containsKey("wug2.txt"));
//        assertEquals("Serf", Branch.getCurrent());
//        assertEquals(commit1._sha1, Commit.getCurrentID());
//
//        // Checkout master branch
//        Main.main("checkout", "master");
//
//        // Create and commit new file
//        Utils.createEmptyFile("nanner.txt");
//        Main.main("add", "nanner.txt");
//        Commit commit2 = new Commit("added wug and wug2", Commit.getCurrentID());
//        commit2.write();
//
//        // Checkout Serf branch and validate file in master branch is not in current branch
//        Main.main("checkout", "Serf");
//        assertEquals("Serf", Branch.getCurrent());
//
//        Main.main("log");
//
//        blobList = Commit.getCurrentBlobs();
//        assertFalse(blobList.containsKey("nanner.txt"));
//        assertNotEquals(commit2._sha1, Commit.getCurrentID());
//    }
//
//    // Test checkout branch failure cases
//    @Test
//    public void checkoutTest4() throws IOException {
//
//        // Create and commit random files
//        Utils.createEmptyFile("wug.txt");
//        Main.main("add", "wug.txt");
//        Main.main("commit", "added wug");
//
//        // Create and checkout new branch
//        Main.main("branch", "Serf");
//        Main.main("checkout", "Serf");
//
//        // Failure case: Checkout branch DNE
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        Main.main("checkout", "CUP");
//        output.close();
//
//        assertEquals("No such branch exists.", output.toString());
//
//        // Failure case: Checkout current branch
//        output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        Main.main("checkout", "Serf");
//        output.close();
//
//        assertEquals("No need to checkout the current branch.", output.toString());
//
//        // Failure case: Untracked file prevents branch checkout
//        output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        Utils.createEmptyFile("dwindle.txt");
//        Main.main("checkout", "Serf");
//        output.close();
//
//        assertEquals("There is an untracked file in the way; " +
//                "delete it, or add and commit it first.", output.toString());
//    }
//
//    // ----- BRANCH TESTS -----
//
//    // Test creating and checkout out new branch, and failure when creating branch with duplicate name
//    @Test
//    public void branchTest() throws IOException {
//
//        // Create and commit random file
//        Utils.createEmptyFile("wug.txt");
//        Main.main("add", "wug.txt");
//        Main.main("commit", "added wug");
//
//        // Create and checkout new branch
//        Main.main("branch", "Serf");
//        Main.main("checkout", "Serf");
//        assertNotEquals(Commit.zeroSha1, Commit.getCurrentID());
//
//        // Remove and commit random file
//        Main.main("rm", "wug.txt");
//        Commit commit2 = new Commit("removed wug", Commit.getCurrentID());
//        commit2.write();
//
//        // Check current commit contains correct files
//        HashMap<String, String> currentBlobs = Commit.getCurrentBlobs();
//        assertFalse(currentBlobs.containsKey("wug.txt"));
//
//        // Check new branch is current branch and contains correct HEAD commit
//        assertEquals("Serf", Branch.getCurrent());
//        assertEquals(commit2._sha1, Commit.getCurrentID());
//
//        // Checkout branch to master
//        Main.main("checkout", "master");
//
//        // Checkout master branch commit and branch valid
//        currentBlobs = Commit.getCurrentBlobs();
//        assertTrue(currentBlobs.containsKey("wug.txt"));
//        assertEquals("master", Branch.getCurrent());
//        assertNotEquals(commit2._sha1, Commit.getCurrentID());
//
//        // Failure case: Creating new branch with duplicate name
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        Main.main("branch", "master");
//        output.close();
//
//        assertEquals("A branch with that name already exists.", output.toString());
//    }
//
//    // ----- REMOVE BRANCH TESTS -----
//
//    // Test removing new branch
//    @Test
//    public void removeBranch() throws IOException {
//
//        // Create and commit random file
//        Utils.createEmptyFile("dog.txt");
//        Main.main("add","dog.txt");
//        Commit firstCommit = new Commit("first commit", Commit.getCurrentID());
//        firstCommit.write();
//
//        // Create and remove new branch
//        Main.main("branch", "serf");
//        Main.main("rm-branch", "serf");
//
//        // Check if new branch still exists and commits preserved
//        assertFalse(Utils.join(Main.BRANCH, "serf").exists());
//        assertNotNull(Commit.getCurrentBlobs());
//    }
//
//    // Test reset command back to previous commits
//    @Test
//    public void resetTest() throws IOException {
//
//        // Create and stage new file
//        Utils.createEmptyFile("notwug.txt");
//        Utils.randomChangeFileContents("notwug.txt");
//        Main.main("add", "notwug.txt");
//
//        // Create and write commit
//        Commit commit1 = new Commit("test Commit",Commit.getCurrentID());
//        commit1.write();
//
//        // Stage another new file
//        Utils.createEmptyFile("wug.txt");
//        Utils.randomChangeFileContents("wug.txt");
//        Main.main("add","wug.txt");
//
//        // Remove already committed file
//        Stage.remove("notwug.txt");
//
//        // Create and write commit
//        Commit commit2 = new Commit( "test Commit2",Commit.getCurrentID());
//        commit2.write();
//
//        // Reset back to first commit
//        Main.main("reset", commit1._sha1);
//
//        // Check current commit is first commit and valid files exist
//        assertEquals(commit1._sha1,Commit.getCurrentID());
//        assertTrue(Utils.join(Main.USERDIR, "notwug.txt").exists());
//        assertFalse(Utils.join(Main.USERDIR, "wug.txt").exists());
//        assertTrue(Utils.join(Main.USERDIR, "notwug.txt").length() > 0);
//
//        // Reset to second commit
//        Main.main("reset", commit2._sha1);
//
//
//        // Check current commit is second commit and valid files exist
//        assertEquals(commit2._sha1,Commit.getCurrentID());
//        assertTrue(Utils.join(Main.USERDIR, "wug.txt").exists());
//        assertFalse(Utils.join(Main.USERDIR, "notwug.txt").exists());
//    }
//
//    // Test reset command failure cases
//    @Test
//    public void resetTest2() throws IOException {
//
//        // Create and commit new file
//        Utils.createEmptyFile("notwug.txt");
//        Utils.randomChangeFileContents("notwug.txt");
//        Main.main("add","notwug.txt");
//        Main.main("commit", "test Commit");
//
//        // Reset back to zero SHA1 commit
//        Main.main("reset", Commit.zeroSha1);
//
//        // Validate zero SHA1 is current commit and valid files
//        assertEquals(Commit.zeroSha1, Commit.getCurrentID());
//        assertFalse(Utils.join(Main.USERDIR, "notwug.txt").exists());
//
//        // Failure case: Reset back to commit ID that DNE
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        Main.main("reset", "1234");
//
//        output.close();
//
//        assertEquals("No commit with that id exists.", output.toString());
//
//        // Failure case: Reset when there is untracked file
//        Utils.createEmptyFile("error.txt");
//        output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//
//        Main.main("reset", "1234");
//        output.close();
//        assertEquals("There is an untracked file in the way; " +
//                "delete it, or add and commit it first.", output.toString());
//    }
//
//    // ----- MERGE TESTS -----
//
//    // Test find split-point s.t. split point is not zero SHA1 commit
//    // Note: split-point is latest common ancestor between current and give branch
//    @Test
//    public void mergeTest() throws IOException {
//
//        // Create and commit new file (This commit will be split-point)
//        Utils.createEmptyFile("wug.txt");
//        Main.main("add", "wug.txt");
//        Commit commit1 = new Commit("1. added wug", Commit.getCurrentID());
//        commit1.write();
//
//        // Create and checkout new branch
//        Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//
//        // Create and commit file onto new branch
//        Utils.createEmptyFile("scratch.txt");
//        Main.main("add","scratch.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentID());
//        commit2.write();
//
//        // Checkout back to master
//        Main.main("checkout", "master");
//
//        // Create and commit new file on master branch
//        Utils.createEmptyFile("face.txt");
//        Main.main("add","face.txt");
//        Main.main("commit", "3. commit face");
//
//        // Check split-point is first commit
//        String splitPoint = Merge.splitPoint(commit2,commit1);
//        assertEquals(splitPoint, commit1._sha1);
//    }
//
//    // Test find split-point s.t. split point is zero SHA1 commit
//    @Test
//    public void mergeTest2() throws IOException {
//
//        // Create and checkout new branch
//        Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//
//        // Create and commit new file (Note: This is the split-point)
//        Utils.createEmptyFile("wug.txt");
//        Main.main("add", "wug.txt");
//        Commit commit1 = new Commit("1. added wug", Commit.getCurrentID());
//        commit1.write();
//
//        // Create and commit another new file on new branch
//        Utils.createEmptyFile("notwug.txt");
//        Main.main("add","notwug.txt");
//        Main.main("commit", "1. added wug");
//
//        // Checkout back to master branch
//        Main.main("checkout", "master");
//
//        // Create and commit new file on master branch
//        Utils.createEmptyFile("scratch.txt");
//        Main.main("add", "scratch.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentID());
//        commit2.write();
//
//        // Create and commit another new file on master branch
//        Utils.createEmptyFile("face.txt");
//        Main.main("add", "face.txt");
//        Main.main("commit", "3. commit face");
//
//        // Check split point is zero SHA1 commit
//        String splitPoint = Merge.splitPoint(commit2,commit1);
//        assertEquals(Commit.zeroSha1, splitPoint);
//    }
//
//    // Test fast-forward merge
//    @Test
//    public void mergeTest3() throws IOException {
//
//        // Create and commit random file
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add", "cup.txt");
//        Main.main("commit", "1. commit dog");
//
//        // Create and checkout new branch
//        Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//
//        // Create and commit new file on new branch
//        Utils.createEmptyFile("mag.txt");
//        Main.main("add", "mag.txt");
//        Main.main("commit", "2. commit scratch");
//
//        // Create and commit another new file on new branch
//        Utils.createEmptyFile("fire.txt");
//        Main.main("add","fire.txt");
//        Commit commit3 = new Commit( "3. commit fire", Commit.getCurrentID());
//        commit3.write();
//
//        // Checkout back to master
//        Main.main("checkout", "master");
//
//        // Check branch is fast-forwarded with correct message
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//
//        // Merge new branch into master
//        Main.main("merge","serf");
//        output.close();
//        assertEquals("Current branch fast-forwarded.", output.toString());
//
//        // Check no merge commit created
//        Commit fastCommit = Commit.getCurrent();
//        assertTrue(fastCommit._mergedId == null);
//        assertEquals(fastCommit._sha1, commit3._sha1);
//
//        // Check new branch commits unchanged
//        Main.main("checkout", "serf");
//        assertEquals(fastCommit._sha1, Commit.getCurrentID());
//    }
//
//    // IMPORTANT = watch out for static variables
//    // Note:
//    // - modified in given branch since sp, not mod. in current, -> changed to given (works)
//    // - commits are same in both branches
//    // - works
//
//    // Test merge of new branch with modified file onto master
//    @Test
//    public void mergeTest4() throws IOException {
//
//        // Create and commit new file
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add","cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentID());
//        commit1.write();
//
//        // Create and checkout out new branch
//        Main.main("branch","serf");
//        Main.main("checkout", "serf");
//
//        // Create and commit new file on new branch
//        Utils.randomChangeFileContents("cup.txt");
//        Main.main("add", "cup.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentID());
//        commit2.write();
//
//        // Checkout to master
//        Main.main("checkout", "master");
//
//        // Create and commit new file onto master branch
//        Utils.createEmptyFile("dog.txt");
//        Main.main("add","dog.txt");
//        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentID());
//        commit3.write();
//
//        // Merge new branch onto master
//        Main.main("merge", "serf");
//
//        // Check split point equals first commit
//        String splitPoint = Merge.splitPoint(commit3, commit2);
//        Commit masterCurrentCommit = Commit.getCurrent();
//        assertNotEquals(splitPoint, commit3._sha1);
//        assertEquals(commit1._sha1, splitPoint);
//
//        // Checkout serf branch
//        Main.main("checkout", "serf");
//
//        // Check current commits of branches differ
//        Commit serfCurrentCommit = Commit.getCurrent();
//        assertNotEquals(masterCurrentCommit, serfCurrentCommit);
//    }
//
//
//    // Test merge failure case: merging given branch that is ancestor of current branch
//    @Test
//    public void mergeTest5() throws IOException {
//
//        // Create and commit new file
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add","cup.txt");
//        Main.main("commit", "1. commit dog");
//
//        // Create and checkout new branch
//        Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//
////        // Update file contents of previously commited file and commit
////        Utils.sameChangeFileContents("cup.txt");
////        Main.main("add","cup.txt");
////        Main.main("commit", "2. commit scratch");
//
//        // Checkout master branch
//        Main.main("checkout", "master");
//
//        // Update and commit master branch version of committed file to have same contents as that in serf branch
//        Utils.randomChangeFileContents("cup.txt");
//        Main.main("add","cup.txt");
//        Main.main("commit", "2. commit scratch");
//
//        // Merge serf branch onto master
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        new Merge().apply("serf");
//        output.close();
//
//        assertEquals("Given branch is an ancestor of the current branch.", output.toString());
//
//    }
//
//    // Test merge with 2 branches that have same file name and contents but created not from split-point
//    @Test
//    public void mergeTest6() throws IOException {
//
//        // Create and commit new file
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add", "cup.txt");
//        Commit commit1 = new Commit("1. commit cup", Commit.getCurrentID());
//        commit1.write();
//
//        // Create and checkout new branch
//        Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//
//        // Update and commit previously committed file
//        Utils.sameChangeFileContents("cup.txt");
//        Main.main("add", "cup.txt");
//        Commit commit2 = new Commit("2. update cup", Commit.getCurrentID());
//        commit2.write();
//
//        // Checkout master branch
//        Main.main("checkout", "master");
//
//        // Update and stage changed to cup.tst
//        Utils.sameChangeFileContents("cup.txt");
//        Main.main("add", "cup.txt");
//
//        // Create and stage new file
//        Utils.createEmptyFile("another.txt");
//        Main.main("add", "another.txt");
//
//        // Commit stage changes
//        Commit commit3 = new Commit("3. updated cup.txt and added another.txt", Commit.getCurrentID());
//        commit3.write();
//
//        // Merge serf branch onto master
//        Main.main("merge","serf");
//
//        // Check split-point is first commit
//        String splitPoint = Merge.splitPoint(commit3, commit2);
//        assertNotEquals(splitPoint, commit3._sha1);
//        assertEquals(commit1._sha1, splitPoint);
//    }
//
//    // Test merge of new branch that has same commits as master does not produce merge.
//    @Test
//    public void mergeTest7() throws IOException {
//
//        // Create and commit new file
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add", "cup.txt");
//        Main.main("commit", "1. commit dog");
//
//        // Create and checkout new branch
//        Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//
//        // Remove previous file and commit on new branch
//        Main.main("rm", "cup.txt");
//        Main.main("commit", "2. commit scratch");
//
//        // Checkout master branch
//        Main.main("checkout", "master");
//
//        // Stage cup.txt for removal and create new file on master branch
//        Stage.remove("cup.txt");
//        Utils.createEmptyFile("another.txt");
//        Main.main("add","another.txt");
//
//        // Commit previous changes on master branch
//        Main.main("commit", "2. commit scratch");
//
//        // Create new file (that was previously deleted)
//        Utils.createEmptyFile("cup.txt");
//
//        // Save previous commit prior to merge
//        Commit previousCommit = Commit.getCurrent();
//
//        // Merge serf branch onto master
//        Main.main("merge", "serf");
//
//        // Check no merge commit was created
//        assertEquals(previousCommit._sha1, Commit.getCurrent()._sha1);
//    }
//
//    // Test merge of new branch into master with non-committed file in master cwd
//    @Test
//    public void mergeTest8() throws IOException {
//
//        // Create and commit new file
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add", "cup.txt");
//        Main.main("commit", "1. commit dog");
//
//        // Create and checkout new branch
//        Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//
//        // Remove previous file and commit
//        Main.main("rm", "cup.txt");
//        Main.main("commit", "2. commit scratch");
//
//        // Checkout master
//        Main.main("checkout", "master");
//
//        // Create and commit new file onto master
//        Utils.createEmptyFile("another.txt");
//        Main.main("add", "another.txt");
//        Main.main("commit", "2. commit scratch");
//
//        // Create new file
//        Utils.createEmptyFile("cup.txt");
//
//        // Merge serf branch onto master
//        Main.main("merge","serf");
//
//        // Check merge removed non-committed file in master
//        assertFalse(Utils.join(Main.USERDIR, "cup.txt").exists());
//    }
//
//    // Test merge conflict: two branches has same filename but contents different than common ancestor
//    @Test
//    public void mergeTest9() throws IOException {
//
//        // Create and commit new file
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add", "cup.txt");
//        Main.main("commit", "1. commit dog");
//
//        // Create and checkout new branch
//        Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//
//        // Create and commit new file to new branch
//        Utils.randomChangeFileContents("cup.txt");
//        Main.main("add", "cup.txt");
//        Main.main("commit", "2. commit scratch");
//
//        // Checkout master
//        Main.main("checkout", "master");
//
//        // Create and commit new file to master branch
//        Utils.randomChangeFileContents("cup.txt");
//        Main.main("add", "cup.txt");
//        Main.main("commit", "2. commit scratch");
//
//        // Merge new branch onto master branch
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(output));
//        Main.main("merge", "serf");
//        output.close();
//
//        assertEquals("Encountered a merge conflict.", output.toString());
//    }
//
//    // Test merge conflict: two branch has same file but contents differ, has valid split point and files
//    @Test
//    public void mergeTest10() throws IOException {
//
//        // Create and commit new file
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add", "cup.txt");
//        Commit commit1 = new Commit("1. commit cup", Commit.getCurrentID());
//        commit1.write();
//
//        // Create and checkout new branch
//        Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//
//        // Update file contents and commit to new branch
//        Utils.randomChangeFileContents("cup.txt");
//        Main.main("add", "cup.txt");
//        Commit commit2 = new Commit("2. commit updated cup.txt", Commit.getCurrentID());
//        commit2.write();
//
//        // Checkout master branch
//        Main.main("checkout", "master");
//
//        // Update contents of previous file on master
//        Utils.randomChangeFileContents("cup.txt");
//        Main.main("add", "cup.txt");
//        Commit commit3 = new Commit("3. commit updated cup.txt", Commit.getCurrentID());
//        commit3.write();
//
//        // Merge serf branch onto master branch
//        Main.main("merge", "serf");
//
//        // Checkout merge commit is HEAD commit of master and serf branch
//        Commit currentCommit = Commit.getCurrent();
//        assertEquals(currentCommit._mergedId.substring(0,7), commit3._sha1.substring(0,7));
//        assertEquals(currentCommit._mergedId.substring(8,15), commit2._sha1.substring(0,7));
//
//        // Check split-point is first commit
//        String splitPoint = Merge.splitPoint(commit3, commit2);
//        assertEquals(commit1._sha1, splitPoint);
//
//        // Check contents of cup.txt differs between both branches
//        HashMap<String, String> masterCurrentCommit = Commit.getBlobs(commit3._tree);
//        HashMap<String, String> serfCurrentCommit = Commit.getBlobs(commit2._tree);
//        HashMap<String, String> headCommit = Commit.getCurrentBlobs();
//        assertNotEquals(masterCurrentCommit.get("cup.txt"), headCommit.get("cup.txt"));
//        assertNotEquals(serfCurrentCommit.get("cup.txt"), headCommit.get("cup.txt"));
//    }
//
//    // Test criss-cross merge of three different branches
//    @Test
//    public void mergeTest11() throws IOException {
//
//        // Create new branch (Note: this call MUST be first to prevent master being ancestor of new branch)
//        Main.main("branch", "branch");
//
//        // Create and commit new files
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add","cup.txt");
//        Commit commit1 = new Commit("1. commit cup", Commit.getCurrentID());
//        commit1.write();
//
//        // Checkout new branch
//        Main.main("checkout", "branch");
//
//        // Create new file with contents and commit in new branch
//        Utils.createEmptyFile("dogcup.txt");
//        Main.main("add", "dogcup.txt");
//        Commit commit2 = new Commit("2. added dogcup", Commit.getCurrentID());
//        commit2.write();
//
//        // Check new commit created
//        assertNotEquals(commit1._sha1,commit2._sha1);
//
//        // Check new branch HEAD commit is commit2
//        assertEquals("branch", Branch.getCurrent());
//        assertEquals(commit2._sha1, Branch.read("branch"));
//
//        // Create new branch called temp
//        Main.main("branch", "temp"); // contains cup with content
//
//        // Merge 1: Merge master branch onto branch branch
//        Main.main("merge", "master");
//
//        // Check merge commit SHA1 is composed of master branch and branch branch SHA1's
//        Commit currentCommit = Commit.getCurrent();
//        assertEquals(currentCommit._mergedId.substring(0,7), commit2._sha1.substring(0,7));
//        assertEquals(currentCommit._mergedId.substring(8,15), commit1._sha1.substring(0,7));
//
//        // Checkout master
//        Main.main("checkout", "master");
//
//        // Create and commit new file
//        Utils.createEmptyFile("mood.txt");
//        Main.main("add", "mood.txt");
//        Commit commit3 = new Commit("4. mood.txt", Commit.getCurrentID());
//        commit3.write();
//
//        // Merge 2: merge temp branch onto master branch
//        Main.main("merge", "temp");
//
//        // Check merge commit SHA1 is composed of master branch and temp branch SHA1's
//        currentCommit = Commit.getCurrent();
//        assertEquals(currentCommit._mergedId.substring(0,7), commit3._sha1.substring(0,7));
//        assertEquals(currentCommit._mergedId.substring(8,15), commit2._sha1.substring(0,7));
//
//        // Checkout branch branch
//        Main.main("checkout", "branch");
//
//        // Create and commit new file onto branch branch
//        Utils.createEmptyFile("spatula.txt");
//        Main.main("add", "spatula.txt");
//        Commit commit5 = new Commit("2. spatula cup", Commit.getCurrentID());
//        commit5.write();
//
//        // Get commit history of branch branch
//        ArrayList<String> branchCommitArray = Utils.getCommitArray(commit5, new ArrayList<>());
//
//        // Checkout master
//        Main.main("checkout", "master"); // -- contains fork, spatula, mood, cup
//
//        // Create and commit new file onto master
//        Utils.createEmptyFile("fork.txt");
//        Main.main("add", "fork.txt");
//        Commit commit7 = new Commit("added fork", Commit.getCurrentID());
//        commit7.write();
//
//        // Get master branch commit history
//        ArrayList<String> masterCommitArray = Utils.getCommitArray(commit7, new ArrayList<>());
//
//        // Merge 3: merge branch branch onto master branch
//        Main.main("merge", "branch");
//
//        Collections.reverse(masterCommitArray);
//        Collections.reverse(branchCommitArray);
//
//        // Check split-point is closest commit to HEAD commit in master branch
//        String splitPoint = Merge.splitPoint(commit7, commit5);
//        assertEquals(splitPoint, branchCommitArray.get(1));
//    }

// TODO-LATER

//    ----- REBASE TESTS -----

//    @Test
//    public void findBranchSplitPointTest() throws IOException {
//
//        
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add","cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentID());
//        commit1.write();
////
//     Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//        Utils.randomChangeFileContents("cup.txt");
//        Main.main("add","cup.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentID());
//        commit2.write();
//
//        Main.main("checkout", "master");
//        Utils.createEmptyFile("dog.txt");
//        Main.main("add","dog.txt");
//        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentID());
//        commit3.write();
//
//        String sp = Merge.splitPoint(commit2,commit3);
//        Commit spCommit = Merge.findBranchSplit(commit2,sp);
//
//        Main.main("checkout", "serf");
//        System.out.println("Splitpoint = " + sp);
//        System.out.println("Branch split = " + spCommit._sha1);
//
//        Log.printLog();
//        assertEquals(sp, spCommit._parentSha1);
//    }

//    @Test
//    public void rebaseTest() throws IOException {
//
//        
//        Utils.createEmptyFile("cup.txt");
//        Main.main("add","cup.txt");
//        Commit commit1 = new Commit("1. commit cup", Commit.getCurrentID());
//        commit1.write();
////
//     Main.main("branch", "serf");
//        Main.main("checkout", "serf");
//        Utils.randomChangeFileContents("cup.txt");
//        Commit commit2 = new Commit("2. modified cup", Commit.getCurrentID());
//        commit2.write();
//
//        Main.main("checkout", "master");
//        Utils.createEmptyFile("dog.txt");
//        Main.main("add","dog.txt");
//        Commit commit3 = new Commit("3. commit dog", Commit.getCurrentID());
//        commit3.write();
//
//        Rebase.apply("serf");
////        Log.printLog(); // initial -> 3. commit dog -> 1. commit cup -> 2. modified cup
//        Log.printGlobal();
//    }
}
