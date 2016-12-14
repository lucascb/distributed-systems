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

        processor = new Server.Processor(this);
        Runnable run = new Runnable() {
            public void run() {
                try {
                    TServerTransport serverTransport = new TServerSocket(port);
                    TServer server = new TSimpleServer(
                        new Args(serverTransport).processor(processor)
                    );
                    System.out.println("Starting server " + serverName + " on port "
                        + port + "...");
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
        int hash = Math.abs(path.hashCode()) % this.numServers;
        String retorno = null;
        // Checks if this server is responsible for the file
        if (hash == this.serverName) {
            System.out.println("GET "+ path + " accepted on server " + this.serverName);
            File f = getFile(path);
            if (f != null) {
                String response = "FILE FOUND ON SERVER " + this.serverName
                    + "\nVersion: " + f.getVersion() + "\nCreation: "
                        + f.getCreationTime() + "\nModification: "
                            + f.getModificationTime() + "\nContent-length: "
                                + f.getData().length() + '\n' + f.getData() + "\n";
                return response;
            }
            else {
                return "FILE NOT FOUND\n";
            }
        }
        else {
            System.out.println("GET " + path + " sent to server "+ hash);
            try {
                TTransport transport = new TSocket("127.0.0.1", servers.get(hash));
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

        if (file != null){
            System.out.println("LIST "+ path + " accepted on server " + this.serverName);
            ArrayList<File> child = file.getChildren();

            if (child != null) {
                StringBuilder builder = new StringBuilder();
                for (File f : child) {
                    builder.append(f.getName()+"\n");
                }
                retorno =  builder.toString();
            }
        }
        else {
            int hash = Math.abs(path.hashCode()) % this.numServers;
            System.out.println("LIST " + path + " sent to server "+ hash);
            try {
                TTransport transport = new TSocket("127.0.0.1", servers.get(hash));
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

    public boolean ADD(int senderName, String path, String data) throws
    org.apache.thrift.TException {
        int hash = Math.abs(path.hashCode()) % this.numServers;
        boolean retorno = false;
        TTransport transport;
        TProtocol protocol;
        Server.Client client;

        if (hash == this.serverName) {
            System.out.println("ADD "+ path + " accepted on server " + this.serverName
            + " received from " + senderName);
            // Checks if the entire path exists
            // E.g. if /a/b/c will be added, then /a and /a/b must exist
            String[] files = path.substring(1).split("/");
            String filepath = "";
            for (String f : files) {
                // Build the path and send a message to the responsible server
                filepath += ("/" + f);
                //System.out.println(filepath);
                hash = Math.abs(filepath.hashCode()) % this.numServers;
                if (hash == this.serverName) {
                    System.out.println("ADD " + filepath + " created on itself");
                    addFile(filepath, "");
                }
                else if (-1 == senderName) {
                    System.out.println("ADD " + filepath + " sent to server " + hash);
                    transport = new TSocket("127.0.0.1", servers.get(hash));
                    transport.open();
                    protocol = new TBinaryProtocol(transport);
                    client = new Server.Client(protocol);
                    // If the parent path doesn't exist then the file can't be added
                    client.ADD(this.serverName, filepath, "");
                    transport.close();
                }
            }
            return true;
        }
        else {
            // If the server is not the responsible then send to the correct one
            String[] files = path.substring(1).split("/");
            String filepath = "";
            for (String f : files) {
                filepath += ("/" + f);
                hash = Math.abs(filepath.hashCode()) % this.numServers;
                if (hash == this.serverName) {
                    System.out.println("ADD " + filepath + " created on itself");
                    addFile(filepath, "");
                }
                else if (hash != senderName) {
                    System.out.println("ADD " + filepath + " sent to server " + hash);
                    transport = new TSocket("127.0.0.1", servers.get(hash));
                    transport.open();
                    protocol = new TBinaryProtocol(transport);
                    client = new Server.Client(protocol);
                    // If the parent path doesn't exist then the file can't be added
                    client.ADD(this.serverName, filepath, "");
                    transport.close();
                }
            }
        }

        for (int i = 0; i < numServers; i++){
            if (i != serverName){
                transport = new TSocket("127.0.0.1", servers.get(i));
                transport.open();
                protocol = new TBinaryProtocol(transport);
                client = new Server.Client(protocol);

                if(!client.CAN_COMMIT()) {
                    transport.close();
                    for (int j = 0; j < numServers; j++){
                        if (j != serverName){
                            transport = new TSocket("127.0.0.1", servers.get(j));
                            transport.open();
                            protocol = new TBinaryProtocol(transport);
                            client = new Server.Client(protocol);

                            client.ABORT();
                            transport.close();
                        }
                        else {
                            this.ft.abort();
                        }
                    }
                    return false;
                }
                transport.close();
            }
            else if (!this.ft.canCommit()) {
                for(int j = 0; j < numServers; j++) {
                    if (j != serverName) {
                        transport = new TSocket("127.0.0.1", servers.get(j));
                        transport.open();
                        protocol = new TBinaryProtocol(transport);
                        client = new Server.Client(protocol);

                        client.ABORT();
                        transport.close();
                    }
                    else {
                        System.out.println("Aborted on server " + this.serverName);
                        this.ft.abort();
                    }
                }
                return false;
            }
        }

        for (int j = 0; j < numServers; j++) {
            if (j != serverName) {
                transport = new TSocket("127.0.0.1", servers.get(j));
                transport.open();
                protocol = new TBinaryProtocol(transport);
                client = new Server.Client(protocol);
                client.COMMIT();
                transport.close();
            }
            else {
                System.out.println("Commited on server " + this.serverName);
                this.ft.commit();
            }
        }
        return retorno;
    }

    public boolean UPDATE(String path, String data) throws
    org.apache.thrift.TException {
        int hash = Math.abs(path.hashCode()) % this.numServers;
        boolean retorno = false;

        if (hash == this.serverName) {
            System.out.println("UPDATE "+ path + " accepted on server " + this.serverName);
            File f = getFile(path);
            if (f != null) {
                f.addData(data);
                return true;
            }
            else {
                return false;
            }
        } else {
            System.out.println("UPDATE " + path + " sent to server "+ hash);
            try {
                TTransport transport = new TSocket("127.0.0.1", servers.get(hash));
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

        if (hash == this.serverName) {
            System.out.println("DELETE " + path + " accepted on server " + this.serverName);
            retorno = removeFile(path);
        }
        else {
            System.out.println("DELETE " + path + " sent to server " + hash);
            try {
                TTransport transport = new TSocket("127.0.0.1", servers.get(hash));
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                Server.Client client = new Server.Client(protocol);

                retorno = client.DELETE(path);
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
            System.out.println("UPDATE_VERSION SENT TO "+ hash+" with path: "+path);
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
            System.out.println("DELETE_VERSION SENT TO "+ hash +" with path: "+path);

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

    public boolean CAN_COMMIT() throws org.apache.thrift.TException{
        return ft.canCommit();
    }

    public void COMMIT(){
        ft.commit();
        System.out.println("Commited on server " + this.serverName);
    }

    public void ABORT(){
        ft.abort();
        System.out.println("Aborted on server " + this.serverName);
    }

    public int getServerName() {
        return this.serverName;
    }
}
