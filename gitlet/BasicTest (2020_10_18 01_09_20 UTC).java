package gitlet;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

public class BasicTest {

    @Test
    public void initializeGitlet() throws IOException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
        Commit c = Commit.getCurrent();
        assertNotNull(c);
        assertEquals(Commit.zeroSha1, Commit.getCurrentSha1());
        assertNotNull(c._tree);
        HashMap<String, String> t = Commit.getBlobs(c._tree);
        assertTrue(t.isEmpty());
    }

    @Test
    public void addToStageTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("cube.txt");
        Stage s = Stage.read();
        assertTrue(s._preStage.isEmpty());
        assertTrue(s._deletion.isEmpty());

        // Note: check if stage addeds files
        Stage.add("cube.txt");
        Stage stage = Stage.read();
        assertTrue(stage._preStage.containsKey("cube.txt"));

        // Note: check adding dne file
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Stage.add("map.txt");
        output.close();
        assertEquals("File does not exist.", output.toString());
    }

    @Test
    public void addTestUser() throws IOException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
        Utils.createRandomFile("cube.txt");
        Stage s = Stage.read();
        assertTrue(s._preStage.isEmpty());
        assertTrue(s._deletion.isEmpty());

        Main.main("add","cube.txt");
        Stage stage = Stage.read();
        assertTrue(stage._preStage.containsKey("cube.txt"));
    }

    @Test
    public void commmitTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentSha1());
        firstCommit.write();

        // Note: check if commit folder updated, commit tree contains files, head changes
        assertEquals(2, Main.COMMITS.listFiles().length);
        HashMap<String, String> h = Commit.getBlobs(firstCommit._tree);
        assertTrue(h.containsKey("wug.txt"));
        assertTrue(Stage.read() != null);
        assertEquals("master", Utils.deserialize(Main.HEAD, String.class));
        assertEquals(firstCommit._sha1, Commit.getCurrentSha1());

        // Note: check added files not change commit blobs
        Utils.createRandomFile("notwug.txt");
        h = Commit.getBlobs(firstCommit._tree);
        assertFalse(h.containsKey("notwug.txt"));
        // Note: check total sha1's commits have
        ArrayList<String> a = Utils.getTotalSha1History(firstCommit, new ArrayList<>());
        System.out.println("Commit history: ");
        System.out.println(a);
        assertTrue(a.contains(firstCommit._sha1));
        assertTrue(a.size() == 2);
    }

    @Test
    public void commitPointerTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentSha1());
        firstCommit.write();

        // Note: check if stage cleared, current commit pointer updated
        Utils.createRandomFile("notwug.txt");
        Stage.add("notwug.txt");
        assertFalse(Stage.read()._preStage.containsKey("wug.txt"));
        Commit s = new Commit("added not wug.txt", Commit.getCurrentSha1());
        assertNotEquals(s._sha1, Commit.getCurrentSha1());
        s.write();
        assertEquals(s._sha1, Commit.getCurrentSha1());
        assertEquals(s._parentSha1, firstCommit._sha1);
        assertEquals("added not wug.txt", s._logMessage);
        assertNotEquals(firstCommit._tree, s._tree);

    }

    @Test
    public void checkRepeatBlobs() throws IOException {
        // Note: check for repeat blobs
        Utils.clearCwdWithGitlet();
        Main.main("init");
        Utils.createRandomFile("cube.txt");
        Main.main("add","cube.txt");
        Main.main("commit","added cube");
        Stage stage = Stage.read();
        assertFalse(stage._preStage.containsKey("cube.txt"));

        Stage.add("cube.txt");
        stage = Stage.read();
        System.out.println(stage._preStage);
        assertFalse(stage._preStage.containsKey("cube.txt"));
    }

    @Test
    public void commitFailureCasesTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
        Utils.createRandomFile("wug.txt");

        // Failure case: no file added
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Main.main("commit", "added wug.txt");
        output.close();
        assertEquals("No changes added to the commit.", output.toString());

        // Note: check if current commit updated and correct log message
        Stage.add("wug.txt");
        Main.main("commit", "added wug.txt");
        Commit commit = Commit.getCurrent();
        assertNotEquals(commit._sha1, Commit.zeroSha1);
        assertEquals("added wug.txt", commit._logMessage);

        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        // Failure case: if commit msg empty
        Main.main("commit");
        output.close();
        assertEquals("Please enter a commit message.", output.toString());
    }

    @Test
    public void removeInCurrentCommit() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentSha1());
        firstCommit.write();

        // Note: Check removed file from stage
        Stage.remove("wug.txt");
        Stage s = Stage.read();
        assertTrue(s._preStage.isEmpty());
        assertTrue(s._deletion.containsKey("wug.txt"));

        Commit second = new Commit("removed wug.txt", Commit.getCurrentSha1());
        second.write();

        s = Stage.read();
        assertTrue(s._preStage.isEmpty());
        assertFalse(s._deletion.containsKey("wug.txt"));

        // Note: file untracked in current commit
        HashMap<String, String> secondCommit = Commit.getBlobs(second._tree);
        assertFalse(secondCommit.containsKey("wug.txt"));
        assertTrue(secondCommit.isEmpty());
    }

    @Test
    public void logTest() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Main.main("init");
        Main.main("log");
    }

    @Test
    public void globalLogTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
        Main.main("global-log");
    }

    @Test
    public void findTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
        Utils.createRandomFile("wug.txt");
        Main.main("add", "wug.txt");
        Main.main("commit","added wug.txt");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("find", "added wug.txt");

        output.close();
        assertEquals(Commit.getCurrentSha1(), output.toString());

        Utils.createRandomFile("anotherWug.txt");
        Main.main("add","anotherWug.txt");
        Main.main("commit","added wug.txt");
    }

    // Note: status command in spec
    @Test
    public void statusTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("goodbye.txt");
        Utils.createRandomFile("junk.txt");
        Utils.createRandomFile("wug3.txt");
        Stage.add("goodbye.txt");
        Stage.add("junk.txt");
        Stage.add("wug3.txt");
        Commit c = new Commit("added wug", Commit.getCurrentSha1());
        c.write();
        HashMap<String, String> h = Commit.getCurrentBlobs();
        assertTrue(h.containsKey("goodbye.txt"));
        assertTrue(h.containsKey("junk.txt"));
        assertTrue(h.containsKey("wug3.txt"));


        Branch.save("other-branch");
        Utils.createRandomFile("wug.txt");
        Utils.createRandomFile("wug2.txt");
        Stage.add("wug.txt");
        Stage.add("wug2.txt");
        Stage.remove("goodbye.txt");
        Stage s = Stage.read();
        assertTrue(s._preStage.containsKey("wug.txt"));
        assertTrue(s._preStage.containsKey("wug2.txt"));
        assertTrue(s._deletion.containsKey("goodbye.txt"));

        Utils.join(Main.USERDIR, "junk.txt").delete();
        assertFalse(Utils.join(Main.USERDIR, "junk.txt").exists());

        s = Stage.read();
        assertFalse(s._deletion.containsKey("junk.txt"));
        Utils.randomChangeFileContents("wug3.txt");
        Utils.createRandomFile("random.stuff");

        Status.print();
    }

    @Test
    public void statusTest1() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("goodbye.txt");
        Stage.add("goodbye.txt");
        Commit c = new Commit("added wug", Commit.getCurrentSha1());
        c.write();

        Stage.remove("goodbye.txt");
        Stage s = Stage.read();
        assertTrue(s._deletion.containsKey("goodbye.txt"));

