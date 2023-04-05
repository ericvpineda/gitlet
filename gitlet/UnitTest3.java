package gitlet;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

/** Test cases with greater-depth testing (i.e. failure/conflict cases) [Contains tests 41-]
 Tests commands: 
 */
public class UnitTest3 {

    // Clear unnecessary files and initialize .gitlet repository
    @Before
    public void initialize() throws IOException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
    }

    // 35. Test reset command by:
    //  1. Create 2 branch with different commits
    //  2. In master branch, create add new file, but reset to previous commit on master
    //  3. Reset master back to original commit and verify original commit
    @Test
    public void resetBasicTest() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("cup.txt");
        Main.main("add", "cup.txt");
        Commit commit1 = new Commit("added cup.txt",Commit.getCurrentID());
        commit1.write();

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Create and commit random file on new branch
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");
        Main.main("commit","added beaver.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Update contents of previous file in master branch
        Utils.randomChangeFileContents("cup.txt");
        Main.main("add", "cup.txt");

        // Reset back to initial commit and verify initial commit
        Main.main("reset", Commit.zeroSha1);
        assertEquals(Commit.zeroSha1, Commit.getCurrentID());

        // Reset to first commit and verity first commit
        Main.main("reset", commit1._sha1);
        assertEquals(commit1._sha1, Commit.getCurrentID());
    }

    // 36. Test reset command when with other branch with commits
    @Test
    public void resetWithOtherBranches() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Commit commit1 = new Commit("added wug.txt",Commit.getCurrentID());
        commit1.write();

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Create and commit random file
        Utils.createEmptyFile("mug.txt");
        Main.main("add", "mug.txt");
        Main.main("commit","added mug.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Update and commit random file in master branch
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Commit commit3 = new Commit("updated wug.txt", Commit.getCurrentID());
        commit3.write();

        // Create and stage random file in master branch
        Utils.randomChangeFileContents("anotherFile.txt");
        Main.main("add", "anotherFile.txt");

        // Reset back to first commit and verity commit
        Checkout.reset(commit1._sha1);
        assertEquals(commit1._sha1, Commit.getCurrentID());

        // Reset back to commit3 and verity commit
        Checkout.reset(commit3._sha1);
        assertEquals(commit3._sha1, Commit.getCurrentID());
    }

    // 37. Test reset command failure: non-existing commit
    @Test
    public void resetFailureCommitDNE() throws IOException {

        // Create and commit new file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit", "added wug.txt");

        // Reset to non-existing commit
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("reset", "1234");
        output.close();

        assertEquals("No commit with that id exists.", output.toString());
    }

    // 38. Test checkout works with short SHA1 identifier
    @Test
    public void checkoutShortSHA1() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Reset to initial commit
        Main.main("reset", "00000");
        assertEquals(Commit.zeroSha1, Commit.getCurrentID());
    }

    // 39. Test special cases of merge: branch fast forward
    // Note: current branch is ancestor of given branch
    @Test
    public void mergeBranchFastForward() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Create and commit another new file
        Utils.createEmptyFile("beaver.txt");
        Main.main("add", "beaver.txt");
        Main.main("commit","added beaver.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Attempt to merge other-branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "other-branch");
        output.close();

        assertEquals("Current branch fast-forwarded.", output.toString());
    }

    // splitpoint is the current branch
    // - do nothing, merge complete and ends with 'given branch is ancestor of current branch'

    // 40. Merge failure case: Attempt to merge with ancestor
    @Test
    public void mergeConflictTest2() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Create new branch
        Main.main("branch", "other-branch");

        // Create and commit random file
        Utils.createEmptyFile("mag.txt");
        Main.main("add", "mag.txt");
        Main.main("commit","added mag.txt");

        // Attempt to merge other-branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "other-branch");
        output.close();

        assertEquals("Given branch is an ancestor of the current branch.", output.toString());
    }

    // 41. Reset failure case: Attempt to reset with untracked file in current working directory
    @Test
    public void badResetError() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Create random file
        Utils.createEmptyFile("calendar.txt");

        // Attempt to merge other-branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("reset", "0001");
        output.close();

        assertEquals("There is an untracked file in the way; delete it, or add and commit it first.", output.toString());
    }

    // 42. Test checkout file at specific commit using short SHA1 identifier
    @Test
    public void shortUidTest() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Commit commit1 = new Commit("added wug", Commit.getCurrentID());
        commit1.write();

        // Update and commit random file in master branch
        Utils.randomChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","modified wug.txt");

        // Checkout file at specific commit
        Main.main("checkout", commit1._sha1.substring(0,5), "--", "wug.txt");

        // Check file has same contents as specfic commit
        File file = Utils.join(Main.USERDIR, "wug.txt");
        assertEquals(file.length(), 0);
    }

    // 43. Merge special case: File modified in same way in both branches shows no change
    @Test
    public void specialMergeCases() throws IOException {

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Create and commit new file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit", "added wug.txt");

        // Checkout master branch
        Main.main("checkout", "master");

        // Update and commit new file
        Utils.sameChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit", "added wug.txt");

        // Checkout other-branch
        Main.main("checkout", "other-branch");

        // Create and commit new file
        Utils.sameChangeFileContents("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit", "added wug.txt");

        // Attempt to merge other-branch onto master branch
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("merge", "master");
        output.close();

        assertEquals("No changes added to the commit.", output.toString());
    }

    // 44. Merge special case: File is removed in both branch, but file still in current working directory
    @Test
    public void specialMergeCases1() throws IOException {

        // Create and commit random file
        Utils.createEmptyFile("wug.txt");
        Main.main("add", "wug.txt");
        Commit commit1 = new Commit("added wug.txt",Commit.getCurrentID());
        commit1.write();

        // Create and checkout new branch
        Main.main("branch", "other-branch");
        Main.main("checkout", "other-branch");

        // Remove file in new branch
        Main.main("rm", "wug.txt");
        Commit commit2 = new Commit("added wug.txt",Commit.getCurrentID());
        commit2.write();

        // Checkout back to master
        Main.main("checkout", "master");

        // Remove file in master branch
        Main.main("rm", "wug.txt");
        Commit commit3 = new Commit("added wug.txt",Commit.getCurrentID());
        commit3.write();

        // Create new file with identical name that was deleted
        Utils.createEmptyFile("wug.txt");

//        // Check split-point is first commit
//        String splitPoint = Merge.splitPoint(commit2, commit3);
//        assertEquals(commit1._sha1, splitPoint);

        Main.main("merge", "other-branch");
//        Checkout.overwriteBranch("serf");
//        assertEquals(commit2._tree,commit3._tree); // Note: trees are the same
    }


}
