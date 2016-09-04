import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;

public class HTTPServer {
    public static final String htmlBegin = "<!DOCTYPE html><html>";
    public static final String htmlEnd = "</html>";
    public static FileTree ft;

    public static void main(String[] args) throws IOException {
        // Cria um socket para o servidor na porta recebida
        int port = Integer.parseInt(args[0]);
        ServerSocket server = new ServerSocket(port);
        System.err.println("Started server on port " + port);
        System.err.println("Waiting for connection...");
        // Cria um socket para o cliente e espera uma requisicao
        Socket client = server.accept();
        System.err.println("Accepted connection from client " +
            client.getInetAddress().toString());
        // Cria um fluxo de comunicacao com o cliente
        BufferedReader clientIn = new BufferedReader(
            new InputStreamReader(client.getInputStream())
        );
        PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);
        // Le a primeira linha do cabeçalho e pega o tipo de requisição
        String stringReceived = clientIn.readLine();
        String[] stringSplitted = stringReceived.split(" ");
        String requestType = stringSplitted[0];
        String file = stringSplitted[1];

        File f1 = new File("/");
        f1.addData("coxinha\n");

        FileTree ft = new FileTree(f1);

        if (requestType.equals("GET")) {
            File f = ft.getFile(file);
            //clientOut.println(htmlBegin + "GET" + htmlEnd);
            clientOut.println("GET 200 OK");
            clientOut.println("Version: " + f.getVersion());
            clientOut.println("Creation: " + f.getCreationTime());
            clientOut.println("Modification: " + f.getModificationTime());
            clientOut.println("Content-length: " + f.getData().length() + "\n");
            clientOut.println(htmlBegin + f.getData() + htmlEnd);
        }
        else if (requestType.equals("PUT")) {
            clientOut.println(htmlBegin + "PUT" + htmlEnd);
        }
        else if (requestType.equals("HEAD")) {
            clientOut.println(htmlBegin + "HEAD" + htmlEnd);
        }
        else if (requestType.equals("POST")) {
            clientOut.println(htmlBegin + "POST" + htmlEnd);
        }
        else if (requestType.equals("DELETE")) {
            clientOut.println(htmlBegin + "DELETE" + htmlEnd);
        }
        else {
            clientOut.println(htmlBegin + "HTTP/1.1 400 Bad Request" + htmlEnd);
            //break;
        }

        //*
        while (!(stringReceived = clientIn.readLine()).isEmpty()) {
            //System.err.println(stringReceived);
            //s = stringReceived.split(" ");
            continue;
        }
        //*/

        System.err.println("Closed connection with client");
        clientOut.close();
        clientIn.close();
        client.close();
        server.close();

    }

}