//        Utils.createRandomFile("goodbye.txt");
        Stage.add("goodbye.txt");
        s = Stage.read();
        assertFalse(s._deletion.containsKey("goodbye.txt"));
        assertFalse(s._preStage.containsKey("goodbye.txt"));

        Status.print();
    }

    // Note: checkout for previous versions of file work
    @Test
    public void checkout() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit commit1 = new Commit("added dog",Commit.getCurrentSha1());
        commit1.write();

//        Utils.randomChangeFileContents("wug.txt");
        Stage.remove("wug.txt");

        // Checkout file that has been changed
        Checkout.overwriteFile("wug.txt");
        assertTrue(Utils.join(Main.USERDIR, "wug.txt").length() == 0);

        // Error = checkout file that dne
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Checkout.overwriteFile("wugz.txt");
        output.close();
        assertEquals("File does not exist in that commit.", output.toString());

        // Checkout file that has been removed
        Stage.remove("wug.txt");
        Checkout.overwriteFile("wug.txt");
    }

    // Note: checking if previous version of file is in commit works
    @Test
    public void checkout2() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit commit1 = new Commit("version 1 of wug.txt", Commit.getCurrentSha1());
        commit1.write();

        Utils.randomChangeFileContents("wug.txt");
        Stage.add("wug.txt");
        Commit commit2 = new Commit("version 2 of wug.txt", Commit.getCurrentSha1());
        commit2.write();

        // Note: checkout overwrite commit
        Checkout.overwriteCommit("wug.txt", commit1._sha1);
        assertTrue(Utils.join(Main.GITLET, "wug.txt").length() == 0);

        // Failure case: no commit exists
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Checkout.overwriteCommit("wugz.txt", "0023");
        output.close();
        assertEquals("No commit with that id exists.", output.toString());

        // Failure case, does not exist in commit
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Checkout.overwriteCommit("wugz.txt", commit1._sha1.substring(0,7));
        output.close();
        assertEquals("File does not exist in that commit.", output.toString());

    }

    // Note: test if checkout branch changes head.txt
    @Test
    public void checkOut3() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Utils.createRandomFile("wug2.txt");
        Stage.add("wug.txt");
        Stage.add("wug2.txt");
        Commit commit1 = new Commit("added wug and wug2", Commit.getCurrentSha1());
        commit1.write();

        Branch.save("Serf");
        Checkout.overwriteBranch("Serf");
        HashMap<String, String> curr = Commit.getCurrentBlobs();
        assertTrue(curr.containsKey("wug.txt"));
        assertTrue(curr.containsKey("wug2.txt"));
        assertEquals("Serf", Branch.getCurrent());
        assertEquals(commit1._sha1, Commit.getCurrentSha1());

        Checkout.overwriteBranch("master");
        Utils.createRandomFile("nanner.txt");
        Stage.add("nanner.txt");
        Commit commit2 = new Commit("added wug and wug2", Commit.getCurrentSha1());
        commit2.write();

        Checkout.overwriteBranch("Serf");
        curr = Commit.getCurrentBlobs();
        assertFalse(curr.containsKey("nanner.txt"));
        assertNotEquals(commit2._sha1, Commit.getCurrentSha1());
    }

    // Note: test if changing file in new branch doesn't affect old branch
    @Test
    public void checkOut31() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Stage stage = new Stage();
        stage.add("wug.txt");
        Commit commit1 = new Commit("added wug and wug2", Commit.getCurrentSha1());
        commit1.write();

        Branch.save("Serf");
        Checkout.overwriteBranch("Serf");

        // Failure case: checkout out branch dne
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Checkout.overwriteBranch("Cup");
        output.close();
        assertEquals("No such branch exists.", output.toString());

        // Failure case: checkout current branch
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Checkout.overwriteBranch("Serf");
        output.close();
        assertEquals("No need to checkout the current branch.", output.toString());

        // Failure case: untracked file
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Utils.createRandomFile("dwindle.txt");
        Checkout.overwriteBranch("Serf");
        output.close();
        assertEquals("There is an untracked file in the way; " +
                "delete it, or add and commit it first.", output.toString());
    }

    // Note: check basic branch swap and naming works
    @Test
    public void branchTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit commit1 = new Commit("added wug and wug2", Commit.getCurrentSha1());
        commit1.write();

        Branch.save("Serf");
        Checkout.overwriteBranch("Serf");
        assertNotEquals(Commit.zeroSha1, Commit.getCurrentSha1());
        Stage.remove("wug.txt");
        Commit commit2 = new Commit("removed wug", Commit.getCurrentSha1());
        commit2.write();
        HashMap<String, String> h = Commit.getCurrentBlobs();
        assertFalse(h.containsKey("wug.txt"));
        assertEquals("Serf", Branch.getCurrent());
        assertEquals(commit2._sha1, Commit.getCurrentSha1());

        Checkout.overwriteBranch("master");
        h = Commit.getCurrentBlobs();
        assertTrue(h.containsKey("wug.txt"));
        assertEquals("master", Branch.getCurrent());
        assertNotEquals(commit2._sha1, Commit.getCurrentSha1());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Branch.save("master");
        output.close();
        assertEquals("A branch with that name already exists.", output.toString());
    }

    // Note: remove branch file successful
    @Test
    public void removeBranch() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("dog.txt");
        Stage.add("dog.txt");
        Commit firstCommit = new Commit("first commit", Commit.getCurrentSha1());
        firstCommit.write();

        Branch.save("serf");
        Branch.remove("serf");
        assertFalse(Utils.join(Main.BRANCH, "serf").exists());
        assertNotNull(Commit.getCurrentBlobs());
        System.out.println(Commit.getCurrentBlobs());
    }

    // Note: checkout reset -- works
    @Test
    public void resetTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("notwug.txt");
        Utils.randomChangeFileContents("notwug.txt");
        Stage.add("notwug.txt");
        Utils.createRandomFile("blower.txt");
        Commit commit1 = new Commit("test Commit",Commit.getCurrentSha1());
        commit1.write();

        Utils.randomChangeFileContents("wug.txt");
        Stage.add("wug.txt");
        Stage.remove("notwug.txt");
        Commit commit2 = new Commit( "test Commit2",Commit.getCurrentSha1());
        commit2.write();

        Checkout.reset(Commit.zeroSha1);
