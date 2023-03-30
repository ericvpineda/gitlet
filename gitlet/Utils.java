package gitlet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/** Assorted utilities.
 *  @author P. N. Hilfinger
 */
class Utils {

    /* SHA-1 HASH VALUES. */

    /** The length of a complete SHA-1 UID as a hexadecimal numeral. */
    static final int UID_LENGTH = 40;

    /** Returns the SHA-1 hash of the concatenation of VALS, which may
     *  be any mixture of byte arrays and Strings. */
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /** Returns the SHA-1 hash of the concatenation of the strings in
     *  VALS. */
    static String sha1(List<Object> vals) {
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    /* FILE DELETION */

    /** Deletes FILE if it exists and is not a directory.  Returns true
     *  if FILE was deleted, and false otherwise.  Refuses to delete FILE
     *  and throws IllegalArgumentException unless the directory designated by
     *  FILE also contains a directory named .gitlet. */
    static boolean restrictedDelete(File file) {
        if (!(new File(file.getParentFile(), ".gitlet")).isDirectory()) {
            throw new IllegalArgumentException("not .gitlet working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /** Deletes the file named FILE if it exists and is not a directory.
     *  Returns true if FILE was deleted, and false otherwise.  Refuses
     *  to delete FILE and throws IllegalArgumentException unless the
     *  directory designated by FILE also contains a directory named .gitlet. */
    static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    /* READING AND WRITING FILE CONTENTS */

    /** Return the entire contents of FILE as a byte array.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Return the entire contents of FILE as a String.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }

    /** Write the result of concatenating the bytes in CONTENTS to FILE,
     *  creating or overwriting it as needed.  Each object in CONTENTS may be
     *  either a String or a byte array.  Throws IllegalArgumentException
     *  in case of problems. */
    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw
                    new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Return an object of type T read from FILE, casting it to EXPECTEDCLASS.
     *  Throws IllegalArgumentException in case of problems. */
    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                 | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Write OBJ to FILE. */
    static void writeObject(File file, Serializable obj) {
        writeContents(file, serialize(obj));
    }

    /* DIRECTORIES */

    /** Filter out all but plain files. */
    private static final FilenameFilter PLAIN_FILES =
        new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        };

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    /* OTHER FILE UTILITIES */

//    /** Return the concatentation of FIRST and OTHERS into a File designator,
//     *  analogous to the {@link java.nio.file.Paths.#\get(String, String[])}
//     *  method. */
    static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

//    /** Return the concatentation of FIRST and OTHERS into a File designator,
//     *  analogous to the {@link java.nio.file.Paths.#get(String, String[])}
//     *  method. */
    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }


    /* SERIALIZATION UTILITIES */

    /** Returns a byte array containing the serialized contents of OBJ. */
    static byte[] serialize(Serializable obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw error("Internal error serializing commit.");
        }
    }



    /* MESSAGES AND ERROR REPORTING */

    /** Return a GitletException whose message is composed from MSG and ARGS as
     *  for the String.format method. */
    static GitletException error(String msg, Object... args) {
        return new GitletException(String.format(msg, args));
    }

    /** Print a message composed from MSG and ARGS as for the String.format
     *  method, followed by a newline. */
    static void message(String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }

    /**-------------Personal Methods------------------- */

    /** Create time stamp for commits */
    public static String createTime() {
        Calendar cal = Calendar.getInstance();
        Formatter f = new Formatter();
        f.format("%ta %th %td %tT %tY %tz",cal,cal,cal,cal,cal,cal);
        return "Date: " + f.toString();
    }

    static <Object extends Serializable> Object deserialize(File file,
                                                            Class<Object> expClass) {
        Object outputObj = null;
        try {
            ObjectInputStream inp = new ObjectInputStream((new FileInputStream(file)));
            outputObj = expClass.cast(inp.readObject());
            inp.close();
        } catch (IOException | ClassNotFoundException e) {
        }
        return outputObj;
    }

    /* Private method that returns a File object*/
    // Note: can look up files up to 5 characters and above (commits)
    static File findFile(String name, File objectsFile) {
        // Note: check commit with fewer than 40 characters
        if (objectsFile.equals(Main.COMMITS) && name.length() < 40) {
            for (File folder : Main.COMMITS.listFiles()) {
                // Note: check if folder contains first 2 charac
                if (folder.getName().equals(name.substring(0,2))) {
                    for (File innerFile : folder.listFiles()) {
                        int len = name.substring(2).length();
                        // Note: if innerfile name until equal to name
                        if (innerFile.getName().substring(0, len).equals(name.substring(2))) {
                            return innerFile;
                        }
                    }
                }
            }
        }
        if (objectsFile.equals(Main.COMMITS) || objectsFile.equals(Main.BLOB) ||
            objectsFile.equals(Main.TREE)) {
            return Utils.join(objectsFile, name.substring(0, 2), name.substring(2));
        }
        return Utils.join(objectsFile, name);
    }

    /** Custom method to deserialize commit */
    static Commit deserializeCommit(String SHA1) {
        if (SHA1 != null) {
            File foundFile = findFile(SHA1, Main.COMMITS);
            if (foundFile.exists()) {
                return deserialize(foundFile, Commit.class);
            }
        }
        return null;
    }

    /* Find file in CWD */
    static boolean findInCwd(String fileName) {
        return join(Main.USERDIR,fileName).exists();
    }

    static Object deserializeObject(File file) {
        Object outputObj = null;
        try {
            ObjectInputStream inp = new ObjectInputStream((new FileInputStream(file)));
            outputObj = inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException e) {
        }
        return outputObj;
    }


    /* Find find in CWD and create sha1 for it */
    static String findFileCWDsha1(String fileName) {
        if (findInCwd(fileName)) {
            File file = join(Main.USERDIR,fileName);
            Blob blob = new Blob(file);
            return blob._sha1;
        }
        return null;
    }

    public static void createRandomFile(String name) throws IOException {
        File random = Utils.join(Main.USERDIR,name);
        random.createNewFile();
    }

    /* Randomly change inner contents of file */
    static void randomChangeFileContents(String object) throws IOException {
        File file = Utils.join(Main.USERDIR,object);
        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        Random rand = new Random();
        bw.write("Random change: " + rand.nextInt(100));
        bw.close();
    }

    static void sameChangeFileContents(String object) throws IOException {
        File file = Utils.join(Main.USERDIR,object);
        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write("This is the same file.");
        bw.close();
    }

    static void testFileMergeConflict(String object) throws IOException {
        File file = Utils.join(Main.USERDIR,object);
        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        Random rand = new Random();
        bw.write("This is the same file.");
        bw.write(System.getProperty("line.separator"));
        bw.write("Random change: " + rand.nextInt(100));
        bw.write(System.getProperty("line.separator"));
        bw.write("Here are some other changes: ");
        bw.write(System.getProperty("line.separator"));
        bw.write("Change 1");
        bw.write(System.getProperty("line.separator"));
        bw.write("Change 2");
        bw.write(System.getProperty("line.separator"));
        bw.write("Change 3");

        bw.close();
    }

    public static ArrayList<String> getCommitArray(Commit commit,ArrayList<String> array) throws IOException {
        if (commit == null) {
            return null;
        } else if (commit._parentSha1 == null) {
            array.add(commit._sha1);
            return array;
        }
        array.add(commit._sha1);
        Commit next = Utils.deserializeCommit(commit._parentSha1);
        return getCommitArray(next,array);
    }

    // Note:
    // - concatenate as string and do .getByte()
    // - remove extra '\n'

    /* Method to replace contents of a file with conflict message */
    public static void createConflictFile(String fileName, String current, String target) {
        String header = "<<<<<<< HEAD\n";
        String middle = "=======\n";
        String end = ">>>>>>>\n";
        String curr = "";
        String tar = "";
        if (current != null) {
            Blob currBlob = deserialize(findFile(current, Main.BLOB), Blob.class);
            curr = currBlob._fileContent;
        }
        if (target != null) {
            Blob tarBlob = deserialize(findFile(target, Main.BLOB), Blob.class);
            tar = tarBlob._fileContent;
        }
        byte[] conflict = (header + curr + middle + tar + end).getBytes();
        Utils.writeContents(Utils.join(Main.USERDIR, fileName), conflict);
    }

    /** Helper method to check if working file is untracked in current branch */
    public static boolean checkUntrackedCwd() {
        ArrayList<String> ignoreFiles = getIgnoreArray();
        ArrayList<String> firstCommitFiles = Utils.getBlobFolderArray();
        File[] objectList = Main.USERDIR.listFiles();

        for (File file : objectList) {
            String fileName = file.getName();
            if (ignoreFiles.contains(fileName)) {
                continue;
            }
            byte[] fileByte = Utils.readContents(file);
            String fileSHA1 = Utils.sha1(fileByte, file.getName());
            // Note: checks if file is in blob folder (tracked)
            if (firstCommitFiles == null || !firstCommitFiles.contains(fileSHA1)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> getTotalSha1History(Commit commit, ArrayList<String> arr) {
        if (commit == null) {
            return arr;
        } else if (!arr.contains(commit._sha1)) {
            arr.add(commit._sha1);
        }
        if (commit._mergedId != null) {
            String merge = commit._mergedId;
            File c1 = Utils.findFile(merge.substring(0,7), Main.COMMITS);
            File c2 = Utils.findFile(merge.substring(8,15), Main.COMMITS);
            getTotalSha1History(Utils.deserialize(c1, Commit.class), arr);
            getTotalSha1History(Utils.deserialize(c2, Commit.class), arr);
        }
        getTotalSha1History(Utils.deserializeCommit(commit._parentSha1), arr);
        return arr;
    }

    public static void clearCwdRemote(File remoteDir) {
        ArrayList<String> ignoreFiles = getIgnoreArray();
        File[] objectList = remoteDir.listFiles();

        for (File file : objectList) {
            String fileName = file.getName();
            if (ignoreFiles.contains(fileName)) {
                continue;
            }
            file.delete();
            // Note: checks if file is in blob folder (tracked)
        }
    }

    public static ArrayList<String> getBlobFolderArray() {
        ArrayList<String> blobFiles = new ArrayList<>();
        File[] files = Main.BLOB.listFiles();
        if (files.length > 0) {
            for (File folder : files) {
                for (File innerFile : folder.listFiles()) {
                    Blob blob = Utils.deserialize(innerFile, Blob.class);
                    blobFiles.add(blob._sha1);
                }
            }
            return blobFiles;
        }
        return null;
    }

    public static ArrayList<String> getIgnoreArray() {
        ArrayList<String> ignoreFiles = new ArrayList<>();
        ignoreFiles.add(".gitlet");
        ignoreFiles.add(".gitignore");
        ignoreFiles.add("gitlet");
        ignoreFiles.add(".idea");
        ignoreFiles.add("testing");
        ignoreFiles.add("proj2.iml");
        ignoreFiles.add("out");
        ignoreFiles.add(".git");
        return ignoreFiles;
    }

    /** Replace files in cwd (branch) */
    public static void replaceCwdFiles(HashMap<String,String> hm) throws IOException {
        Utils.clearCwd();
        Iterator iter = hm.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            String name = (String) obj.getKey();
            String sha1 = (String) obj.getValue();
            Utils.overwriteHelper(name, sha1);
        }
    }

    /** Helper method to overwrite file based on sha1 */
    public static void overwriteHelper(String fileName, String sha1) throws IOException {
        if (findInCwd(fileName)) {
            // Find object in Main.Objects
            File blobFile = Utils.findFile(sha1, Main.BLOB);
            Blob blob = Utils.deserialize(blobFile, Blob.class);
            Utils.writeContents(join(Main.USERDIR,fileName), blob._fileContent);
        } else {
            // Note: checkout command doesn't use this functionality?
            Utils.uploadFileCWD(sha1);
            Stage.restore(fileName);
        }
    }

    /** Uploads file into CWD based on SHA1 */
    public static void uploadFileCWD(String fileSHA1) throws IOException {
        File blobFile = findFile(fileSHA1, Main.BLOB);
        Blob blob = deserialize(blobFile,Blob.class);
        File innerFile = join(Main.USERDIR,blob._name);
        innerFile.createNewFile();
        Utils.writeContents(innerFile,blob._fileContent);
    }


    public static void clearCwd() {
        ArrayList<String> ignoreFiles = getIgnoreArray();
        File[] objectList = Main.USERDIR.listFiles();

        for (File file : objectList) {
            String fileName = file.getName();
            if (ignoreFiles.contains(fileName)) {
                continue;
            }
            file.delete();
            // Note: checks if file is in blob folder (tracked)
        }
    }

    public static void deleteDirectory(File file) {
        for (File subfile : file.listFiles()) {
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }
            subfile.delete();
        }
    }

    public static void clearCwdWithGitlet() throws IOException {
        ArrayList<String> ignoreFiles = getIgnoreArray();
        ignoreFiles.remove(0);
        File[] objectList = Main.USERDIR.listFiles();

        for (File file : objectList) {
            String fileName = file.getName();
            if (ignoreFiles.contains(fileName)) {
                continue;
            }
            file.delete();
            // Note: checks if file is in blob folder (tracked)
        }

        if (Main.GITLET.exists()) {
            File gitlet = Utils.join(Main.GITLET);
            deleteDirectory(gitlet);
            gitlet.delete();
        }
    }

    public static void clearCwdWithGitletRemote(File remote) throws IOException {
        ArrayList<String> ignoreFiles = getIgnoreArray();
        ignoreFiles.remove(0);
        File[] objectList = remote.listFiles();

        for (File file : objectList) {
            String fileName = file.getName();
            if (ignoreFiles.contains(fileName)) {
                continue;
            }
            file.delete();
            // Note: checks if file is in blob folder (tracked)
        }
        if (remote.exists()) {
            File file = Utils.join(remote, ".gitlet");
            Files.walk(Paths.get(file.getAbsolutePath()))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    public static void remoteInitialize(File remote) throws IOException {
        File INDEX = Utils.join(remote,"index.txt");
        File OBJECTS = Utils.join(remote,"objects");
        File COMMITS = Utils.join(OBJECTS,"commits");
        File BLOB = Utils.join(OBJECTS,"blobs");
        File TREE = Utils.join(OBJECTS,"trees");
        File HEAD = Utils.join(remote,"HEAD.txt");
        File CONFIG = Utils.join(remote,"config.txt");
        remote.mkdir();
        OBJECTS.mkdir();
        COMMITS.mkdir();
        BLOB.mkdir();
        TREE.mkdir();

        File helper = Utils.join(remote, "refs");
        File heads = Utils.join(helper, "heads");
        helper.mkdir();
        heads.mkdir();
        CONFIG.createNewFile();
        INDEX.createNewFile();
        HEAD.createNewFile();

        File remoteDir = Utils.join(remote, ".gitlet");
        Commit remoteC = new Commit("initial commit", null, remoteDir);
        remoteC.writeRemote(remoteDir);
        Branch.saveRemote("master", Utils.join(remoteDir, "refs", "heads"), Commit.zeroSha1);
        Branch.savePointer("master", Utils.join(remoteDir, "HEAD.txt"));
    }

    public static void writeRemoteBlobs(String treeSha1, File remoteDir) throws IOException {
        File blobFile = Utils.join(remoteDir, "objects", "blobs");
        ArrayList<String> blobFolder = Utils.getBlobFolderArrayRemote(blobFile);
        HashMap<String, String> blobs = Commit.getBlobs(treeSha1);
        Iterator iter = blobs.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            if (!blobFolder.contains(obj.getValue())) {
                File currBlob = Utils.findFile((String) obj.getValue(), Main.BLOB);
                Blob blob = Utils.deserialize(currBlob, Blob.class);
                writeRemote(blob._sha1, blob, blobFile);
            }
        }
    }

    public static void writeRemote(String sha1, Object contents, File location) throws IOException {
        File innerFile = Utils.join(location,sha1.substring(0,2));
        innerFile.mkdir();
        File loc = Utils.join(location,sha1.substring(0,2),sha1.substring(2));
        loc.createNewFile();
        Utils.writeObject(loc, (Serializable) contents);
    }

    public static boolean checkUntrackedCwdRemote(File remote) {
        ArrayList<String> ignoreFiles = getIgnoreArray();
        File blobFolder = Utils.join(remote,".gitlet", "objects", "blobs");
        ArrayList<String> firstCommitFiles = Utils.getBlobFolderArrayRemote(blobFolder);
        File[] objectList = remote.listFiles();

        for (File file : objectList) {
            String fileName = file.getName();
            if (ignoreFiles.contains(fileName)) {
                continue;
            }
            byte[] fileByte = Utils.readContents(file);
            String fileSHA1 = Utils.sha1(fileByte, file.getName());
            // Note: checks if file is in blob folder (tracked)
            if (firstCommitFiles == null || !firstCommitFiles.contains(fileSHA1)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> getBlobFolderArrayRemote(File remoteBlobs) {
        ArrayList<String> blobFiles = new ArrayList<>();

        File[] files = remoteBlobs.listFiles();
        if (files != null && files.length > 0) {
            for (File folder : files) {
                for (File innerFile : folder.listFiles()) {
                    Blob blob = Utils.deserialize(innerFile, Blob.class);
                    blobFiles.add(blob._sha1);
                }
            }
            return blobFiles;
        }
        return new ArrayList<>();
    }

    public static ArrayList<Commit> getCommitHistoryRemote(File remote, Commit commit, ArrayList<Commit> arr) {
        if (commit == null) {
            return arr;
        } else if (commit._parentSha1 == null) {
            arr.add(commit);
            return arr;
        } else if (!arr.contains(commit._sha1)) {
            arr.add(commit);
        }
        File next = Utils.join(remote, "objects","commits",
                commit._parentSha1.substring(0,2), commit._parentSha1.substring(2));
//        if (commit._mergedId != null) {
//            String merge = commit._mergedId;
//            File c1 = Utils.findFile(merge.substring(0,7), Main.COMMITS);
//            Commit c1Commit = Utils.deserialize(c1, Commit.class);
//            File c2 = Utils.findFile(merge.substring(9,15), Main.COMMITS);
//            Commit c2Commit = Utils.deserialize(c2, Commit.class);
//            getCommitHistoryRemote(remote, c1Commit, arr);
//            getCommitHistoryRemote(remote, c2Commit, arr);
//        }
        commit = Utils.deserialize(next, Commit.class);
        getCommitHistoryRemote(remote, commit, arr);
        return arr;
    }

    public static void replaceCwdFilesRemote(HashMap<String,String> hm, File remoteDir) throws IOException {
        Utils.clearCwdRemote(remoteDir);
        Iterator iter = hm.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            String name = (String) obj.getKey();
            String sha1 = (String) obj.getValue();
            Utils.overwriteRemote(name, sha1, remoteDir);
        }
    }

    public static void writeBlobsToDisk(File remoteDir, String treeSha1) throws IOException {
        File treeLoc = Utils.join(remoteDir, "objects", "trees", treeSha1.substring(0,2), treeSha1.substring(2));
        HashMap<String, String> treeHm = Utils.deserialize(treeLoc, Tree.class)._blobList;
        Iterator iter = treeHm.entrySet().iterator();
        for (Iterator it = iter; it.hasNext(); ) {
            Map.Entry obj = (Map.Entry) it.next();
            String sha1 = (String) obj.getValue();
            File blobFile = Utils.join(Main.BLOB, "objects", "blobs", sha1.substring(0,2), sha1.substring(2));
            if (!blobFile.exists()) {
                Blob newBlob = Utils.deserialize(blobFile, Blob.class);
                newBlob.write();
            }
        }
    }

    public static void overwriteRemote(String fileName, String sha1, File remoteDir) throws IOException {
        File blobFile = Utils.findFile(sha1, Utils.join(Main.BLOB, sha1.substring(0,2), sha1.substring(2)));
        Blob blob = Utils.deserialize(blobFile, Blob.class);
        File cwdFile = join(remoteDir,fileName);
        if (Utils.join(remoteDir, fileName).exists()) {
            Utils.writeContents(cwdFile, blob._fileContent);
        } else {
            // Note: checkout command doesn't use this functionality?
            cwdFile.createNewFile();
            if (blob != null && blob._fileContent != null) {
                Utils.writeContents(cwdFile, blob._fileContent);
            }
        }
    }

    // Note: do I need this?
    public static void remoteReset(String commit, File remoteDir) throws IOException {
        if (Utils.checkUntrackedCwdRemote(remoteDir)) {
            System.out.print("There is an untracked file in the way; " +
                    "delete it, or add and commit it first.");
            return;
        }
        Commit givenCom = Utils.deserializeCommit(commit);
        HashMap<String, String> givenBlobList = Commit.getBlobs(givenCom._tree);
        Utils.replaceCwdFilesRemote(givenBlobList, remoteDir);
    }
}