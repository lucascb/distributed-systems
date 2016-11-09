import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Request implements Runnable {
    private HTTPServer server;
    private Socket clientSocket;

    public Request(HTTPServer server, Socket socket) {
        this.server = server;
        this.clientSocket = socket;
    }

    public void run() {
        try {
            // Cria um fluxo de comunicacao com o cliente
            BufferedReader clientIn = new BufferedReader(
                new InputStreamReader(this.clientSocket.getInputStream(), "ISO-8859-1")
            );
            PrintWriter clientOut = new PrintWriter(
                this.clientSocket.getOutputStream(), true
            );

            // Le a primeira linha do cabeçalho e pega o tipo de requisição e o arquivo
            String stringReceived = clientIn.readLine();
            if (stringReceived.equals("")) {
                this.server.shutdown();
                return;
            }
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
                //System.out.println("Recebido: " + data);
            }
            if (requestType.equals("GET")) {
                System.err.println("GET request received");
                File f = this.server.getFile(path);
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
                File f = this.server.getFile(path);
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
                File f = this.server.getFile(path);
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
                if ((f = this.server.addFile(path, data)) != null) {
                    String response = "HTTP/1.1 200 OK\n" + "Version: " + f.getVersion()
                        + "\nCreation: " + f.getCreationTime() + "\nModification: " +
                            f.getModificationTime() + '\n';
                    clientOut.println(response);
                }
                else {
                    f = this.server.getFile(path);
                    String response = "HTTP/1.1 409 Conflict" + "\nVersion: " +
                        f.getVersion() + "\nCreation: " + f.getCreationTime() +
                            "\nModification: " + f.getModificationTime() + '\n';
                    //clientOut.println("HTTP/1.1 409 Conflict");
                    clientOut.println(response);
                }
            }
            else if (requestType.equals("DELETE")) {
                System.err.println("DELETE request received");
                if (this.server.removeFile(path)) {
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
            //System.err.println("Closed connection with client");
            this.clientSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