//        assertEquals(commit1._sha1,Commit.getCurrentSha1());
//        assertTrue(Utils.join(Main.USERDIR, "notwug.txt").exists());
//        assertFalse(Utils.join(Main.USERDIR, "wug.txt").exists());
//        assertTrue(Utils.join(Main.USERDIR, "notwug.txt").length() > 0);

//        Checkout.reset(commit2._sha1);
//        assertEquals(commit2._sha1,Commit.getCurrentSha1());
//        assertTrue(Utils.join(Main.USERDIR, "wug.txt").exists());
//        assertFalse(Utils.join(Main.USERDIR, "notwug.txt").exists());
    }

    @Test
    public void checkResetFailureTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("notwug.txt");
        Utils.randomChangeFileContents("notwug.txt");
        Stage.add("notwug.txt");
        Commit commit1 = new Commit("test Commit", Commit.getCurrentSha1());
        commit1.write();

        Checkout.reset(Commit.zeroSha1);
        assertEquals(Commit.zeroSha1, Commit.getCurrentSha1());
        assertFalse(Utils.join(Main.USERDIR, "notwug.txt").exists());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Checkout.reset("1234");
        output.close();
        assertEquals("No commit with that id exists.", output.toString());

        Utils.createRandomFile("error.txt");
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Checkout.reset("1234");
        output.close();
        assertEquals("There is an untracked file in the way; " +
                "delete it, or add and commit it first.", output.toString());
    }

    // Note: find splitpoint works
    @Test
    public void findSplitPointTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();

        // Note: this is the splitpoint
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit commit1 = new Commit("1. added wug", Commit.getCurrentSha1());
        commit1.write();

        Branch.save("serf");
        Checkout.overwriteBranch("serf");

        Utils.createRandomFile("scratch.txt");
        Stage.add("scratch.txt");
        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit2.write();

        Checkout.overwriteBranch("master");
        Utils.createRandomFile("face.txt");
        Stage.add("face.txt");
        Commit commit3 = new Commit("3. commit face", Commit.getCurrentSha1());
        commit3.write();

        String splitPoint = Merge.splitPoint(commit2,commit1);
        assertEquals(splitPoint, commit1._sha1);
    }

    @Test
    public void findSplitPointTest2() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();

        Branch.save("serf");
        Checkout.overwriteBranch("serf");

        // Note: this is the splitpoint
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit commit1 = new Commit("1. added wug", Commit.getCurrentSha1());
        commit1.write();

        Utils.createRandomFile("notwug.txt");
        Stage.add("notwug.txt");
        Commit c2 = new Commit("1. added wug", Commit.getCurrentSha1());
        c2.write();


        Checkout.overwriteBranch("master");
        Utils.createRandomFile("scratch.txt");
        Stage.add("scratch.txt");
        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit2.write();

        Utils.createRandomFile("face.txt");
        Stage.add("face.txt");
        Commit commit3 = new Commit("3. commit face", Commit.getCurrentSha1());
        commit3.write();

        String splitPoint = Merge.splitPoint(commit2,commit1);
        assertEquals(Commit.zeroSha1, splitPoint);
    }
