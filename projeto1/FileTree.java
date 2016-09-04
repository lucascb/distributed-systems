public class FileTree {
    private File root;

    public FileTree(File root) {
        this.root = root;
    }

    public File getFile(String filename) {
        return this.root.getFile(filename);
    }
}
