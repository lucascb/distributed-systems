import java.net.*;
import java.io.*;

public class HTTPServer {

    public static void main(String[] args) throws IOException {

        int port = Integer.parseInt(args[0]);
        ServerSocket server = new ServerSocket(port);
        System.err.println("Started server on port " + port);
        System.err.println("Waiting for connection...");

        Socket client = server.accept();
        System.err.println("Accepted connection from client " +
            client.getInetAddress().toString());

        BufferedReader clientIn = new BufferedReader(
            new InputStreamReader(client.getInputStream())
        );
        PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);

        String stringReceived;
        String[] s;
        while ((stringReceived = clientIn.readLine()) != null) {
            System.err.println("Received: " + stringReceived);
            // Quebra a mensagem recebida em partes e verifica o tipo da requisição HTTP
            s = stringReceived.split(" ");
            if (s[0].equals("GET")) {
                clientOut.println("GET");
                continue;
            }
            else if (s[0].equals("PUT")) {
                clientOut.println("PUT");
                continue;
            }
            else if (s[0].equals("HEAD")) {
                clientOut.println("HEAD");
                continue;
            }
            else if (s[0].equals("POST")) {
                clientOut.println("POST");
                continue;
            }
            else if (s[0].equals("DELETE")) {
                clientOut.println("DELETE");
                continue;
            }
            else {
                clientOut.println("HTTP/1.1 400 Bad Request");
                break;
            }
        }

        System.err.println("Closed connection with client");
        clientOut.close();
        clientIn.close();
        client.close();
        server.close();

    }

}