//
    // Note: current branch fast forward works!
    @Test
    public void mergeFastForwardTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
        commit1.write();
//
        Branch.save("serf");
        Checkout.overwriteBranch("serf");
        Utils.createRandomFile("mag.txt");
        Stage.add("mag.txt");
        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit2.write();

        Utils.createRandomFile("fire.txt");
        Stage.add("fire.txt");
        Commit commit3 = new Commit( "3. commit fire", Commit.getCurrentSha1());
        commit3.write();
//
        Checkout.overwriteBranch("master");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        new Merge().apply("serf");
        output.close();
        assertEquals("Current branch fast-forwarded.", output.toString());

        Commit fastCommit = Commit.getCurrent();
        assertTrue(fastCommit._mergedId == null);
        assertEquals(fastCommit._sha1, commit3._sha1);

        Checkout.overwriteBranch("serf");
        assertEquals(fastCommit._sha1, Commit.getCurrentSha1());
    }
//
    // IMPORTANT = watch out for static variables
    // Note:
    // - modified in given branch since sp, not mod. in current, -> changed to given (works)
    // - commits are same in both branches
    // - works
    @Test
    public void mergeModifiedTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
        commit1.write();
//
        Branch.save("serf");
        Checkout.overwriteBranch("serf");
        Utils.randomChangeFileContents("cup.txt");
        Stage.add("cup.txt");
        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit2.write();

        Checkout.overwriteBranch("master");
        Utils.createRandomFile("dog.txt");
        Stage.add("dog.txt");
        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit3.write();
        new Merge().apply("serf");

        String sp = Merge.splitPoint(commit3, commit2);
        Commit c = Commit.getCurrent();
        assertNotEquals(sp, commit3._sha1);
        assertEquals(commit1._sha1, sp);

        Checkout.overwriteBranch("serf");
        assertNotEquals(c, Commit.getCurrent());
    }



    @Test
    public void givenBranchAncestorTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
        commit1.write();
