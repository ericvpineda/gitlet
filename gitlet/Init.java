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

        // Crate master branch
        Branch.write("master");
        Branch.writeHead("master", Main.HEAD);

        // Create initial commit
        Commit initCommit = new Commit("initial commit", null);

        // Note: Will create new stage
        initCommit.write();

        // Create remote branch [TODO-LATER]
//        Remote remote = new Remote();
//        Remote.write(remote);
    }
}
