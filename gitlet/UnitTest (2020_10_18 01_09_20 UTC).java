package gitlet;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import static org.junit.Assert.*;

/* The suite of all JUnit tests for the gitlet package.
   @author
 */
public class UnitTest {

    // Q1 - init
    @Test
    public void initTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        assertTrue(Main.GITLET.exists());
        assertTrue(Main.COMMITS.exists());
        assertTrue(Main.BLOB.exists());
        assertTrue(Main.TREE.exists());
        assertTrue(Main.HEAD.exists());
        assertEquals(Commit.zeroSha1, Commit.getCurrentSha1());
        assertEquals("master", Branch.getCurrent());
        assertEquals(Commit.zeroSha1, Commit.getCurrentSha1());
        assertNotNull(Stage.read());
    }

    @Test
    public void initTestRepeat() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Init.initialize();
        output.close();
        assertEquals("A Gitlet version-control system already exists " +
                "in the current directory.", output.toString());
    }


    // Q2 - test add, modify, and checkout back to current commit
    @Test
    public void basicCheckoutTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit c = new Commit("added wug", Commit.getCurrentSha1());
        c.write();

        Utils.randomChangeFileContents("wug.txt");
        Checkout.overwriteFile("wug.txt"); // Note: wug should have nothing in it
        assertTrue(Utils.join(Main.USERDIR, "wug.txt").length() == 0);
    }

    // Q3 - set up chain of commits and check log
    @Test
    public void basicLogTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit c = new Commit("added wug", Commit.getCurrentSha1());
        c.write();

        Utils.createRandomFile("notwug.txt");
        Stage.add("notwug.txt");
        Commit c1 = new Commit("added notwug", Commit.getCurrentSha1());
        c1.write();

//        Log.printLog();
//        Log.printGlobal();
    }

    // Q4 - checkout to prev version of commit
    @Test
    public void prevCheckoutTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit c = new Commit("added wug", Commit.getCurrentSha1());
        c.write();

        Utils.randomChangeFileContents("wug.txt");
        Stage.add("wug.txt");
        Commit c1 = new Commit("modified wug", Commit.getCurrentSha1());
        c1.write();

        Checkout.overwriteCommit("wug.txt",c._sha1);
        assertTrue(Utils.join(Main.USERDIR, "wug.txt").length() == 0);
    }


    // -- Note: missing tests 5-10 --


    // Note: Q11 - Status with 2 adds
    @Test
    public void basicStatusTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Status.print();
    }

    // Note: Q12 - Status with 2 adds
    @Test
    public void addStatusTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet(); 
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Utils.createRandomFile("baker.txt");
        Stage.add("wug.txt");
        Stage.add("baker.txt");
        Status.print();
    }

    // Note: Q13 - remove tracked file and check status
    @Test
    public void removeStatusTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("beaver.txt");
        Stage.add("beaver.txt");
        Commit firstCommit = new Commit("first commit", Commit.getCurrentSha1());
        firstCommit.write();

        Stage.remove("beaver.txt");
        Status.print();
    }

    // Note: Q14 - add 2 files and rm one
    @Test
    public void addRemoveStatusTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
        Utils.createRandomFile("beaver.txt");
        Utils.createRandomFile("wug.txt");
        Main.main("add","beaver.txt");
        Main.main("add","wug.txt");
        Main.main("rm","beaver.txt");

        assertTrue(Utils.join(Main.USERDIR, "beaver.txt").exists());
        assertFalse(Stage.read()._preStage.containsKey("beaver.txt"));
//        assertFalse(Stage.read()._preStage.containsKey("wug.txt"));
        assertTrue(Stage.read()._deletion.isEmpty());

        Main.main("status");
    }

    // Note: Q15 - add and commit 2 files, remove 1, check status,
    //              restore file and re-add file (should remove mark to remove file)
    // -> mean every checkout needs to be able to remove file from removed list?
    @Test
    public void removeAddStatusTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
        Utils.createRandomFile("beaver.txt");
        Utils.createRandomFile("wug.txt");
        Main.main("add","beaver.txt");
        Main.main("add","wug.txt");
        Main.main("commit","added beaver and wug");
        Main.main("rm","beaver.txt");

        Utils.createRandomFile("beaver.txt");
        Main.main("add","beaver.txt");
        assertFalse(Stage.read()._deletion.containsKey("beaver.txt"));
        assertFalse(Stage.read()._preStage.containsKey("beaver.txt"));
        Main.main("status");
    }

    // Q16 - commit with nothing to stage
    @Test
    public void emptyCommitTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Commit firstCommit = new Commit("first commit", Commit.getCurrentSha1());
        firstCommit.write();
    }

    // Note: Q17 - commit with blank message
    @Test
    public void emptyCommit() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
        Main.main("commit","  ");
    }

    // Note: Q18 - Check adding already tracked and unchanged file has not effect status
    @Test
    public void nopAddTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("beaver.txt");
        Stage.add("beaver.txt");
        Stage.add("beaver.txt");
        assertTrue(Stage.read()._preStage.containsKey("beaver.txt"));
        Status.print();
    }

    // Note: check non-existent file correct error
    @Test
    public void addMissingErrTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Stage.add("beaver.txt");
    }

    // Note: Q20 - check commit clears staging area
    @Test
    public void statusAfterCommitTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("beaver.txt");
        Stage.add("beaver.txt");
        Commit firstCommit = new Commit("first commit", Commit.getCurrentSha1());
        firstCommit.write();

        assertTrue(Stage.read()._preStage.isEmpty());
        assertTrue(Stage.read()._deletion.isEmpty());

        Status.print();
    }

    // Note: Q21 - Check remove unstage, untracked file correct err
    @Test
    public void nopRemoveErrTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("beaver.txt");
        Stage.remove("beaver.txt");
    }

    // Note: Q22 - Check remove and unstage file thats manually deleted
    @Test
    public void removeDeletedTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("beaver.txt");
        Stage.add("beaver.txt");
        Commit firstCommit = new Commit("first commit", Commit.getCurrentSha1());
        firstCommit.write();

        Utils.join(Main.USERDIR,"beaver.txt").delete();
        Stage.remove("beaver.txt");
        Status.print();
        HashMap<String, String> h = Stage.read()._deletion;
        assertTrue(h.containsKey("beaver.txt"));
    }
}


