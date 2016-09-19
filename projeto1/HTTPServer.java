import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;

public class HTTPServer {
    public static final String htmlBegin = "<!DOCTYPE html><html>";
    public static final String htmlEnd = "</html>";
    public static FileTree ft = new FileTree();

    public void start(int port) throws IOException {
        // Cria um socket para o servidor na porta recebida
        ServerSocket server = new ServerSocket(port);
        System.err.println("Started server on port " + port);
        // Cria um socket para o cliente e espera uma requisicao
        while (true) {
            System.err.println("Waiting for connection...");
            Socket client = server.accept();
            System.err.println("Accepted connection from client " +
                client.getInetAddress().toString());
            // Cria um fluxo de comunicacao com o cliente
            BufferedReader clientIn = new BufferedReader(
                new InputStreamReader(client.getInputStream())
            );
            PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);
            // Le a primeira linha do cabeçalho e pega o tipo de requisição
            String stringReceived;
            ArrayList<String> headerBuffer = new ArrayList<String>();
            //*
            while ((stringReceived = clientIn.readLine()) != null &&
                    !stringReceived.isEmpty()) {
                //System.out.println(stringReceived);
                headerBuffer.add(stringReceived);
            }

            String[] stringSplitted = headerBuffer.get(0).split(" ");
            String requestType = stringSplitted[0];
            String path = stringSplitted[1];
            String data = "";
            if (requestType.equals("END")) break;
            //*/
            // Caso o tipo da requisicao seja PUT ou POST, le o corpo
            if (requestType.equals("PUT") || requestType.equals("POST")) {
                StringBuilder dataBuffer = new StringBuilder();
                while ((stringReceived = clientIn.readLine()) != null &&
                        !stringReceived.isEmpty()) {
                    //System.out.println(stringReceived);
                    dataBuffer.append(stringReceived);
                }
                data = dataBuffer.toString();
            }
            if (requestType.equals("GET")) {
                File f = ft.getFile(path);
                if (f != null) {
                    //clientOut.println(htmlBegin + "GET" + htmlEnd);
                    clientOut.println("GET 200 OK");
                    clientOut.println("Version: " + f.getVersion());
                    clientOut.println("Creation: " + f.getCreationTime());
                    clientOut.println("Modification: " + f.getModificationTime());
                    clientOut.println("Content-length: " + f.getData().length() + "\n");
                    clientOut.println(f.getData());
                }
                else {
                    clientOut.println("GET 404 Not Found\n");
                }
            }
            else if (requestType.equals("PUT")) {
                File f = ft.getFile(path);
                if (f != null) {
                    f.addData(data);
                    clientOut.println("PUT 200 OK");
                    clientOut.println("Version: " + f.getVersion());
                    clientOut.println("Creation: " + f.getCreationTime());
                    clientOut.println("Modification: " + f.getModificationTime());
                }
                else {
                    clientOut.println("PUT 404 Not Found\n");
                }
            }
            else if (requestType.equals("HEAD")) {
                File f = ft.getFile(path);
                if (f != null) {
                    //clientOut.println(htmlBegin + "GET" + htmlEnd);
                    clientOut.println("HEAD 200 OK");
                    clientOut.println("Version: " + f.getVersion());
                    clientOut.println("Creation: " + f.getCreationTime());
                    clientOut.println("Modification: " + f.getModificationTime());
                    clientOut.println("Content-length: " + f.getData().length() + "\n");
                }
                else {
                    clientOut.println("HEAD 404 Not Found\n");
                }
            }
            else if (requestType.equals("POST")) {
                File f;
                if ((f = ft.addFile(path, data)) != null) {
                    clientOut.println("POST 200 OK");
                    clientOut.println("Version: " + f.getVersion());
                    clientOut.println("Creation: " + f.getCreationTime());
                    clientOut.println("Modification: " + f.getModificationTime());
                }
                else {
                    clientOut.println("POST 409 Conflict");
                }
            }
            else if (requestType.equals("DELETE")) {
                if (ft.removeFile(path)) {
                    clientOut.println("DELETE 200 OK");
                }
                else {
                    clientOut.println("DELETE 406 Not Acceptable");
                }
            }
            else {
                clientOut.println("HTTP/1.1 400 Bad Request");
                //break;
            }
            clientOut.close();
            clientIn.close();
            client.close();
        }
        System.err.println("Closed connection with client");
        server.close();
    }

    public static void main(String[] args) throws IOException {
        // Cria a arvore de arquivos
        File f1 = new File("/");
        f1.addData("aaaaaaaaa");

        File f2 = new File("a");
        f2.addData("bbbbbbbbbb");

        File f3 = new File("b");
        f3.addData("cccccc");
        f3.addData("ddddddd");

        File f4 = new File("d");
        f4.addData("eeeeeeeeee");
        f4.addData("ffffff");

        f1.addChild(f2);
        f1.addChild(f3);
        f3.addChild(f4);

        ft.setRoot(f1);

        new HTTPServer().start(Integer.parseInt(args[0]));
    }
}
