import java.util.ArrayList;
import java.io.IOException;
import org.apache.thrift.TException;

public abstract class Server {
	private static ArrayList<HTTPServer> servers = new ArrayList<HTTPServer>();
	private static int serverCont = 0;

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
            System.err.println("Wrong parameter, please type 'java Server port'");
            System.exit(-1);
        }
        HTTPServer s = new HTTPServer(Integer.parseInt(args[0]), serverCont++);
		servers.add(s, serverCont++);
	}

	public static void addServer(int port, int serverName) {
		try {
			servers.add(server);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static HTTPServer getServer(int serverName) {
		return servers.get(serverName);
	}

	public static String GET(String path)
	throws org.apache.thrift.TException {
		int hash = path.hashCode() % servers.size();

		for (HTTPServer server : servers) {
			String r = server.GET(path);

			if (r != null) {
				System.out.println("Dado encontrado no servidor " +
					server.getServerName()
				);
				return r;
			}
		}
		return null;
	}

    public static String LIST(String path) throws org.apache.thrift.TException {
    	for (HTTPServer server : servers) {
    		String r = server.LIST(path);
			if (r != null)
				return r;
		}
		return null;
    }

    public static boolean ADD(String path, String data)
	throws org.apache.thrift.TException {
    	int hash = (path.hashCode() % servers.size());
    	HTTPServer server = getServer(hash);

    	System.out.println("Dado inserido no servidor " + server.getServerName());

    	return server.ADD(path, data);
    }

    public static boolean UPDATE(String path, String data)
	throws org.apache.thrift.TException {
    	for (HTTPServer server : servers) {
    		boolean r = server.UPDATE(path, data);
			if (r) return true;
		}
		return false;
    }

    public static boolean DELETE(String path) throws org.apache.thrift.TException {
    	for (HTTPServer server : servers){
			boolean r = server.DELETE(path);
			if (r) return true;
		}
		return false;
    }

    public static boolean UPDATE_VERSION(String path, String data, int version) throws org.apache.thrift.TException{
    	for (HTTPServer server : servers){
			boolean r = server.UPDATE_VERSION(path, data, version);
			if (r) return true;
		}
		return false;
    }

    public static boolean DELETE_VERSION(String path, int version) throws
	org.apache.thrift.TException {
    	for (HTTPServer server : servers) {
			boolean r = server.DELETE_VERSION(path, version);
			if (r) return true;
		}
		return false;
    }

}
