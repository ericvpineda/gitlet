package gitlet;

import java.io.File;
import java.util.ArrayList;

/* Class for find command */
public class Find {

    // Question: have a commit folder to improve runtime?
    /**
     * Finds Commit with given message
     */
    static void find(String message) {
        ArrayList<String> commits = new ArrayList<>();
        File[] objectList = Main.COMMITS.listFiles();
        for (int i = 0; i < objectList.length; i++) {
            for (File inner : objectList[i].listFiles()) {
                Commit file = Utils.readObject(inner, Commit.class);
                if (file._logMessage.equals(message)) {
                    commits.add(file._sha1);
                }
            }
        }
        if (commits.isEmpty()) {
            System.out.print("Found no commit with that message.");
        } else {
            for (int i = 0; i < commits.size(); i++) {
                System.out.print(commits.get(i));
                if (i != commits.size() - 1) {
                    System.out.println();
                }
            }
        }
    }
}
