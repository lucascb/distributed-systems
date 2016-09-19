import java.util.ArrayList;
import java.time.Instant;

public class File {
    private String name;
    private String data;
    private String creationTime;
    private String modificationTime;
    private int version;
    private ArrayList<File> child;

    public File(String name) {
        this.name = name;
        this.data = "";
        this.creationTime = String.valueOf(Instant.now().toEpochMilli());
        this.modificationTime = this.creationTime;
        this.version = 0;
        this.child = new ArrayList<File>();
    }

    public boolean addChild(File f) {
        return this.child.add(f);
    }

    public void addData(String s) {
        this.modificationTime = String.valueOf(Instant.now().toEpochMilli());
        this.version++;
        this.data += s;
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
