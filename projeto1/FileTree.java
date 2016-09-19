public class FileTree {
    private File root;

    public void setRoot(File f) {
        this.root = f;
    }

    public File getFile(String filepath) {
        if (filepath.equals("/")) {
            return this.root;
        }
        //System.out.println(filepath.substr(1));
        String[] paths = filepath.substring(1).split("/");
        File current = this.root;
        for (String path : paths) {
            if ((current = current.getChild(path)) == null) {
                return null;
            }
        }
        return current;
    }

    public File addFile(String filepath, String data) {
        if (this.getFile(filepath) != null) {
            return null;
        }
        String[] paths = filepath.substring(1).split("/");
        String filename = paths[paths.length-1];
        System.out.println(filename);
        File current = this.root;
        for (int i = 0; i < paths.length-1; i++) {
            if ((current = current.getChild(paths[i])) == null) {
                return null;
            }
        }
        File f = new File(filename);
        f.addData(data);
        current.addChild(f);
        return f;
    }

    public boolean removeFile(String filepath) {
        if (this.getFile(filepath) == null) {
            return false;
        }
        String[] paths = filepath.substring(1).split("/");
        String filename = paths[paths.length-1];
        System.out.println(filename);
        File current = this.root;
        for (int i = 0; i < paths.length-1; i++) {
            if ((current = current.getChild(paths[i])) == null) {
                return false;
            }
        }
        return current.removeChild(filename);
    }
}
