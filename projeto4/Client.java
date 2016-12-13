import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class Client {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Wrong parameter, please type 'java Server port'");
            System.exit(-1);
        }
        try {
            TTransport transport;

            transport = new TSocket("127.0.0.1", Integer.parseInt(args[0]));
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            Server.Client client = new Server.Client(protocol);

            perform(client);
            transport.close();
        } catch (TException x) {
            x.printStackTrace(); 
        }
    }

    private static void perform(Server.Client client)
    throws TException {
//        client.ADD("/user", "eduardo");
//        client.ADD("/user/age", "21 anos");  
        client.ADD("/user/age/a", "eduardo");
        client.ADD("/user/age/a/b", "21 anos");  
        client.ADD("/user/age/b", "eduardo");
        client.ADD("/user/age/b/c", "21 anos");  
        //client.ADD("/coxinha", "gostosa");
        //System.out.println(client.LIST("/user"));
        System.out.println(client.GET("/user") + " - \n" + client.GET("/user/age"));
        //System.out.println(client.GET("/coxinha"));
        System.out.println(client.LIST("/user"));
    }
}
