import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashMap;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class ServerThrift implements Server.Iface {
    private int serverName;
    private int port;
    private HTTPServer nextServer, previousServer;
    private ServerSocket socket;
    private FileTree ft = new FileTree();
    private boolean isOnline = true;
    private Server.Processor processor;
    private HashMap<Integer, Integer> servers = new HashMap<Integer, Integer>();
    private int numServers = 0;

    public static void main(String[] args) throws IOException {
        new ServerThrift(Integer.parseInt(args[0]));
        
    }


    public void loadFile(String filename) {
        try {
            BufferedReader reader = new BufferedReader(
                new FileReader(filename)
            );
            String line = reader.readLine();

            while (line != null) {
                String[] s = line.split(" ");
                int n = Integer.parseInt(s[0]);
                int p = Integer.parseInt(s[1]);
                this.servers.put(n, p);

                // Read next line for while condition
                line = reader.readLine();
                numServers++;
            }
            reader.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public ServerThrift(int serverName) throws IOException {
        loadFile("servers.txt"); 
        this.port = servers.get(serverName);
        this.serverName = serverName;

        System.out.println(""+port);

        processor = new Server.Processor(this);
        Runnable run = new Runnable() {
            public void run() {
                try {
                    TServerTransport serverTransport = new TServerSocket(port);
                    TServer server = new TSimpleServer(
                        new Args(serverTransport).processor(processor)
                    );
                    System.out.println("Starting server " + serverName + "...");
                    server.serve();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(run).start();
    }

    public void CREATE(int port) { }

    public File getFile(String filepath) {
        return this.ft.getFile(filepath);
    }

    public File addFile(String filepath, String data) {
        return this.ft.addFile(filepath, data);
    }

    public boolean removeFile(String filepath) {
        return this.ft.removeFile(filepath);
    }

    public void shutdown() {
        this.isOnline = false;
    }

    /*
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Wrong parameter, please type 'java HTTPServer port'");
            System.exit(-1);
        }
        new HTTPServer(Integer.parseInt(args[0]), 0);
    }
    */

    public String GET(String path) throws org.apache.thrift.TException{
        File f = getFile(path);
        String retorno = null;

        if (f != null) {
            System.out.println("GET "+ path +" accepted on server " + this.serverName);
            String header = "HTTP/1.1 200 OK\n" + "Version: " + f.getVersion() + "\nCreation: "
            + f.getCreationTime() + "\nModification: " +
                    f.getModificationTime() + "\nContent-length: " +
                        f.getData().length() + '\n';

            retorno = header + f.getData()+"\n";
        }
        else {
            int hash = Math.abs(path.hashCode()) % this.numServers;   
            System.out.println("GET SENDED TO "+ hash+" with path: "+path);
            

            try {
                TTransport transport;

                transport = new TSocket("127.0.0.1", servers.get(hash));
                transport.open();

                TProtocol protocol = new TBinaryProtocol(transport);
                Server.Client client = new Server.Client(protocol);

                retorno = client.GET(path);
                transport.close();

            } catch (TException x) {
                x.printStackTrace();
            }
        }
        return retorno;
    }

    public String LIST(String path) throws org.apache.thrift.TException {
        File file = getFile(path);
        String retorno = null;

        if(file != null){
            System.out.println("LIST RECEIVED ON THIS SERVER");
            ArrayList<File> child = file.getChildren();

            if (child != null) {
                StringBuilder builder = new StringBuilder();

                for (File f : child) {
                    builder.append(f.getName()+"\n");
                }
                retorno =  builder.toString();
            } 
        } 

        else{
            int hash = Math.abs(path.hashCode()) % this.numServers;
            System.out.println("LIST SENDED TO "+ hash+" with path: "+path);
            

            try {
                TTransport transport;

                transport = new TSocket("127.0.0.1", servers.get(hash));
                transport.open();

                TProtocol protocol = new TBinaryProtocol(transport);
                Server.Client client = new Server.Client(protocol);

                retorno = client.LIST(path);
                transport.close();
            } catch (TException x) {
                x.printStackTrace();
            }
        }
        return retorno;
    }

    public boolean ADD(String path, String data) throws
    org.apache.thrift.TException {
        int hash = Math.abs(path.hashCode()) % this.numServers;
        boolean retorno = false;

        if (hash == this.serverName) {
            System.out.println("ADD " + path +" accepted on server " + this.serverName);
            return addFile(path, data) != null;
        }
        else {
            System.out.println("ADD SENDED TO "+ hash+" with path: "+path);
            try {
                TTransport transport;

                transport = new TSocket("127.0.0.1", servers.get(hash));
                transport.open();

                TProtocol protocol = new TBinaryProtocol(transport);
                Server.Client client = new Server.Client(protocol);

                retorno = client.ADD(path, data);
                transport.close();
            } catch (TException x) {
                x.printStackTrace();
            }
        }
        return retorno;
    }

    public boolean UPDATE(String path, String data) throws
    org.apache.thrift.TException {

        File f = getFile(path);
        int hash = Math.abs(path.hashCode()) % this.numServers;
        boolean retorno = false;

        if (f != null) {
            System.out.println("UPDATE RECEIVED ON THIS SERVER");
            f.addData(data);
            retorno =  true;

        } else{
            
            System.out.println("UPDATE SENDED TO "+ hash+" with path: "+path);
            try {
                TTransport transport;
                transport = new TSocket("127.0.0.1", servers.get(hash));
                transport.open();

                TProtocol protocol = new TBinaryProtocol(transport);
                Server.Client client = new Server.Client(protocol);

                retorno = client.UPDATE(path, data);
                transport.close();

            } catch (TException x) {
                x.printStackTrace(); 
                retorno = false;

            }
        }
        return retorno;            
    }

    public boolean DELETE(String path) throws org.apache.thrift.TException {
        int hash = Math.abs(path.hashCode()) % this.numServers;
        boolean retorno = false; 

        if(hash == this.serverName){
            System.out.println("DELETE RECEIVED ON THIS SERVER");
            retorno =  removeFile(path);

        } else{
            System.out.println("DELETE SENDED TO "+ hash+" with path: "+path);
            try {
                TTransport transport;
                transport = new TSocket("127.0.0.1", servers.get(hash));
                transport.open();

                TProtocol protocol = new TBinaryProtocol(transport);
                Server.Client client = new Server.Client(protocol);

                retorno =  client.DELETE(path);
                transport.close();

            } catch (TException x) {
                x.printStackTrace(); 
                retorno =  false;

            }

        }
        return retorno;
    }

    public boolean UPDATE_VERSION(String path, String data, int version) throws org.apache.thrift.TException {
        File f = getFile(path);
        int hash = Math.abs( path.hashCode()) % this.numServers;
        boolean retorno = false;

        if (f != null && f.getVersion() == version) {
            System.out.println("UPDATE_VERSION RECEIVED ON THIS SERVER");
            f.addData(data);
            retorno = true;

        } else if (f == null){
            System.out.println("UPDATE_VERSION SENDED TO "+ hash+" with path: "+path); 
            try {
                TTransport transport;
                transport = new TSocket("127.0.0.1", servers.get(hash));
                transport.open();

                TProtocol protocol = new TBinaryProtocol(transport);
                Server.Client client = new Server.Client(protocol);

                retorno =  client.UPDATE_VERSION(path, data, version);
                transport.close();

            } catch (TException x) {
                x.printStackTrace();
                retorno = false;

            }

        } else retorno =  false;

        return retorno;
    }

    public boolean DELETE_VERSION(String path, int version) throws
    org.apache.thrift.TException {
        
        int hash = Math.abs(path.hashCode()) % this.numServers;
        File f = getFile(path);
        boolean retorno = false;
 
        if (f != null && f.getVersion() == version) {
            System.out.println("DELETE_VERSION RECEIVED ON THIS SERVER");
            retorno =  removeFile(path);

        } else if(f == null){
            System.out.println("DELETE_VERSION SENDED TO "+ hash+" with path: "+path);

            try {
                TTransport transport;
                transport = new TSocket("127.0.0.1", servers.get(hash));
                transport.open();

                TProtocol protocol = new TBinaryProtocol(transport);
                Server.Client client = new Server.Client(protocol);

                retorno = client.DELETE_VERSION(path, version); 
                transport.close();

            } catch (TException x) {
                x.printStackTrace();
                retorno = false; 

            }

        } else retorno = false;
        return retorno;
    }

    public int getServerName() {
        return this.serverName;
    }

    public void setNextServer(HTTPServer s) {
        this.nextServer = s;
    }

    public void setPreviousServer(HTTPServer s) {
        this.previousServer = s;
    }

    public HTTPServer getNextServer() {
        return this.nextServer;
    }

    public HTTPServer getPreviousServer() {
        return this.previousServer;
    }

    public boolean equals(Object o) {
        HTTPServer server = (HTTPServer) o;

        if ((server != null) && (server.getServerName() == this.serverName))
            return true;
        else
            return false;
    }
}
