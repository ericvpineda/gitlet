package gitlet;

import java.io.File;
import java.util.ArrayList;

/* Class for find command */
public class Find {

    // Question: have a commit folder to improve runtime?
    /**
     * Gitlet command to find Commit with certain message
     */
    static void find(String comMessage) {
        ArrayList<String> commits = new ArrayList<>();
        File[] objectList = Main.COMMITS.listFiles();
        for (int i = 0; i < objectList.length; i++) {
            for (File inner : objectList[i].listFiles()) {
                Commit comFile = (Commit) Utils.deserializeObject(inner);
                if (comFile._logMessage.equals(comMessage)) {
                    commits.add(comFile._sha1);
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
