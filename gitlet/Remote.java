package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

public class Remote implements Serializable {
    HashMap<String, String> _config;

    public Remote() {
        _config = new HashMap<>();
    }

    public static void write(Remote remote) {
        Utils.writeObject(Main.CONFIG, remote);
    }

    public static Remote read() {
        return Utils.readObject(Main.CONFIG, Remote.class);
    }


    public static void add(String name, String dr) throws IOException {
        Remote download = Remote.read();
        if (download._config.containsKey(name)) {
            System.out.println("A remote with that name already exists.");
            return;
        }
        String direct =  dr.replace("[/\\\\]", Matcher.quoteReplacement(System.getProperty("file.separator")));
        // Note: create gitlet repo
        File remote = new File(direct);
        Utils.remoteInitialize(remote);
        download._config.put(name,dr);
        write(download);
    }

    public static void remove(String name) throws IOException {
        Remote remote = Utils.readObject(Main.CONFIG, Remote.class);
        if (remote == null || !remote._config.containsKey(name)) {
            System.out.println("A remote with that name does not exist.");
            return;
        }
        File remoteFile = Utils.join(remote._config.get(name));
        Utils.clearCwdWithGitletRemote(remoteFile);
        remote._config.remove(name);
        write(remote);
    }

    public static void push(String name, String branch) throws IOException {
        Remote remote = Remote.read();
        if (remote == null || !remote._config.containsKey(name) && !Utils.join(remote._config.get(name)).exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        File remoteDir = Utils.join(remote._config.get(name));
        File rGitlet = Utils.join(remoteDir, ".gitlet");
        File rBranch = Utils.join(rGitlet,"refs","heads", branch);

        String branchSha1;
        String currSha1 = Commit.getCurrentID();
        if (!rBranch.exists()) {
            rBranch.createNewFile();
            branchSha1 = Commit.zeroSha1;
        } else {
            branchSha1 = Utils.readContentsAsString(rBranch);
        }

        // Note: get the head branch
        ArrayList<String> current = Utils.getCommitArray(Commit.getCurrent(), new ArrayList<>());
        int missing = getPos(branchSha1, current);
        if (missing < 0) {
            System.out.println("Please pull down remote changes before pushing.");
            return;
        }
        for (int i = missing; i >= 0; i--) {
            Commit copy = Utils.deserializeCommit(current.get(missing));
            // Note: write blobs to remote
            Utils.writeRemoteBlobs(copy._tree, rGitlet);
            // Note: write commit to remote
            Commit rCommit = new Commit(copy, branchSha1);
            rCommit.writeRemote(rGitlet);
            missing--;
            if (missing < 0) {
                break;
            }
            branchSha1 = current.get(missing);
        }
        // Note: reset to head commit
        Utils.remoteReset(currSha1, remoteDir);
        Utils.writeObject(rBranch, currSha1);
    }

    private static int getPos(String branch, ArrayList<String> current) {
        int start = current.size() - 1;
        while (!branch.equals(current.get(start))) {
            start--;
            if (start < 0) {
                return start;
            }
        }
        return start - 1;
    }


    public static void fetch(String name, String branch) throws IOException {
        Remote remote = Remote.read();
        if (remote == null || !remote._config.containsKey(name) &&
                !Utils.join(remote._config.get(name)).exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        File rDir = Utils.join(remote._config.get(name));
        File rGitlet = Utils.join(rDir, ".gitlet");
        File remoteBranchFile = Utils.join(rGitlet,"refs","heads", branch);
        File localBranch = Utils.join(Main.BRANCH, name + "_" + branch);
        if (!remoteBranchFile.exists()) {
            System.out.println("That remote does not have that branch.");
            return;
        }
        String headSha1 = Utils.readContentsAsString(remoteBranchFile);
        if (!localBranch.exists()) {
            localBranch.createNewFile();
        }
        // Note: get head commit file
        File commitFile = Utils.join(rGitlet, "objects","commits", headSha1.substring(0,2), headSha1.substring(2));
        Commit commit = Utils.readObject(commitFile, Commit.class);
        // Note: get array of all commits
        ArrayList<Commit> remoteCommits = Utils.getCommitHistoryRemote(rGitlet, commit, new ArrayList<>());
        for (int i = remoteCommits.size() - 1; i > 0; i--) {
            Commit c = remoteCommits.get(i);
            File curr = Utils.join(Main.COMMITS, c._sha1.substring(0,2), c._sha1.substring(2));
            if (!curr.exists()) {
                Utils.writeRemote(c._sha1, c._tree, Main.COMMITS);
                File treeLoc = Utils.join(rGitlet, "objects", "trees", c._tree.substring(0,2), c._tree.substring(2));
                Tree tree = Utils.readObject(treeLoc, Tree.class);
                Utils.writeRemote(c._tree, tree, Main.TREE);
                Utils.writeBlobsToDisk(rGitlet, c._tree);
            }
        }
        Utils.writeObject(localBranch, headSha1);
    }


    public static void pull(String name, String branch) {

    }

}
