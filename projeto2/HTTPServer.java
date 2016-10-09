import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
                new InputStreamReader(client.getInputStream(), "ISO-8859-1")
            );
            PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);

            // Le a primeira linha do cabeçalho e pega o tipo de requisição e o arquivo
            String stringReceived = clientIn.readLine();
            if (stringReceived.equals("")) break;
            String[] stringSplitted = stringReceived.split(" ");
            String requestType = stringSplitted[0];
            String path = stringSplitted[1];

            // Le o restante do cabeçalho e pega os parametros
            Map<String, String> parameters = new HashMap<String, String>();
            while ((stringReceived = clientIn.readLine()) != null &&
                    !stringReceived.isEmpty()) {
                String[] paramSplitted = stringReceived.split(" ", 2);
                parameters.put(paramSplitted[0], paramSplitted[1]);
            }
            //System.err.println(parameters);

            // Caso o tipo da requisicao seja PUT ou POST, le o corpo
            String data = "";
            if (requestType.equals("PUT") || requestType.equals("POST")) {
                String length = parameters.get("Content-Length:");
                int bodySize = Integer.parseInt(length);
                //System.err.println(bodySize);
                StringBuilder dataBuffer = new StringBuilder();
                char charReceived;
                for (int i = 0; i < bodySize; i++) {
                    charReceived = (char) clientIn.read();
                    //System.out.println(stringReceived);
                    dataBuffer.append(charReceived);
                }
                data = dataBuffer.toString();
            }
            if (requestType.equals("GET")) {
                System.err.println("GET request received");
                File f = ft.getFile(path);
                if (f != null) {
                    String header = "HTTP/1.1 200 OK\n" + "Version: " + f.getVersion()
                        + "\nCreation: " + f.getCreationTime() + "\nModification: " +
                            f.getModificationTime() + "\nContent-length: " +
                                f.getData().length() + '\n';
                    clientOut.println(header);
                    clientOut.println(f.getData());
                }
                else {
                    clientOut.println("HTTP/1.1 404 Not Found\n");
                }
            }
            else if (requestType.equals("PUT")) {
                System.err.println("PUT request received");
                File f = ft.getFile(path);
                if (f != null) {
                    f.addData(data);
                    String response = "HTTP/1.1 200 OK\n" + "Version: " + f.getVersion()
                        + "\nCreation: " + f.getCreationTime() + "\nModification: " +
                            f.getModificationTime() + '\n';
                    clientOut.println(response);
                }
                else {
                    clientOut.println("HTTP/1.1 404 Not Found\n");
                }
            }
            else if (requestType.equals("HEAD")) {
                System.err.println("HEAD request received");
                File f = ft.getFile(path);
                if (f != null) {
                    String response = "HTTP/1.1 200 OK\n" + "Version: " + f.getVersion()
                        + "\nCreation: " + f.getCreationTime() + "\nModification: " +
                            f.getModificationTime() + "\nContent-length: " +
                                f.getData().length() + '\n';
                    clientOut.println(response);
                }
                else {
                    clientOut.println("HTTP/1.1 HEAD 404 Not Found");
                }
            }
            else if (requestType.equals("POST")) {
                System.err.println("POST request received");
                File f;
                if ((f = ft.addFile(path, data)) != null) {
                    String response = "HTTP/1.1 200 OK\n" + "Version: " + f.getVersion()
                        + "\nCreation: " + f.getCreationTime() + "\nModification: " +
                            f.getModificationTime() + '\n';
                    clientOut.println(response);
                }
                else {
                    f = ft.getFile(path);
                    String response = "HTTP/1.1 409 Conflict" + "\nVersion: " +
                        f.getVersion() + "\nCreation: " + f.getCreationTime() +
                            "\nModification: " + f.getModificationTime() + '\n';
                    //clientOut.println("HTTP/1.1 409 Conflict");
                    clientOut.println(response);
                }
            }
            else if (requestType.equals("DELETE")) {
                System.err.println("DELETE request received");
                if (ft.removeFile(path)) {
                    clientOut.println("HTTP/1.1 200 OK");
                }
                else {
                    clientOut.println("HTTP/1.1 406 Not Acceptable");
                }
            }
            else {
                System.err.println("Bad request received");
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
        if (args.length != 1) {
            System.err.println("Wrong parameter, please type 'java HTTPServer port'");
            return;
        }
        // Cria a arvore de arquivos
        ft.addFile("/a", "aaaaaaaaa");
        ft.addFile("/b", "bbbbbbbbbb");
        ft.addFile("/a/c", "cccccc");
        ft.addFile("/b/d", "ddddddd");

        new HTTPServer().start(Integer.parseInt(args[0]));
    }
}
