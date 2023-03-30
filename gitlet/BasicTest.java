package gitlet;

import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import static org.junit.Assert.*;

// Test basic gitlet commands with shallow depth
public class BasicTest {

    // Clear current working directory and .gitlet folder (if exists)
    @Before
    public void initialize() throws IOException {
        Utils.clearCwdWithGitlet();
        Main.main("init");
    }

    // Test initial commit valid and no blobs exist
    @Test
    public void initializeGitletTest() {

        Commit c = Commit.getCurrent();
        assertNotNull(c);
        assertEquals(Commit.zeroSha1, Commit.getCurrentSha1());
        assertNotNull(c._tree);
        HashMap<String, String> t = Commit.getBlobs(c._tree);
        assertTrue(t.isEmpty());
    }

    // Test different files types properly added to stage 
    @Test
    public void addToStageTest() throws IOException {
        
        Utils.createRandomFile("cube.txt");
        Stage s = Stage.read();
        assertTrue(s._preStage.isEmpty());
        assertTrue(s._deletion.isEmpty());

        // Check if news files added to stage
        Stage.add("cube.txt");
        Stage stage = Stage.read();
        assertTrue(stage._preStage.containsKey("cube.txt"));

        // Check adding file that DNE
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Stage.add("map.txt");
        output.close();
        
        assertEquals("File does not exist.", output.toString());
    }

    // Test user input of staging file
    @Test
    public void addToStageUser() throws IOException {

        Utils.createRandomFile("cube.txt");
        Stage s = Stage.read();
        assertTrue(s._preStage.isEmpty());
        assertTrue(s._deletion.isEmpty());
    
        // User manually adds file to stage  
        Main.main("add","cube.txt");
        
        // Check file in staging
        Stage stage = Stage.read();
        assertTrue(stage._preStage.containsKey("cube.txt"));
    }

    // Test basic commit flow
    @Test
    public void commitTest() throws IOException {
        
        // Create commit of new file
        Utils.createRandomFile("wug.txt");
        Main.main("add","wug.txt");
        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentSha1());
        firstCommit.write();

        // Check if commit folder updated, commit tree contains files, head changes
        assertEquals(2, Main.COMMITS.listFiles().length);
        HashMap<String, String> commitBlobs = Commit.getBlobs(firstCommit._tree);
        assertTrue(commitBlobs.containsKey("wug.txt"));
        assertTrue(Stage.read() != null);
        
        // Test HEAD is pointing to master branch
        assertEquals("master", Utils.deserialize(Main.HEAD, String.class));
        
        // Check commit SHA1 matches current commit SHA1
        assertEquals(firstCommit._sha1, Commit.getCurrentSha1());

        // Check added files does not change commit blobs
        Utils.createRandomFile("notwug.txt");
        commitBlobs = Commit.getBlobs(firstCommit._tree);
        assertFalse(commitBlobs.containsKey("notwug.txt"));
        
