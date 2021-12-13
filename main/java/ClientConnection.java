import java.net.Socket;
import java.util.Objects;

public class ClientConnection {
    private Socket client;
    private TCP_Server handler;

    public ClientConnection(Socket client, TCP_Server handler) {
        this.client = client;
        this.handler = handler;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientConnection that = (ClientConnection) o;
        return Objects.equals(client, that.client) && Objects.equals(handler, that.handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(client, handler);
    }
}
