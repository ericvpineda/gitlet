package gitlet;

import java.io.File;
import java.io.IOException;

// ------ Fix Issues -------
// - check merge, rebase class
// - don't forget style check
// - change all read/write object <- should not have extra bytes

/* Driver class for Gitlet, the tiny stupid version-control system.
   @Eric Pineda
*/
public class Main {
    static final File USERDIR = Utils.join(System.getProperty("user.dir"));
    static final File GITLET = Utils.join(USERDIR,".gitlet");
    static final File STAGE = Utils.join(GITLET,"index.txt");
    static final File OBJECTS = Utils.join(GITLET,"objects");
    static final File COMMITS = Utils.join(OBJECTS,"commits");
    static final File BLOB = Utils.join(OBJECTS,"blobs");
    static final File TREE = Utils.join(OBJECTS,"trees");
    static final File BRANCH = Utils.join(GITLET,"refs","heads");
    static final File HEAD = Utils.join(GITLET,"HEAD.txt");
    static final File CONFIG = Utils.join(GITLET,"config.txt");

    /* Usage: java gitlet.Main ARGS, where ARGS contains <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.print("Please enter a command.");
            return;
        } else if (!args[0].equals("init") && !GITLET.exists()) {
            System.out.print("Not in an initialized Gitlet directory");
            return;
        }
        String command = args[0];
        switch (command) {
            case "init":
                init(args);
                break;
            case "add":
                add(args);
                break;
            // Note: Project does not use '-m'**
            case "commit":
                commit(args);
                break;
            case "rm":
                remove(args);
                break;
            case "log":
                log(args);
                break;
            case "global-log":
                globalLog(args);
                break;
            case "find":
                find(args);
                break;
            case "status":
                status(args);
                break;
            case "checkout":
                checkOut(args);
                break;
            case "branch":
                branch(args);
                break;
            case "rm-branch":
                removeBranch(args);
                break;
            case "reset":
                reset(args);
                break;
            case "merge":
                merge(args);
                break;
            case "rebase":
                rebase(args);
                break;
            case "add-remote":
                addRemote(args);
                break;
            case "rm-remote":
                removeRemote(args);
                break;
            case "push":
                push(args);
                break;
            case "fetch":
                fetch(args);
                break;
            default:
                System.out.print("No command with that name exists.");
                break;
        }
    }

    /**
     * Init command
     */
    public static void init(String[] args) throws IOException {
        if (args.length != 1) {
            errorMessage();
            return;
        }
        Init.initialize();
    }

    /**
     * Add command
     */
    public static void add(String[] args) throws IOException {
        if (args.length != 2) {
            errorMessage();
            return;
        }
        Stage stage = new Stage();
        stage.add(args[1]);
    }

    /**
     * Commit command
     */
    public static void commit(String[] args) throws IOException {
        if (args.length == 1 || args.length == 2 && args[1].isBlank()) {
            System.out.print("Please enter a commit message.");
            return;
        } else if (args.length != 2) {
            errorMessage();
            return;
        }
        Commit commit = new Commit(args[1], Commit.getCurrentSha1());
        commit.write();
    }

    /**
     * Remove command
     */
    public static void remove(String[] fileName) throws IOException {
        if (fileName.length != 2) {
            errorMessage();
            return;
        }
        Stage stage = new Stage();
        stage.remove(fileName[1]);
    }

    /**
     * Log command
     */
    public static void log(String[] args) {
        if (args.length != 1) {
            errorMessage();
            return;
        }
        Log.printLog();
    }

    /**
     * Global log command
     */
    public static void globalLog(String[] args) {
        if (args.length != 1) {
            errorMessage();
            return;
        }
        Log.printGlobal();
    }

    /**
     * Find command
     */
    public static void find(String[] message) {
        if (message.length != 2) {
            errorMessage();
            return;
        }
        Find.find(message[1]);
    }

    /**
     * Status command
     */
    public static void status(String[] args) throws IOException {
        if (args.length != 1) {
            errorMessage();
            return;
        }
        Status.print();
    }

    /**
     * Checkout command
     */
    public static void checkOut(String[] args) throws IOException {
        if (args.length == 2) {
            Checkout.overwriteBranch(args[1]);
        } else if (args.length == 3 && args[1].equals("--")) {
            Checkout.overwriteFile(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            Checkout.overwriteCommit(args[3], args[1]);
        } else {
            errorMessage();
        }
    }

    /**
     * Branch command
     */
    public static void branch(String[] args) throws IOException {
        if (args.length == 2 && args[1].equals("-a")) {
            Branch.listBranches();
        } else if (args.length == 2) {
            Branch.save(args[1]);
        } else {
            errorMessage();
        }
    }

    /**
     * Remove branch command
     */
    public static void removeBranch(String[] args) {
        if (args.length == 2) {
            Branch.remove(args[1]);
        } else {
            errorMessage();
        }
    }

    /**
     * Reset command
     */
    public static void reset(String[] args) throws IOException {
        if (args.length == 2) {
            Checkout.reset(args[1]);
        } else {
            errorMessage();
        }
    }

    /**
     * Merge command
     */
    public static void merge(String[] args) throws IOException {
        if (args.length == 2) {
            Merge m = new Merge();
            m.apply(args[1]);
        } else {
            errorMessage();
        }
    }

    public static void rebase(String[] args) throws IOException {
        if (args.length == 2) {
            Rebase.apply(args[1]);
        } else {
            errorMessage();
        }
    }

    public static void addRemote(String[] args) throws IOException {
        if (args.length == 3) {
            Remote.add(args[1],args[2]);
        } else {
            errorMessage();
        }
    }

    public static void removeRemote(String[] args) throws IOException {
        if (args.length == 2) {
            Remote.remove(args[1]);
        } else {
            errorMessage();
        }
    }
    public static void push(String[] args) throws IOException {
        if (args.length == 3) {
            Remote.push(args[1],args[2]);
        } else {
            errorMessage();
        }
    }

    public static void fetch(String[] args) throws IOException {
        if (args.length == 3) {
            Remote.fetch(args[1],args[2]);
        } else {
            errorMessage();
        }
    }

    /**
     * Error message
     */
    public static void errorMessage() {
        System.out.println("Incorrect operands.");
    }

}
