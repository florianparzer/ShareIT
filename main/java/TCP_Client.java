import java.io.IOException;
import java.net.Socket;

public class TCP_Client {
    private Socket serverSocket;

    public TCP_Client(int port) throws IOException {
        this.serverSocket = new Socket("127.0.0.1", port);
        System.out.println("Client: connected to " + serverSocket.getInetAddress());
    }

    public void close() throws IOException{
        serverSocket.close();
    }
}