//
        Branch.save("serf");
        Checkout.overwriteBranch("serf");
        Utils.sameChangeFileContents("cup.txt");
        Stage.add("cup.txt");
        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit2.write();

        Checkout.overwriteBranch("master");
        Utils.sameChangeFileContents("cup.txt");
        Stage.add("cup.txt");
        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit3.write();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        new Merge().apply("serf");
        output.close();
        assertEquals("Given branch is an ancestor of the current branch.", output.toString());

    }

    // Note: file both removed -> no merge commit occurs
    @Test
    public void mergeUnmodifiedTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
        commit1.write();
//
        Branch.save("serf");
        Checkout.overwriteBranch("serf");
        Utils.sameChangeFileContents("cup.txt");
        Stage.add("cup.txt");
        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit2.write();

        Checkout.overwriteBranch("master");
        Utils.sameChangeFileContents("cup.txt");
        Stage.add("cup.txt");
        Utils.createRandomFile("another.txt");
        Stage.add("another.txt");
        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit3.write();
        new Merge().apply("serf");
        Commit m = Commit.getCurrent();

        String sp = Merge.splitPoint(commit3, commit2);
        assertNotEquals(sp, commit3._sha1);
        assertEquals(commit1._sha1, sp);
//        assertNotEquals(commit3._sha1, m._parentSha1);
    }

    @Test
    public void fileNoteRemovedCwdTest() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
        commit1.write();
