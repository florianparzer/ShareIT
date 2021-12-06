import java.net.Socket;

public class ClientConnection {
    private Socket client;
    private TCP_Server handler;

    public ClientConnection(Socket client, TCP_Server handler) {
        this.client = client;
        this.handler = handler;
    }
}
