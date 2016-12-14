//import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class FileTree {
    private Map<String, File> ft;
    private Map<String, File> commitedFt;

    public FileTree() {
        this.ft = new ConcurrentHashMap<String, File>();
        this.commitedFt = new ConcurrentHashMap<String, File>();
        File f = new File("/", "");
        this.ft.put("/", f);
    }

    public File getFile(String filepath) {
        return this.commitedFt.get(filepath);
    }

    public File addFile(String filepath, String data) {
        if (this.ft.get(filepath) != null) {
            return null;
        }
        File f = new File(filepath, data);
        this.ft.put(filepath, f);
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

    public boolean canCommit() {
        if(ft.equals(commitedFt)){
            return true;
        } else {
            System.out.println("Gostaria de commitar?");
            Scanner s = new Scanner(System.in);
            String entrada = s.nextLine();
            if (entrada.equals("s")) return true;
            else return false;
        }
    }

    public void commit(){
        commitedFt = new ConcurrentHashMap<String, File>(ft);
    }

    public void abort(){
        ft = new ConcurrentHashMap<String, File>(commitedFt);
    }
}