//
        Branch.save("serf");
        Checkout.overwriteBranch("serf");
        Stage.remove("cup.txt");
        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit2.write();

        Checkout.overwriteBranch("master");
        Stage.remove("cup.txt");
        Utils.createRandomFile("another.txt");
        Stage.add("another.txt");
        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit3.write();
        Utils.createRandomFile("cup.txt");
        new Merge().apply("serf");

        assertTrue(Utils.join(Main.USERDIR, "cup.txt").exists());
    }

    @Test
    public void mergeAbsentInGivenBranchRemoved1() throws IOException, ClassNotFoundException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
        commit1.write();
//
        Branch.save("serf");
        Checkout.overwriteBranch("serf");
        Stage.remove("cup.txt");
        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit2.write();

        Checkout.overwriteBranch("master");
        Utils.createRandomFile("another.txt");
        Stage.add("another.txt");
        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit3.write();
        Utils.createRandomFile("cup.txt");
        new Merge().apply("serf");

        assertTrue(!Utils.join(Main.USERDIR, "cup.txt").exists());
    }

    @Test
    public void mergeConflictTest1() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
        commit1.write();

        Branch.save("serf");
        Checkout.overwriteBranch("serf");
        Utils.randomChangeFileContents("cup.txt");
        Stage.add("cup.txt");
        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit2.write();

        Checkout.overwriteBranch("master");
        Utils.randomChangeFileContents("cup.txt");
        Stage.add("cup.txt");
        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit3.write();


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        new Merge().apply("serf");
        output.close();
        assertEquals("Encountered a merge conflict.", output.toString());
    }

    @Test
    public void mergeConflictTest1x5() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();
        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
        commit1.write();

        Branch.save("serf");
        Checkout.overwriteBranch("serf");
        Utils.randomChangeFileContents("cup.txt");
        Stage.add("cup.txt");
        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit2.write();

        Checkout.overwriteBranch("master");
        Utils.randomChangeFileContents("cup.txt");
        Stage.add("cup.txt");
        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
        commit3.write();

        new Merge().apply("serf");

        Commit m = Commit.getCurrent();
        System.out.println();
        System.out.println(m._mergedId);
        assertEquals(m._mergedId.substring(0,7), commit3._sha1.substring(0,7));
        assertEquals(m._mergedId.substring(8,15), commit2._sha1.substring(0,7));
        String sp = Merge.splitPoint(commit3, commit2);
        assertNotEquals(sp, commit3._sha1);
        assertEquals(commit1._sha1, sp);
        assertEquals(commit3._sha1, m._parentSha1);

        HashMap<String, String> p1 = Commit.getBlobs(commit3._tree);
        HashMap<String, String> p2 = Commit.getBlobs(commit2._tree);
        HashMap<String, String> h = Commit.getCurrentBlobs();
        assertNotEquals(p1.get("cup.txt"), h.get("cup.txt"));
        assertNotEquals(p2.get("cup.txt"), h.get("cup.txt"));
    }

    @Test
    public void crissCrossMergeTest() throws IOException {
        Utils.clearCwdWithGitlet();
        Init.initialize();

        Branch.save("branch"); // Note does not have any fiels

        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        // Initial commit master (splits master from initial) -- contains cup
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
        Commit commit1 = new Commit("1. commit cup", Commit.getCurrentSha1());
        commit1.write();

        // Initial commit branch -- contains cup
        Checkout.overwriteBranch("branch");
//        Utils.createRandomFile("cup.txt");
//        Utils.randomChangeFileContents("cup.txt"); // Note: has contents
//        Stage.add("cup.txt"); // Note: supposed to show 'no change added' if commits have same file?
//
//        Commit commit2 = new Commit("2. remove cup", Commit.getCurrentSha1());
//        commit2.write();
//        assertNotEquals(commit1._sha1,commit2._sha1);
//        assertEquals("branch",Branch.getCurrent());
//        assertEquals(commit2._sha1, Branch.read("branch"));
//
//        // Create temp branch
//        Branch.save("temp"); // contains cup with content
//
//        // Merge master into branch
//        new Merge().apply("master"); // Conflict file with cup.txt (branch has content)
//        System.out.print("Merge 1");
//        Commit m = Commit.getCurrent();
//        System.out.println();
//        System.out.println(m._mergedId);
//        assertEquals(m._mergedId.substring(0,7), commit2._sha1.substring(0,7));
//        assertEquals(m._mergedId.substring(8,15), commit1._sha1.substring(0,7));
//
////         Make another commit
//        Checkout.overwriteBranch("master"); // - contains cup and mood
//        Utils.createRandomFile("mood.txt");
//        Stage.add("mood.txt");
//        Commit commit3 = new Commit("4. mood.txt", Commit.getCurrentSha1());
//        commit3.write();
//
////         Merge temp into master
//        new Merge().apply("temp"); // Conflict file cup
//        System.out.print("Merge 2");
//        m = Commit.getCurrent();
//        System.out.println();
//        System.out.println(m._mergedId);
//        assertEquals(m._mergedId.substring(0,7), commit3._sha1.substring(0,7));
//        assertEquals(m._mergedId.substring(8,15), commit2._sha1.substring(0,7));
////
////         Make another commit branch
//        Checkout.overwriteBranch("branch"); // -- contains cup, spatula
//        Utils.createRandomFile("spatula.txt");
//        Stage.add("spatula.txt");
//        Commit commit5 = new Commit("2. spatula cup", Commit.getCurrentSha1());
//        commit5.write();
//        ArrayList<String> branch = Utils.getCommitArray(commit5, new ArrayList<>());
//
//        // Make another commit master
//        Checkout.overwriteBranch("master"); // -- contains fork, spatula, mood, cup
//        Utils.createRandomFile("fork.txt");
//        Stage.add("fork.txt");
//        Commit commit7 = new Commit("added fork", Commit.getCurrentSha1());
//        commit7.write();
//        ArrayList<String> master = Utils.getCommitArray(commit7, new ArrayList<>());
//
//        System.out.println(branch);
//        System.out.println(master);
//
//        System.out.println("Merge 3");
//        new Merge().apply("branch");
////
//        Collections.reverse(master);
//        Collections.reverse(branch);
//
//        String sp = Merge.splitPoint(commit7, commit5);
//        Commit merge = Utils.deserializeCommit(Commit.getCurrentSha1());
//        System.out.println();
//        System.out.println(merge._mergedId); // Criss cross merge doesn't work
//        assertEquals(sp, branch.get(1));
    }

