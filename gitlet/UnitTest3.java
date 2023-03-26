//package gitlet;
//
//import org.junit.Test;
//
//import java.io.IOException;
//
//import static org.junit.Assert.assertEquals;
//
//public class UnitTest3 {
//
//    // merging with item still in cwd
//    @Test
//    public void mergeConflictTest4() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
//        commit1.write();
//
//        Branch.save("serf");
//
//        Utils.createRandomFile("mag.txt");
//        Stage.add("mag.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit2.write();
//
//        Utils.randomChangeFileContents("mag.txt");
//        new Merge().apply("serf");
//    }
//
//    // Q35 - Check reset by:
//    // 1. create 2 branch with 2 diff commits
//    // 2. on master, introduce and add another file but reset to another commit on master
//    //      - clear staging area and move master branch pointer (check log here)
//    // 3. check out between master and other branch to make sure correct commit in log
//    // 4. reset master back to original and ensure all original commits are there (log)
//    @Test
//    public void mergeRmConflictTest() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. added cup", Commit.getCurrentSha1());
//        commit1.write();
//
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Utils.createRandomFile("mug.txt");
//        Stage.add("mug.txt");
//        Commit commit2 = new Commit("2. add mug", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteBranch("master");
//        Utils.randomChangeFileContents("box.txt");
//
//        Stage.add("box.txt");
//        Checkout.reset(Commit.zeroSha1);
//        assertEquals(Commit.zeroSha1, Commit.getCurrentSha1());
//        Log.printLog();
//
//        Checkout.reset(commit1._sha1);
//        assertEquals(commit1._sha1, Commit.getCurrentSha1());
////        Log.printLog();
//    }
//
//
//    @Test
//    public void mergeRmConflictTest2() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. added cup", Commit.getCurrentSha1());
//        commit1.write();
//
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Utils.createRandomFile("mug.txt");
//        Stage.add("mug.txt");
//        Commit commit2 = new Commit("2. add mug", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteBranch("master");
//        Utils.randomChangeFileContents("box.txt");
//        Stage.add("box.txt");
//        Commit commit3 = new Commit("3. added box", Commit.getCurrentSha1());
//        commit3.write();
//
//        Utils.randomChangeFileContents("anotherFile.txt");
//        Stage.add("anotherFile.txt");
////
//        Checkout.reset(commit1._sha1);
//        assertEquals(commit1._sha1, Commit.getCurrentSha1());
//
//        Checkout.reset(commit3._sha1);
//        assertEquals(commit3._sha1, Commit.getCurrentSha1());
//        Status.print();
//        Log.printLog();
//    }
//
//    // Q36 - reset to nonexistent comment failure message
//    // - not sure what this error is
//    @Test
//    public void mergeErrTest() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("wug.txt");
//        Stage.add("wug.txt");
//        Commit c = new Commit("added wug.txt", Commit.getCurrentSha1());
//        c.write();
//        Checkout.reset("00");
//        Status.print();
//    }
//
//    // Q36a - check that checkout works with short ID's
//    @Test
//    public void mergeParent2() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("wug.txt");
//        Utils.createRandomFile("wug2.txt");
//        Stage.add("wug.txt");
//        Stage.add("wug2.txt");
//        Commit commit1 = new Commit("added wug and wug2", Commit.getCurrentSha1());
//        commit1.write();
//
//        Branch.save("Serf");
//        Checkout.overwriteBranch("Serf");
//        Checkout.reset("00000");
//        assertEquals(Commit.zeroSha1, Commit.getCurrentSha1());
//    }
//
//    @Test
//    public void mergeParent3() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("wug.txt");
//        Stage.add("wug.txt");
//        Commit commit1 = new Commit("added wug and wug2", Commit.getCurrentSha1());
//        commit1.write();
//
//        Utils.createRandomFile("iphone.txt");
//        Utils.createRandomFile("horse.txt");
//        Stage.add("iphone.txt");
//        Stage.add("horse.txt");
//        Commit commit2 = new Commit("added wug and wug2", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.reset(commit1._sha1);
//        Checkout.reset(commit2._sha1);
//        Checkout.reset(commit1._sha1);
//        assertEquals(commit1._sha1, Commit.getCurrentSha1());
//    }
//
//    // Q37 - special cases of merge (fast forward and ancestor)
//    // branch fast forward
//    // - splitpoint is current branch
//    // - current branch is ancestor of given branch
//    @Test
//    public void mergeConflictTest1() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
//        commit1.write();
////
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Utils.createRandomFile("mag.txt");
//        Stage.add("mag.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit2.write();
//
//        Utils.createRandomFile("fire.txt");
//        Stage.add("fire.txt");
//        Commit commit3 = new Commit( "3. commit fire", Commit.getCurrentSha1());
//        commit3.write();
//
//        Checkout.overwriteBranch("master");
//        new Merge().apply("serf");
////        String commitSHA1 = Branch.read("master");
////        assertEquals(commitSHA1,commit3._sha1);
//    }
//
//    // splitpoint is the current branch
//    // - do nothing, merge complete and ends with 'given branch is ancestor of current branch'
//    @Test
//    public void mergeConflictTest2() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
//        commit1.write();
//
//        Branch.save("serf");
//
//        Utils.createRandomFile("mag.txt");
//        Stage.add("mag.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit2.write();
//
//        new Merge().apply("serf");
//
//    }
//
//    // Q38 - bad reset error (file in the way)
//    @Test
//    public void badResetError() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("wug.txt");
//        Utils.createRandomFile("wug2.txt");
//        Stage.add("wug.txt");
//        Stage.add("wug2.txt");
////        Stage.add("iphone.txt");
//        Commit commit1 = new Commit("added wug and wug2", Commit.getCurrentSha1());
//        commit1.write();
//
//        Branch.save("Serf");
//        Checkout.overwriteBranch("Serf");
//        Utils.createRandomFile("calendar.txt");
//
//        Checkout.reset("0001");
//        assertEquals(commit1._sha1, Commit.getCurrentSha1());
//    }
//
//    // Q39 - checkout file at specific commit using short uid
//    @Test
//    public void shortUidTest() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("wug.txt");
//        Stage.add("wug.txt");
//        Commit commit1 = new Commit("added wug", Commit.getCurrentSha1());
//        commit1.write();
//
//        Utils.randomChangeFileContents("wug.txt");
//        Stage.add("wug.txt");
//        Commit commit2 = new Commit("modified wug", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteCommit("wug.txt",commit1._sha1.substring(0,5));
//    }
//
//    // Q40 - merge special cases
//    // - modified in current branch and given branch in same way
//    // - Note: shows 'no changes added to the commit'
//    @Test
//    public void specialMergeCases() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit cup", Commit.getCurrentSha1());
//        commit1.write();
////
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Utils.sameChangeFileContents("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit2 = new Commit("2. modified cup", Commit.getCurrentSha1());
//        commit2.write();
//        String curr = Commit.getCurrentSha1();
//        assertEquals(curr, commit2._sha1);
//
//
//        Checkout.overwriteBranch("master");
//        Utils.sameChangeFileContents("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit3 = new Commit("3. modified cup", Commit.getCurrentSha1());
//        commit3.write();
//        curr = Commit.getCurrentSha1();
//        assertEquals(curr, commit3._sha1);
//
//        String commit = Merge.splitPoint(commit1,commit2);
//        assertEquals(commit,commit1._sha1);
//
//        new Merge().apply("serf");
//        Checkout.overwriteBranch("serf");
//        assertEquals(commit2._tree, commit3._tree); // Note: trees are the same
//    }
//
//    // - file is removed in both branches, but name present in cwd -> file is not removed
//    @Test
//    public void specialMergeCases1() throws IOException {
//        Utils.clearCwdWithGitlet();
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit cup", Commit.getCurrentSha1());
//        commit1.write();
////
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Stage.remove("cup.txt");
//        Commit commit2 = new Commit("2. modified cup", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteBranch("master");
//        Stage.remove("cup.txt");
//        Commit commit3 = new Commit( "3. modified cup", Commit.getCurrentSha1());
//        commit3.write();
//
//        Utils.createRandomFile("cup.txt");
//
//        new Merge().apply("serf");
////        Checkout.overwriteBranch("serf");
////        assertEquals(commit2._tree,commit3._tree); // Note: trees are the same
//    }
//
//
//}
