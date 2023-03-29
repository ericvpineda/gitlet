//package gitlet;
//
//import org.junit.Test;
//
//import java.io.IOException;
//
//import static org.junit.Assert.assertEquals;
//
//public class UnitTest2 {
//
//    String iphone = System.getProperty("user.dir") + "/iphone.txt";
//    String wug = System.getProperty("user.dir") + "/wug.txt";
//    String scratch = System.getProperty("user.dir") + "/scratch.txt";
//
//    // Q23 - Check global log prints out all commits
//    @Test
//    public void globalLogTest() throws IOException, ClassNotFoundException {
//
//        Init.initialize();
//        Utils.createRandomFile("beaver.txt");
//        Stage.add("beaver.txt");
//        Commit firstCommit = new Commit("added beaver.txt",Commit.getCurrentSha1());
//        firstCommit.write();
//
//        Utils.createRandomFile("dog.txt");
//        Stage.add("dog.txt");
//        Commit secondCommit = new Commit("added dog.txt",Commit.getCurrentSha1());
//        secondCommit.write();
//
//        Log.printGlobal();
//    }
//
//    // Q24 - Check global-lgo prints out commits no longer in any branch
//    @Test
//    public void globalLogPrevTest() throws IOException, ClassNotFoundException {
//
//        Init.initialize();
//        Utils.createRandomFile("beaver.txt");
//        Stage.add("beaver.txt");
//        Commit firstCommit = new Commit("added beaver.txt",Commit.getCurrentSha1());
//        firstCommit.write();
//
//        Utils.createRandomFile("dog.txt");
//        Stage.add("dog.txt");
//        Commit secondCommit = new Commit("added dog.txt",Commit.getCurrentSha1());
//        secondCommit.write();
//
//        Checkout.reset("00000");
//        String branchSha1 = Commit.getCurrentSha1();
//        assertEquals(Commit.zeroSha1, branchSha1);
//
//        Log.printGlobal();
//    }
//
//    // Note: Q25 - Test find command when it succeeds
//    @Test
//    public void successfulFindTest() throws IOException {
//
//        Init.initialize();
//        Utils.createRandomFile("beaver.txt");
//        Stage.add("beaver.txt");
//        Commit firstCommit = new Commit("added beaver.txt",Commit.getCurrentSha1());
//        firstCommit.write();
//
//        Utils.createRandomFile("dog.txt");
//        Stage.add("dog.txt");
//        Commit secondCommit = new Commit("added beaver.txt",Commit.getCurrentSha1());
//        secondCommit.write();
//
//        Find.find("added beaver.txt");
//    }
//
//    // Q26 - Find command to find commits no longer on any branch
//    @Test
//    public void successfulFindOrphan() throws IOException {
//
//        Init.initialize();
//        Utils.createRandomFile("beaver.txt");
//        Stage.add("beaver.txt");
//        Commit firstCommit = new Commit("added beaver.txt",Commit.getCurrentSha1());
//        firstCommit.write();
//
//        Utils.createRandomFile("dog.txt");
//        Stage.add("dog.txt");
//        Commit secondCommit = new Commit("added dog.txt",Commit.getCurrentSha1());
//        secondCommit.write();
//
//        Checkout.reset(firstCommit._sha1);
//        String branchSha1 = Commit.getCurrentSha1();
//        assertEquals(branchSha1, firstCommit._sha1);
//
//        Find.find("added dog.txt");
//    }
//
//    // Q27 - Checkout a previous version of file from previous commit
//    @Test
//    public void checkoutDetailTest() throws IOException {
//
//        Init.initialize();
//        Utils.createRandomFile("wug.txt");
//        Stage.add("wug.txt");
//        Commit commit1 = new Commit("added wug.txt",Commit.getCurrentSha1());
//        commit1.write();
//
//        Utils.randomChangeFileContents("wug.txt");
//        Checkout.overwriteFile("wug.txt");
//        Status.print();
//    }
//
//    // Q28 - Checkout previous version of file form previous commit
//    @Test
//    public void checkoutDetailTest1() throws IOException {
//
//        Init.initialize();
//        Utils.createRandomFile("wug.txt");
//        Stage.add("wug.txt");
//        Commit commit1 = new Commit("added wug.txt",Commit.getCurrentSha1());
//        commit1.write();
//
//        Utils.randomChangeFileContents("wug.txt");
//        Stage.add("wug.txt");
//        Commit commit2 = new Commit("modified wug",Commit.getCurrentSha1());
//        commit2.write();
//
////        Utils.randomChangeFileContents("wug.txt");
//        Checkout.overwriteCommit("wug.txt",commit1._sha1);
//        // Note: has file as (modified)
//        Status.print();
//
//    }
//
//    // Q29 - Checkout failure cases (3)
//    @Test
//    public void checkoutFailure1() throws IOException {
//
//        Init.initialize();
//        Checkout.overwriteCommit("wug.txt",Commit.zeroSha1);
//    }
//
//    @Test
//    public void checkoutFailure2() throws IOException {
//
//        Init.initialize();
//        Checkout.overwriteCommit("wug.txt","12345");
//    }
//
//    @Test
//    public void checkoutFailure3() throws IOException {
//
//        Init.initialize();
//        Checkout.overwriteBranch("serf");
//    }
//
//    @Test
//    public void checkoutFailure4() throws IOException {
//
//        Main.main("init");
//        Main.main("checkout","a4adbcbfd1f2d7b07fbf8029c8ded51de5034e97", "++","wug.txt");
//    }
//
//    // Q30 - Create 2 branches, track diff files and switch between them
//    //      - files added, modified, removed
//    // 1. Files added branch test
//    @Test
//    public void branchesTest() throws IOException {
//
//        Init.initialize();
//        Utils.createRandomFile("wug.txt");
//        Stage.add("wug.txt");
//        Commit c1 = new Commit("added wug.txt", Commit.getCurrentSha1());
//        c1.write();
//
//        Branch.save("other-branch");
//        Checkout.overwriteBranch("other-branch");
//        Utils.createRandomFile("balls.txt");
//        Stage.add("balls.txt");
//        Commit c2 = new Commit("added balls", Commit.getCurrentSha1());
//        c2.write();
//
//        Checkout.overwriteBranch("master");
//    }
//
//    // 2. Files modified
//    @Test
//    public void branchesTest1() throws IOException {
//
//        Init.initialize();
//        Utils.createRandomFile("wug.txt");
//        Stage.add("wug.txt");
//        Commit c1 = new Commit("added wug.txt", Commit.getCurrentSha1());
//        c1.write();
//
//        Branch.save("other-branch");
//        Checkout.overwriteBranch("other-branch");
//        Utils.randomChangeFileContents("wug.txt");
//        Stage.add("wug.txt");
//        Commit c2 = new Commit("added balls", Commit.getCurrentSha1());
//        c2.write();
//
//        Checkout.overwriteBranch("master");
//    }
//
//    // 3). Files removed ??
//    // - note: what happens when you have file removed and switch branches
//    // - i think it is supposed to be cleared
//    @Test
//    public void branchesTest2() throws IOException {
//
//        Init.initialize();
//        Utils.createRandomFile("wug.txt");
//        Stage.add("wug.txt");
//        Commit c1 = new Commit("added wug.txt", Commit.getCurrentSha1());
//        c1.write();
//
//        Stage.remove("wug.txt");
//        Branch.save("other-branch");
//        Checkout.overwriteBranch("other-branch");
//
//        Stage.remove("wug.txt");
//        Commit c2 = new Commit("added balls", Commit.getCurrentSha1());
//        c2.write();
//
//        Checkout.overwriteBranch("master");
////        Checkout.overwriteBranch("other-branch");
//        Status.print();
//    }
//
//    // Q31a - Check that 2 branches can't be given same name
//    @Test
//    public void rmBranches() throws IOException, ClassNotFoundException {
//
//        Init.initialize();
//        Branch.save("other-branch");
//        Branch.save("other-branch");
//    }
//
//    // Q33 - create 2 branches, merge with mather with 2 different file types -> merge conflict
//    @Test
//    public void fileOverWriteErrTest() throws IOException {
//
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
//        commit1.write();
//
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Utils.randomChangeFileContents("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteBranch("master");
//        Utils.randomChangeFileContents("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit3.write();
//
//        new Merge().apply("serf");
//    }
//
//    @Test
//    public void fileOverWriteErrTest1() throws IOException {
//
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
//        commit1.write();
//
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Utils.randomChangeFileContents("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteBranch("master");
//        Utils.randomChangeFileContents("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit3.write();
//
//        new Merge().apply("serf");
//    }
//
//
//    @Test
//    public void fileOverWriteErrTest2() throws IOException {
//
//        Init.initialize();
//        Branch.save("serf");
//
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
//        commit1.write();
//
//        Checkout.overwriteBranch("serf");
//        Utils.randomChangeFileContents("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteBranch("master");
//        new Merge().apply("serf");
//    }
//
//    // Q33 - create 2 branches, merge into master with conflict caused by file changed in one and removed in other
//    @Test
//    public void mergeNoConflictTest() throws IOException, ClassNotFoundException {
//
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
//        commit1.write();
//
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Utils.testFileMergeConflict("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteBranch("master");
//        Stage.remove("cup.txt");
//        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit3.write();
//
//        new Merge().apply("serf");
//    }
//
//    @Test
//    public void mergeNoConflictTest2() throws IOException{
//
//        Init.initialize();
//        Utils.createRandomFile("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. commit dog", Commit.getCurrentSha1());
//        commit1.write();
//
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Stage.remove("cup.txt");
//        Commit commit2 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit2.write();
//
//        Checkout.overwriteBranch("master");
//        Utils.randomChangeFileContents("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit3 = new Commit("2. commit scratch", Commit.getCurrentSha1());
//        commit3.write();
//
//        new Merge().apply("serf");
//    }
//
//    // Q34 - checkout all merge errors cases
//    // - file absent at split point and contents differ in given and current branch
//    @Test
//    public void mergeConflictsTest() throws IOException{
//
//        Init.initialize();
//
//        Branch.save("serf");
//        Checkout.overwriteBranch("serf");
//        Utils.testFileMergeConflict("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit1 = new Commit("1. added cup", Commit.getCurrentSha1());
//        commit1.write();
////        Tree t1 = Utils.deserialize(Utils.findFile(commit1._tree, Main.TREE), Tree.class);
////        System.out.println(t1._blobList);
//
//        Checkout.overwriteBranch("master");
//        Utils.testFileMergeConflict("cup.txt");
//        Stage.add("cup.txt");
//        Commit commit2 = new Commit("2. modified", Commit.getCurrentSha1());
//        commit2.write();
////        Tree t2 = Utils.deserialize(Utils.findFile(commit2._tree, Main.TREE), Tree.class);
////        System.out.println(t2._blobList);
//
//        new Merge().apply("serf"); // Note: merge file has [current === given]
//    }
//
//    // failure case with staged items
//    @Test
//    public void mergeConflictTest3() throws IOException {
////        Init.initialize();
//        Status.print();
////        new Merge().apply("serf");
//
//    }
//
//    // failure case - cannot merge branch with itself
//    @Test
//    public void mergeConflictTest4() throws IOException {
//        Init.initialize();
//        new Merge().apply("master");
//    }
//
//    // failure case - having untracked file
//    @Test
//    public void mergeConflictTest5() throws IOException {
//        Init.initialize();
//        Utils.createRandomFile("errorFile.txt");
//
//        new Merge().apply("serf");
//
//    }
//}
//
