import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class HTTPServer {
    private int port;
    private ServerSocket socket;
    private FileTree ft = new FileTree();
    private boolean isOnline = true;

    public HTTPServer(int port) throws IOException {
        this.port = port;

        /* Cria a arvore de arquivos
        ft.addFile("/a", "aaaaaaaaa");
        ft.addFile("/b", "bbbbbbbbbb");
        ft.addFile("/a/c", "cccccc");
        ft.addFile("/b/d", "ddddddd");
        //*/
        try {
            // Cria um socket para o servidor na porta recebida
            this.socket = new ServerSocket(port);
            System.err.println("Started server on port " + port);
        }
        catch (IOException e) {
            System.err.println("Could not create server on port " + port);
            System.exit(-1);
        }
        while (this.isOnline) {
            // Cria um socket para o cliente e espera uma requisicao
            System.err.println("Waiting for connection...");
            Socket client = this.socket.accept();
            System.err.println("Accepted connection from client " +
            client.getInetAddress().toString());
            // Dispara uma thread para atender a requisicao e aguarda uma proxima
            new Thread(new Request(this, client)).start();
        }
        System.err.println("Server halting...");
        this.socket.close();
    }

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

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Wrong parameter, please type 'java HTTPServer port'");
            System.exit(-1);
        }
        new HTTPServer(Integer.parseInt(args[0]));
    }
}
