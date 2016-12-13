import java.util.ArrayList;
import java.time.Instant;

public class File {
    private String name;
    private String data;
    private String creationTime;
    private String modificationTime;
    private int version;
    private ArrayList<File> child;
    private File predecessor;

    public File(String name, String data) {
        this.name = name;
        this.data = data;
        this.creationTime = String.valueOf(Instant.now().toEpochMilli());
        this.modificationTime = this.creationTime;
        this.version = 0;
        this.child = new ArrayList<File>();
        this.predecessor = null;
    }

    public boolean addChild(File f) {
        return this.child.add(f);
    }


    public void addData(String s) {
    	this.modificationTime = String.valueOf(Instant.now().toEpochMilli());
        this.version++;
        this.data = (s + '\n');
    }

    public void setPredecessor(File f) {
        this.predecessor = f;
    }

    public File getPredecessor() {
        return this.predecessor;
    }

    public String getName() {
        return this.name;
    }

    public String getData() {
        return this.data;
    }

    public String getCreationTime() {
        return this.creationTime;
    }

    public String getModificationTime() {
        return this.modificationTime;
    }

    public int getVersion() {
        return this.version;
    }

    public File getChild(String childname) {
        for (File f : this.child) {
            if (f.getName().equals(childname)) {
                return f;
            }
        }
        return null; // Nao encontrou
    }

    public ArrayList<File> getChildren(){
        return this.child;
    }

    public boolean hasChild() {
        return this.child.size() != 0;
    }

    public boolean removeChild(String childname) {
        for (File f : this.child) {
            if (f.getName().equals(childname)) {
                if (!f.hasChild()) {
                    this.child.remove(f);
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }
}
