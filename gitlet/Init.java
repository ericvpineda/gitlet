package gitlet;

import java.io.File;
import java.io.IOException;

/* Class for init command */
public class Init {

    /**
     * Initializes gitlet repo
     */
    static void initialize() throws IOException {
        if (Main.GITLET.exists()) {
            System.out.print("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        Main.GITLET.mkdir();
        initContents();
        Branch.save("master");
        Branch.savePointer("master", Main.HEAD);
        Commit initCommit = new Commit("initial commit", null);
        initCommit.write();
        Stage stage = new Stage();
        Stage.write(stage);
        Remote remote = new Remote();
        Remote.write(remote);
    }

    /**
     * Make associated files for repo
     */
    static void initContents() throws IOException {
        Main.OBJECTS.mkdir();
        Main.COMMITS.mkdir();
        Main.BLOB.mkdir();
        Main.TREE.mkdir();
        File helper = Utils.join(Main.GITLET, "refs");
        File heads = Utils.join(helper, "heads");
        helper.mkdir();
        heads.mkdir();

        Main.CONFIG.createNewFile();
        Main.INDEX.createNewFile();
        Main.HEAD.createNewFile();
    }
}
