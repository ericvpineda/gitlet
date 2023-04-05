//package gitlet;
//
//import org.junit.Test;
//
//import java.io.IOException;
//
//import static org.junit.Assert.assertNotEquals;
//
//public class UnitTest4 {
//
//    @Test
//    public void specialMergeCases1() throws IOException {
//
//        Init.initialize();
//        Utils.createEmptyFile("cup.txt");
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
//        Utils.createEmptyFile("mug.txt");
//        Stage.add("mug.txt");
//        Commit commit3 = new Commit( "3. modified cup", Commit.getCurrentSha1());
//        commit3.write();
//
//        new Merge().apply("serf"); // Note cup.txt should be removed
//        assertNotEquals(commit3._sha1, Commit.getCurrentSha1());
//
//    }
//
//    // Q43 - Criss cross merge??
//
//
//    @Test
//    public void remoteAddTest() throws IOException {
//
//        Init.initialize();
//        String dr = "C:\\Users\\evpin\\Desktop\\gitTest";
//        Remote.add("eric",dr);
//    }
//
//    @Test
//    public void removeRemoteTest() throws IOException {
//
//        Init.initialize();
//        String dr = "C:\\Users\\evpin\\Desktop\\gitTest";
//        Remote.add("eric",dr);
////        Remote.remove("eric");
//    }
//
//    @Test
//    public void setUpDir() throws IOException {
//        Init.initialize();
//        Utils.createEmptyFile("notwug.txt");
//        Stage.add("notwug.txt");
//        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentSha1());
//        firstCommit.write();
//        Remote.add("eric","C:\\Users\\evpin\\Desktop\\gitTest");
//    }
//
//    @Test
//    public void pushTest() throws IOException {
//        Remote.push("eric","other-branch");
//    }
//
//    @Test
//    public void fetchTestHelper() throws IOException {
//        Init.initialize();
//        Remote.add("eric","C:\\Users\\evpin\\Desktop\\gitTest");
//    }
//
//    @Test
//    public void fetchTest() throws IOException {
//        Remote.fetch("eric", "other-branch");
//    }
//
//    @Test
//    public void removeRemote() throws IOException {
//        Remote.remove("eric");
//
//    }
//
//    @Test
//    public void remoteFetchPush() throws IOException {
//        Init.initialize();
//        Utils.createEmptyFile("notwug.txt");
//        Stage.add("notwug.txt");
//        Commit firstCommit = new Commit("version 1 wug", Commit.getCurrentSha1());
//        firstCommit.write();
//
//        Remote.add("eric","C:\\Users\\evpin\\Desktop\\gitTest");
//        Remote.push("eric","other-branch");
//    }
//
//    @Test
//    public void remoteFetchPush1() throws IOException {
//        Utils.createEmptyFile("phone.txt");
//        Stage.add("phone.txt");
//        Commit c2 = new Commit("version 1 wug", Commit.getCurrentSha1());
//        c2.write();
//
//        Remote.add("josh","C:\\Users\\evpin\\Desktop\\gitTest2");
//        Remote.push("josh","other-branch");
//
//        Remote.fetch("eric", "other-branch");
//        Checkout.overwriteBranch("eric/other-branch");
////
//        Utils.createEmptyFile("box.txt");
//        Stage.add("box.txt");
//        Commit c3 = new Commit("version 1 wug", Commit.getCurrentSha1());
//        c3.write();
//    }
//
//    @Test
//    public void logTest() {
//        Log.printLog();
//    }
//}
