import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class TCP_Server {
    private ServerSocket server;
    private List<ClientConnection> connections;

    public TCP_Server(int port) throws IOException{
        server = new ServerSocket(port);
    }

    public void addClient(Socket client) throws IOException{
        ClientConnection clientConnection = new ClientConnection(client, this);
    }


    public void listenConnection(){
        try {
            while (true) {
                System.out.println("Waiting for Connection");
                Socket client = server.accept();
                System.out.println("Server: connected to Client " + client.getInetAddress());
                this.addClient(client);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void close() throws IOException{
        server.close();
    }

    public static void main(String[] args) {
        try {
            //TODO Read Config-Files
            TCP_Server server = new TCP_Server(1234);
            server.listenConnection();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
