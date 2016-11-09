//import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileTree {
    private Map<String, File> ft;

    public FileTree() {
        this.ft = new ConcurrentHashMap<String, File>();
        File f = new File("/", "");
        this.ft.put("/", f);
    }

    public File getFile(String filepath) {
        return this.ft.get(filepath);
    }

    public File addFile(String filepath, String data) {
        if (this.ft.get(filepath) != null) {
            return null;
        }
        String[] paths = filepath.substring(1).split("/");
        String filename = paths[paths.length - 1];
        String currentPath = "";
        File currentFile = this.ft.get("/");
        for (int i = 0; i < paths.length - 1; i++) {
            currentPath = currentPath + "/" + paths[i];
            if ((currentFile = this.ft.get(currentPath)) == null) {
                return null;
            }
        }
        File f = new File(filename, data);
        this.ft.put(filepath, f);
        currentFile.addChild(f);
        f.setPredecessor(currentFile);
        return f;
    }

    public boolean removeFile(String filepath) {
        File f;
        if ((f = this.ft.get(filepath)) == null || f.hasChild()) {
            return false;
        }
        this.ft.remove(filepath);

        File predecessor = f.getPredecessor();

        String[] paths = filepath.substring(1).split("/");
        String filename = paths[paths.length - 1];

        return predecessor.removeChild(filename);
    }
}
