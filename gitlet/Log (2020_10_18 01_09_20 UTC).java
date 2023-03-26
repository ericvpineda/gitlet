package gitlet;

import java.io.File;

/* Class for log and global-log command */
public class Log {

    // Note: issue with long list of commits??
    /**
     * Gitlet command for Log
     */
    public static void printLog() {
        Commit headCommit = Commit.getCurrent();
        while (headCommit != null) {
            System.out.println("===");
            System.out.println("commit " + headCommit._sha1);
            // Note: parent1 = branch did merge, parent2 = merged-in branch
            if (headCommit._mergedId != null) {
                System.out.println("Merge: " + headCommit._mergedId);
            }
            System.out.println(headCommit._time);
            System.out.print(headCommit._logMessage);
            headCommit = findPrevCommit(headCommit);
            if (headCommit != null) {
                System.out.println();
                System.out.println();
            }
        }
    }

    /**
     * Helper method to find previous commit
     */
    private static Commit findPrevCommit(Commit headCommit)  {
        if (headCommit._parentSha1 == null) {
            return null;
        }
        return Utils.deserializeCommit(headCommit._parentSha1);
    }

    /**
     * Global-log command
     */
    public static void printGlobal() {
        File[] commitList = Main.COMMITS.listFiles();
        for (int i = 0; i < commitList.length; i++) {
            for (File file : commitList[i].listFiles()) {
                Commit commit = Utils.deserialize(file,Commit.class);
                System.out.println("===");
                System.out.println("commit " + commit._sha1);
                // Note: parent1 = branch did merge, parent2 = merged-in branch
                if (commit._mergedId != null) {
                    System.out.println("Merge: " + commit._mergedId);
                }
                System.out.println(commit._time);
                System.out.print(commit._logMessage);
                if (i != commitList.length - 1) {
                    System.out.println();
                    System.out.println();
                }
            }
        }
    }
}
