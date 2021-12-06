import java.io.IOException;
import java.net.Socket;

public class TCP_Client {
    private Socket client;

    public TCP_Client(int port) throws IOException {
        this.client = new Socket("127.0.0.1", port);
        System.out.println("Client: connected to " + client.getInetAddress());
    }

    public void close() throws IOException{
        client.close();
    }
}
