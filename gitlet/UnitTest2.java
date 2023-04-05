package gitlet;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import static org.junit.Assert.*;

/** Test cases with greater-depth testing (i.e. failure/conflict cases) [Contains tests 23-41]
 Tests commands: global-log, checkout, find, branch, rm-branch, merge
 */
public class UnitTest2 {

    // Clear unnecessary files and initialize .gitlet repository
    @Before
    public void initialize() throws IOException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
    }

    // 23. Test check global log prints out all commits
    @Test
    public void globalLogTest() throws IOException {

        // Create and commit new file
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");
        Main.main("commit", "added beaver.txt");

        // Create and commit another new file
        Utils.createEmptyFile("dog.txt");
        Main.main("add", "dog.txt");
        Main.main("commit", "added dog.txt");

        Main.main("global-log");
    }

    // Q24. Test global-log prints out all commits (indifferent to HEAD commit position)
    @Test
    public void globalLogPrevTest() throws IOException {

        // Create and commit new file
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");
        Main.main("commit", "added beaver.txt");

        // Create and commit another new file
        Utils.createEmptyFile("dog.txt");
        Main.main("add", "dog.txt");
        Main.main("commit", "added dog.txt");

        // Reset back to initial commit
        Checkout.reset("00000");
        String branchSha1 = Commit.getCurrentID();
        assertEquals(Commit.zeroSha1, branchSha1);

        Main.main("global-log");
    }

    // 25. Test find command when it succeeds
    @Test
    public void successfulFindTest() throws IOException {

        // Create and commit new file
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");
        Commit firstCommit = new Commit("added beaver.txt",Commit.getCurrentID());
        firstCommit.write();

        // Attempt to find commit by log message
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("find", "added beaver.txt");
        output.close();

        assertEquals(firstCommit._sha1, output.toString());
    }

    // 26. Test find command when HEAD commit is ancestor of target commit
    @Test
    public void successfulFindOrphan() throws IOException {

        // Create and commit new file
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");
        Commit firstCommit = new Commit("added beaver.txt",Commit.getCurrentID());
        firstCommit.write();

        // Create and commit another new file
        Utils.createEmptyFile("dog.txt");
        Main.main("add", "dog.txt");
        Main.main("commit", "added dog.txt");

        // Reset back to first comit
        Main.main("reset", firstCommit._sha1);

        // Verify that HEAD commit is first commit
        String branchSha1 = Commit.getCurrentID();
        assertEquals(branchSha1, firstCommit._sha1);

        // Attempt to find commit by log message
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("find", "added beaver.txt");
        output.close();

        assertEquals(firstCommit._sha1, output.toString());
    }

    // 27. Test checkout a file to version in previous commit
    @Test
    public void checkoutDetailTest() throws IOException {

        // Create and commit new file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit", "added wug.txt");

        // Update file contents
        Utils.randomChangeFileContents("wug.txt");

        // Checkout file to version in commit
        Main.main("checkout", "--", "wug.txt");

        // Check file contents is empty
        File file = Utils.join(Main.USERDIR, "wug.txt");
        assertEquals(file.length(), 0);
    }

    // 28. Test checkout previous version of file from GIVEN commit
    @Test
    public void checkoutDetailTest1() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Commit commit1 = new Commit("added wug.txt",Commit.getCurrentID());
        commit1.write();

        // Update and commit random file
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","modified wug.txt");

        // Checkout file to version in commit1
        Main.main("checkout", commit1._sha1, "--", "wug.txt");

        // Check file contents is empty
        File file = Utils.join(Main.USERDIR, "wug.txt");
        assertEquals(file.length(), 0);
    }

    // 29. Test checkout failure cases: File does not exist in commit
    @Test
    public void checkoutFailure1() throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("checkout", Commit.zeroSha1, "--", "wug.txt");
        output.close();

        assertEquals("File does not exist in that commit.", output.toString());
    }

    // 29. Test checkout failure cases: Commit does not exist
    @Test
    public void checkoutFailure2() throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("checkout", "12345", "--", "wug.txt");
        output.close();

        assertEquals("No commit with that id exists.", output.toString());
    }

    // 30. Test checkout failure cases: Branch that does not exist
    @Test
    public void checkoutFailure3() throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("checkout", "serf");
        output.close();

        assertEquals("No such branch exists.", output.toString());
    }

    // 31. Test checkout branch when adding files
    @Test
    public void branchAddingFilesTest() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Create and commit random file on new branch
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");
        Main.main("commit","added beaver.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Verify files in master are unique to new branch
        HashMap<String, String> currentBlobs = Commit.getCurrentBlobs();
        assertFalse(currentBlobs.containsKey("beaver.txt"));
    }

    // 31. Test checkout branch when modifying different files
    @Test
    public void branchModifyFilesTest() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Change and commit previous file
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","updated wug.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Check file on master branch is unchanged
        File file = Utils.join(Main.USERDIR, "wug.txt");
        assertEquals(file.length(), 0);
    }

    // 32. Test checkout branch when removing different files
    @Test
    public void branchTest2() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Remove and commit file in new branch
        Main.main("rm", "wug.txt");
        Main.main("commit","removed wug.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Check file on master branch exists
        File file = Utils.join(Main.USERDIR, "wug.txt");
        assertTrue(file.exists());
    }

    // 33. Test two branches can't be given same name
    @Test
    public void rmBranches() throws IOException, ClassNotFoundException {

        // Create new branch
        Main.main("branch", "other-branch");

        // Create duplicate branch call
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("branch", "other-branch");
        output.close();

        assertEquals("A branch with that name already exists.", output.toString());
    }

    // 34. Test merge conflict: Two branches have different versions of file compared to latest common ancestor
    @Test
    public void mergeConflictModifiedFilesTwoBranchesTest() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Create and commit file on new branch
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","modified wug.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Update and commit random file
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","modified wug.txt");

        // Attempt to merge other-branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "other-branch");
        output.close();

        assertEquals("Encountered a merge conflict.", output.toString());
    }

    // 35. Test merge conflict: Branches have different file content with zero SHA1 as latest common ancestor
    @Test
    public void mergeConflictAtZeroSHA1Commit() throws IOException {

        // Create and checkout new branch
        Main.main("branch", "serf");

        // Create and commit random file on master branch
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Checkout new branch
        Main.main("checkout", "serf");

        // Update and commit random file on new branch
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","modified wug.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Attempt to merge serf branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "serf");
        output.close();

        assertEquals("Encountered a merge conflict.", output.toString());
    }

    // 36. Test merge conflict: file changed in other branch and removed in master branch
    @Test
    public void mergeNoConflictFileRemoved() throws IOException, ClassNotFoundException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Update and commit random file in other-branch branch
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","modified wug.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Remove file in master branch
        Main.main("rm", "wug.txt");
        Main.main("commit","removed wug.txt");

        // Attempt to merge other-branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "other-branch");
        output.close();

        assertEquals("Encountered a merge conflict.", output.toString());
    }


    // 37. Test merge conflict: file changed in master branch and removed in other branch
    @Test
    public void mergeNoConflictFileRemovedInOtherBranch() throws IOException{

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Remove file in other-branch branch
        Main.main("rm", "wug.txt");
        Main.main("commit","removed wug.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Update and commit random file in master branch
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","modified wug.txt");

        // Attempt to merge other-branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "other-branch");
        output.close();

        assertEquals("Encountered a merge conflict.", output.toString());
    }

    // 38. Test merge conflict: file absent at split point and contents differ in given and current branch
    @Test
    public void mergeConflictFileRemovedInMasterBranch() throws IOException{

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Update and commit random file in other-branch branch
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","modified wug.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Update and commit random file in master branch
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","modified wug.txt");

        // Attempt to merge other-branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "other-branch");
        output.close();

        assertEquals("Encountered a merge conflict.", output.toString());
    }

    // 39. Test merge failure: attempt to merge with files staged from addition or removal
    @Test
    public void mergeFailureFilesStagedAdditionAndRemoval() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "wug.txt");
        Main.main("add", "beaver.txt");
        Main.main("commit","added beaver.txt wug.txt");

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Update and commit random file in other-branch branch
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","modified wug.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Remove wug.txt in master branch
        Main.main("rm", "wug.txt");

        // Add updated beaver.txt changes to stage
        Utils.randomChangeFileContents("beaver.txt");
        Main.main("add", "beaver.txt");

        // Attempt to merge other-branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "other-branch");
        output.close();

        assertEquals("You have uncommitted changes.", output.toString());
    }

    // 40. Test merge failure: attempt to merge branch with itself
    @Test
    public void mergeFailureMergeWithItself() throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "master");
        output.close();

        assertEquals("Cannot merge a branch with itself.", output.toString());
    }

    // 41. Test merge failure: attempt to merge branch with untracked file present
    @Test
    public void mergeFailureWithUntrackedFile() throws IOException {

        // Create and checkout new branch
        Main.main("branch", "other-branch");

        // Create new file that is not staged
        Utils.createEmptyFile("errorFile.txt");

        // Attempt to merge other-branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "other-branch");
        output.close();

        assertEquals("There is an untracked file in the way; delete it, or add and commit it first.", output.toString());
    }
}