//    // Note: works! (rebase command)
//    @Test
//    public void findBranchSplitPointTest() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
//        commit1.write();
////
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Utils.randomChangeFileContents("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteBranch("master");
//        Utils.createRandomFile("dog.txt");
//        Stage.add("dog.txt");
//        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit3.write();
//
//        String sp = Merge.splitPoint(commit2,commit3);
//        Commit spCommit = Merge.findBranchSplit(commit2,sp);
//
//        Checkout.overwriteBranch("serf");
//        System.out.println("Splitpoint = " + sp);
//        System.out.println("Branch split = " + spCommit._sha1);
//
//        Log.printLog();
//        assertEquals(sp, spCommit._parentSha1);
//    }

//    @Test
//    public void rebaseTest() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit cup", Commit.getCurrentSha1());
//        commit1.write();
////
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Utils.randomChangeFileContents("cup.txt");
//        Commit commit2 = new Commit("2. modified cup", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteBranch("master");
//        Utils.createRandomFile("dog.txt");
//        Stage.add("dog.txt");
//        Commit commit3 = new Commit("3. commit dog", Commit.getCurrentSha1());
//        commit3.write();
//
//        Rebase.apply("serf");
////        Log.printLog(); // initial -> 3. commit dog -> 1. commit cup -> 2. modified cup
//        Log.printGlobal();
//    }
}
