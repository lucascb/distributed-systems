import java.net.*;
import java.io.*;

public class HTTPServer {

    public static void main(String[] args) throws IOException {
        int port = 4444;
        ServerSocket server = new ServerSocket(port);
        System.err.println("Started server on port " + port);
        System.err.println("Waiting for connection...");

        Socket client = server.accept();
        System.err.println("Accepted connection from client.");

        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter output = new PrintWriter(client.getOutputStream(), true);

        String s;
        while ((s = input.readLine()) != null) {
            System.err.println("Received: " + s);
            output.println(s);

            if (s.equals("bye")) {
                break;
            }
        }

        System.err.println("Closing connection with client.");
        output.close();
        input.close();
        client.close();
        server.close();

    }

}
