package gitlet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    /** Files to ignore when updating .gitlet directory */
    static final ArrayList<String> IGNORE_FILES = new ArrayList<>(Arrays.asList(
            ".gitlet",
            ".gitignore",
            "gitlet",
            ".idea",
            "testing",
            "proj2.iml",
            "out",
            ".git",
            "readme.md",
            "notes.md"
    ));

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
    static <T extends Serializable> T readObject(File file, Class<T> expectedClass) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Write single object to FILE. */
    static void writeObject(File file, Serializable object) {
        writeContents(file, (Object) serialize(object));
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
            stream.close();
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

    /** Create a file path for gitlet files or folders */
    // Note: can look up files up to 5 characters and above (commits)
    static File createFilePath(String name, File file) {

        // Return file path
        File path = null;

        // Check if file is Commit object with short hash
        if (file.equals(Main.COMMITS) && name.length() < 40) {
            for (File folder : Main.COMMITS.listFiles()) {
                // Note: check if folder contains first 2 characters
                if (folder.getName().equals(name.substring(0,2))) {
                    for (File innerFile : folder.listFiles()) {
                        int len = name.substring(2).length();
                        // Note: if innerfile name until equal to name
                        if (innerFile.getName().substring(0, len).equals(name.substring(2))) {
                            path = innerFile;
                            break;
                        }
                    }
                }
            }
        }

        // Return key file path for commits, blobs or tree objects
        List<File> keyFiles = Arrays.asList(Main.COMMITS, Main.BLOB, Main.TREE);
        if (keyFiles.contains(file)) {
            File tempPath = Utils.join(file, name.substring(0, 2), name.substring(2));
            if (tempPath.exists()) {
                path = tempPath;
            }
        }

        // Return non-key file paths
        File tempPath = Utils.join(file, name);
        if (tempPath.exists()) {
            path = tempPath;
        }
        return path;
    }

    /** Check if file exists in current working directory */
    static boolean existsInCWD(String fileName) {
        return join(Main.USERDIR,fileName).exists();
    }

    /* Find find in CWD and create sha1 for it */
    static String findFileCWDsha1(String fileName) {
        if (existsInCWD(fileName)) {
            File file = join(Main.USERDIR,fileName);
            Blob blob = new Blob(file);
            return blob._sha1;
        }
        return null;
    }

    public static void createEmptyFile(String name) throws IOException {
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
        fos.close();
    }

    public static ArrayList<String> getCommitArray(Commit commit,ArrayList<String> array) throws IOException {
        if (commit == null) {
            return null;
        } else if (commit._parentSha1 == null) {
            array.add(commit._sha1);
            return array;
        }
        array.add(commit._sha1);
        Commit next = Commit.getByID(commit._parentSha1);
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
            Blob currBlob = readObject(createFilePath(current, Main.BLOB), Blob.class);
            curr = currBlob._fileContent;
        }
        if (target != null) {
            Blob tarBlob = readObject(createFilePath(target, Main.BLOB), Blob.class);
            tar = tarBlob._fileContent;
        }
        byte[] conflict = (header + curr + middle + tar + end).getBytes();
        Utils.writeContents(Utils.join(Main.USERDIR, fileName), conflict);
    }

    /** Helper method to check if working file is untracked in current branch */
    public static boolean checkUntrackedCwd() {
        ArrayList<String> firstCommitFiles = Utils.getBlobFolderArray();
        File[] objectList = Main.USERDIR.listFiles();

        for (File file : objectList) {
            String fileName = file.getName();
            if (IGNORE_FILES.contains(fileName)) {
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
            File c1 = Utils.createFilePath(merge.substring(0,7), Main.COMMITS);
            File c2 = Utils.createFilePath(merge.substring(8,15), Main.COMMITS);
            getTotalSha1History(Utils.readObject(c1, Commit.class), arr);
            getTotalSha1History(Utils.readObject(c2, Commit.class), arr);
        }
        getTotalSha1History(Commit.getByID(commit._parentSha1), arr);
        return arr;
    }

    public static ArrayList<String> getBlobFolderArray() {
        ArrayList<String> blobFiles = new ArrayList<>();
        File[] files = Main.BLOB.listFiles();
        if (files.length > 0) {
            for (File folder : files) {
                for (File innerFile : folder.listFiles()) {
                    Blob blob = Utils.readObject(innerFile, Blob.class);
                    blobFiles.add(blob._sha1);
                }
            }
            return blobFiles;
        }
        return null;
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
        if (existsInCWD(fileName)) {
            // Find object in Main.Objects
            File blobFile = Utils.createFilePath(sha1, Main.BLOB);
            Blob blob = Utils.readObject(blobFile, Blob.class);
            Utils.writeContents(join(Main.USERDIR,fileName), blob._fileContent);
        } else {
            // Note: checkout command doesn't use this functionality?
            Utils.uploadFileCWD(sha1);
            Stage.restore(fileName);
        }
    }

    /** Uploads file into CWD based on SHA1 */
    public static void uploadFileCWD(String fileSHA1) throws IOException {
        File blobFile = createFilePath(fileSHA1, Main.BLOB);
        Blob blob = readObject(blobFile,Blob.class);
        File innerFile = join(Main.USERDIR,blob._name);
        innerFile.createNewFile();
        Utils.writeContents(innerFile,blob._fileContent);
    }


    /** Helper function to clear current working directory */
    public static void clearCwd() {
        File[] objectList = Main.USERDIR.listFiles();

        for (File file : objectList) {
            String fileName = file.getName();
            if (IGNORE_FILES.contains(fileName)) {
                continue;
            }
            file.delete();
            // Note: checks if file is in blob folder (tracked)
        }
    }

    /** Helper function for clearCwdWithGitlet */
    public static void deleteDirectory(File file) {
        for (File subfile : file.listFiles()) {
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }
            boolean isDeleted = subfile.delete();
            if (!isDeleted) {
                System.out.println("DEBUG: Cannot delete file=" + subfile.toString());
            }
        }
    }

    /** Clear current working directory along with gitlet repository */
    public static void clearCwdWithGitlet() {
        File[] userDirectory = Main.USERDIR.listFiles();

        if (userDirectory != null) {
            for (File file : userDirectory) {
                String fileName = file.getName();
                if (IGNORE_FILES.contains(fileName) && !fileName.equals(".gitlet")) {
                    continue;
                }
                if (file.isDirectory()) {
                    deleteDirectory(file);
                }
                boolean isDeleted = file.delete();
                if (!isDeleted) {
                    System.out.println("DEBUG: Unable to delete file=" + file.getName());
                }
            }
        }
    }
}