        // Check total SHA1's commits have
        ArrayList<String> commitHistory = Utils.getTotalSha1History(firstCommit, new ArrayList<>());
        assertTrue(commitHistory.contains(firstCommit._sha1));
        assertTrue(commitHistory.size() == 2);
    }

    // Test having two committing actions
    @Test
    public void commitTwoTest() throws IOException {

        // Create and commit random file
        Utils.createRandomFile("wug.txt");
        Main.main("add", "wug.txt");
        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentSha1());
        firstCommit.write();

        // Create and commit another random file
        Utils.createRandomFile("notwug.txt");
        Main.main("add", "notwug.txt");

        // Check wug.txt not in stage, notwug.txt in stage
        assertFalse(Stage.read()._preStage.containsKey("wug.txt"));
        assertTrue(Stage.read()._preStage.containsKey("notwug.txt"));

        // Check if stage cleared, current commit pointer updated
        Commit currentCommit = new Commit("added not wug.txt", Commit.getCurrentSha1());

        // Check new commit has not been committed yet
        assertNotEquals(currentCommit._sha1, Commit.getCurrentSha1());

        // Commit new commit
        currentCommit.write();

        // Check new commit is current ocmmit
        assertEquals(currentCommit._sha1, Commit.getCurrentSha1());

        // Check valid parent commit of new commit
        assertEquals(currentCommit._parentSha1, firstCommit._sha1);

        // Check commit metadata is correct
        assertEquals("added not wug.txt", currentCommit._logMessage);
        assertNotEquals(firstCommit._tree, currentCommit._tree);
    }

    // Test staging unmodified file that has already been committed
    @Test
    public void repeatStagingFileTest() throws IOException {

        // Stage and commit random file
        Utils.createRandomFile("cube.txt");
        Main.main("add","cube.txt");
        Main.main("commit","added cube");
        Stage stage = Stage.read();

        // Check stage does not have file
        assertFalse(stage._preStage.containsKey("cube.txt"));

        // Re-stage same file again
        Stage.add("cube.txt");
        stage = Stage.read();

        // Check file is not staged since not changes have been made
        assertFalse(stage._preStage.containsKey("cube.txt"));
    }

    // Test commit failure cases
    @Test
    public void commitFailureCasesTest() throws IOException {

        // Create random file
        Utils.createRandomFile("wug.txt");

        // 1. Failure case: no file added
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("commit", "added wug.txt");
        output.close();

        assertEquals("No changes added to the commit.", output.toString());

        // 2. Failure case: Empty commit message
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Main.main("commit");
        output.close();

        assertEquals("Please enter a commit message.", output.toString());
    }

    // Test successfully removing committed file from current commit
    @Test
    public void removeFileFromCommitTest() throws IOException {

        // Create random file and commit
        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentSha1());
        firstCommit.write();

        // Check file is removed from stage and is in deletion hashmap
        Stage.remove("wug.txt");
        Stage stage = Stage.read();
        assertTrue(stage._preStage.isEmpty());
        assertTrue(stage._deletion.containsKey("wug.txt"));

        // Commit removal of file
        Commit second = new Commit("removed wug.txt", Commit.getCurrentSha1());
        second.write();

        // Check stage is empty and deletion hashmap does not have file
        stage = Stage.read();
        assertTrue(stage._preStage.isEmpty());
        assertFalse(stage._deletion.containsKey("wug.txt"));

        // Check file untracked in current commit
        HashMap<String, String> secondCommit = Commit.getBlobs(second._tree);
        assertFalse(secondCommit.containsKey("wug.txt"));
        assertTrue(secondCommit.isEmpty());
    }

    // Test log run successfully
    @Test
    public void logTest() throws IOException {

        Main.main("log");
    }

    // Test global log runs successfully
    @Test
    public void globalLogTest() throws IOException {

        Main.main("global-log");
    }

    // Test find commit
    @Test
    public void findTest() throws IOException {

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

    // status command in spec
    @Test
    public void statusTest() throws IOException {

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
    public void statusTest1() throws IOException {

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

    // checkout for previous versions of file work
    @Test
    public void checkout() throws IOException {

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

    // checking if previous version of file is in commit works
    @Test
    public void checkout2() throws IOException {

        Utils.createRandomFile("wug.txt");
        Stage.add("wug.txt");
        Commit commit1 = new Commit("version 1 of wug.txt", Commit.getCurrentSha1());
        commit1.write();

        Utils.randomChangeFileContents("wug.txt");
        Stage.add("wug.txt");
        Commit commit2 = new Commit("version 2 of wug.txt", Commit.getCurrentSha1());
        commit2.write();

        // checkout overwrite commit
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

    // test if checkout branch changes head.txt
    @Test
    public void checkOut3() throws IOException {

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

    // test if changing file in new branch doesn't affect old branch
    @Test
    public void checkOut31() throws IOException {

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

    // check basic branch swap and naming works
    @Test
    public void branchTest() throws IOException {

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

    // remove branch file successful
    @Test
    public void removeBranch() throws IOException {

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

    // checkout reset -- works
    @Test
    public void resetTest() throws IOException {

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

    // find splitpoint works
    @Test
    public void findSplitPointTest() throws IOException {

        // this is the splitpoint
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

        Branch.save("serf");
        Checkout.overwriteBranch("serf");

        // this is the splitpoint
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
    // current branch fast forward works!
    @Test
    public void mergeFastForwardTest() throws IOException {
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
    public void mergeModifiedTest() throws IOException {

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
    public void givenBranchAncestorTest() throws IOException {

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

    // file both removed -> no merge commit occurs
    @Test
    public void mergeUnmodifiedTest() throws IOException {

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
    public void fileNoteRemovedCwdTest() throws IOException {

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
    public void mergeAbsentInGivenBranchRemoved1() throws IOException {

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

        Branch.save("branch"); // Note does not have any fiels

        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        // Initial commit master (splits master from initial) -- contains cup
        Utils.createRandomFile("cup.txt");
        Stage.add("cup.txt");
        Commit commit1 = new Commit("1. commit cup", Commit.getCurrentSha1());
        commit1.write();

        // Initial commit branch -- contains cup
        Checkout.overwriteBranch("branch");
        Utils.createRandomFile("cup.txt");
        Utils.randomChangeFileContents("cup.txt"); // has contents
        Stage.add("cup.txt"); // supposed to show 'no change added' if commits have same file?

        Commit commit2 = new Commit("2. remove cup", Commit.getCurrentSha1());
        commit2.write();
        assertNotEquals(commit1._sha1,commit2._sha1);
        assertEquals("branch",Branch.getCurrent());
        assertEquals(commit2._sha1, Branch.read("branch"));

        // Create temp branch
        Branch.save("temp"); // contains cup with content

        // Merge master into branch
        new Merge().apply("master"); // Conflict file with cup.txt (branch has content)
        System.out.print("Merge 1");
        Commit m = Commit.getCurrent();
        System.out.println();
        System.out.println(m._mergedId);
        assertEquals(m._mergedId.substring(0,7), commit2._sha1.substring(0,7));
        assertEquals(m._mergedId.substring(8,15), commit1._sha1.substring(0,7));

//         Make another commit
        Checkout.overwriteBranch("master"); // - contains cup and mood
        Utils.createRandomFile("mood.txt");
        Stage.add("mood.txt");
        Commit commit3 = new Commit("4. mood.txt", Commit.getCurrentSha1());
        commit3.write();

//         Merge temp into master
        new Merge().apply("temp"); // Conflict file cup
        System.out.print("Merge 2");
        m = Commit.getCurrent();
        System.out.println();
        System.out.println(m._mergedId);
        assertEquals(m._mergedId.substring(0,7), commit3._sha1.substring(0,7));
        assertEquals(m._mergedId.substring(8,15), commit2._sha1.substring(0,7));
//
//         Make another commit branch
        Checkout.overwriteBranch("branch"); // -- contains cup, spatula
        Utils.createRandomFile("spatula.txt");
        Stage.add("spatula.txt");
        Commit commit5 = new Commit("2. spatula cup", Commit.getCurrentSha1());
        commit5.write();
        ArrayList<String> branch = Utils.getCommitArray(commit5, new ArrayList<>());

        // Make another commit master
        Checkout.overwriteBranch("master"); // -- contains fork, spatula, mood, cup
        Utils.createRandomFile("fork.txt");
        Stage.add("fork.txt");
        Commit commit7 = new Commit("added fork", Commit.getCurrentSha1());
        commit7.write();
        ArrayList<String> master = Utils.getCommitArray(commit7, new ArrayList<>());

        System.out.println(branch);
        System.out.println(master);

        System.out.println("Merge 3");
        new Merge().apply("branch");
//
        Collections.reverse(master);
        Collections.reverse(branch);

        String sp = Merge.splitPoint(commit7, commit5);
        Commit merge = Utils.deserializeCommit(Commit.getCurrentSha1());
        System.out.println();
        System.out.println(merge._mergedId); // Criss cross merge doesn't work
        assertEquals(sp, branch.get(1));
    }

//    ----- REBASE TESTS -----

//    @Test
//    public void findBranchSplitPointTest() throws IOException {
//
//        
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
//
//        
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
