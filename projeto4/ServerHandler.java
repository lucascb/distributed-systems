import org.apache.thrift.TException;

public class ServerHandler implements Server.Iface {
	
	@Override
	public String GET(String path) throws org.apache.thrift.TException{
        File f = getFile(path);
        if (f != null) {
            String header = "HTTP/1.1 200 OK\n" + "Verion: " + f.getVersion() + "\nCreation: " 
            + f.getCreationTime() + "\nModification: " +
                    f.getModificationTime() + "\nContent-length: " +
                        f.getData().length() + '\n';

            String retorno = header + f.getData()+"\n";        
            return retorno;
        }
        return null;
        
    }

	@Override
    public String LIST(String path) throws org.apache.thrift.TException{
        File file = getFile(path);

        ArrayList<File> child = file.getChildren();

        if(child != null){

            StringBuilder builder = new StringBuilder();

            for(File f : child){
                builder.append(f.getName()+"\n");
            }        
           
            return builder.toString();
        } else{

            return null;
        }

    }

	@Override
    public boolean ADD(String path, String data) throws org.apache.thrift.TException{
        if (addFile(path, data) == null) return false;
        else return true;


    }

	@Override
    public boolean UPDATE(String path, String data) throws org.apache.thrift.TException{
        File f = getFile(path);

        if (f != null) {
            f.addData(data);
            return true;

        } else return false;
    }

	@Override
    public boolean DELETE(String path) throws org.apache.thrift.TException{
        return removeFile(path);
    }

	@Override
    public boolean UPDATE_VERSION(String path, String data, int version) throws org.apache.thrift.TException{
        File f = getFile(path);

        if(f != null && f.getVersion() == version){
            f.addData(data);
            return true;

        } else return false;



    }

	@Override
    public boolean DELETE_VERSION(String path, int version) throws org.apache.thrift.TException{

        File f = getFile(path);

        if(f != null && f.getVersion() == version){
            return removeFile(path);

        } else return false;

    }	
	
	
}
