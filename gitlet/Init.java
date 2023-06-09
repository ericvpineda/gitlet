package gitlet;

import java.io.File;
import java.io.IOException;

/* Class for init command */
public class Init {

    /**
     * Initializes .gitlet repository
     */
    static void initialize() throws IOException {

        // Check duplicate .gitlet folder exists
        if (Main.GITLET.exists()) {
            System.out.print("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        // Create necessary folders (.gitlet, objects, commits, etc.)
        Main.GITLET.mkdir();
        Main.OBJECTS.mkdir();
        Main.COMMITS.mkdir();
        Main.BLOB.mkdir();
        Main.TREE.mkdir();
        File helper = Utils.join(Main.GITLET, "refs");
        File heads = Utils.join(helper, "heads");
        helper.mkdir();
        heads.mkdir();

        // Create metadata files
        Main.CONFIG.createNewFile();
        Main.STAGE.createNewFile();
        Main.HEAD.createNewFile();

        // Write empty stage to disk
        Stage.write(new Stage());

        // Create master branch
        Branch.write("master");
        Branch.writeHead("master", Main.HEAD);

        // Create and write initial commit
        Commit initCommit = new Commit("initial commit", null);
        initCommit.write();
    }
}
