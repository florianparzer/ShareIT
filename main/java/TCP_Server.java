import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TCP_Server {
    private ServerSocket localTcpServer;
    private LinkedHashSet<ClientConnection> connections;
    private String configRoot;
    private String documentRoot;
    private String tmpFolder;

    /**
     * Generates a new TCP_Server Object. In order to do that it reads parameters from a config-file
     * The config-file is found in the configRoot
     * @param configRoot The path of the directory where the config-file is found
     * @throws IOException if the file cannot be opened a IOException is thrown
     */
    public TCP_Server(String configRoot) throws IOException{
        this.configRoot = configRoot;
        connections = new LinkedHashSet<ClientConnection>();
        int port;
        //Define Patterns
        Pattern commentPattern = Pattern.compile("(?:#.*|\\s*)");
        Pattern portPattern = Pattern.compile("(?:tcpPort:)\\s*([0-9]{1,5})\\s*");
        Pattern docPattern = Pattern.compile("(?:documentRoot:)\\s*([A-Za-z0-9/\\.\\-_\\?]+)\\s*");
        Pattern tmpPattern = Pattern.compile("(?:tmp:)\\s*([A-Za-z0-9/\\.\\-_\\?]+)\\s*");
        String line;
        try(BufferedReader in = new BufferedReader(new FileReader(configRoot+ "server.conf"))) {
            //try(BufferedReader in = new BufferedReader(new FileReader("configRoot/server.conf"))) {
            Matcher matcher;
            while((line = in.readLine()) != null){
                matcher = commentPattern.matcher(line);
                if(matcher.matches()){
                    //Line matches Comment Pattern
                    continue;
                }
                matcher = portPattern.matcher(line);
                if(matcher.matches()){
                    //Line matches portPattern
                    String portValue = matcher.group(1);
                    //Negative lookahead regex: 1 or more 0 not followed by end-of-line
                    //Replaces leading 0
                    portValue = portValue.replace("^0+(?!$)", "");
                    port = Integer.parseInt(portValue);
                    localTcpServer = new ServerSocket(port);
                    continue;
                }
                matcher = docPattern.matcher(line);
                if(matcher.matches()){
                    //Line matches docPattern
                    documentRoot = matcher.group(1);
                    continue;
                }
                matcher = tmpPattern.matcher(line);
                if(matcher.matches()){
                    //Line matches docPattern
                    tmpFolder = matcher.group(1);
                    continue;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Adds a new client to the set of clients, it also generates and starts the listening thread for the Client Connection
     * @param client is the Socket for the Client Connection
     * @throws IOException
     */
    public void addClient(Socket client){
        ClientConnection clientConnection = new ClientConnection(client, this);
        connections.add(clientConnection);
        clientConnection.start();
    }

    public void removeClient(ClientConnection clientConnection){
        connections.remove(clientConnection);
    }

    public String getDocumentRoot() {
        return documentRoot;
    }

    public String getTmpFolder() {
        return tmpFolder;
    }

    /**
     *Listens for new TCP Connections and creates and starts new ClientConnections
     */
    public void listenConnection(){
        while (true) {
            //Endless loop to accept incoming client connections
            System.out.println("Waiting for Connection");
            try{
                Socket client = localTcpServer.accept();
                System.out.println("Server: connected to Client " + client.getInetAddress());
                byte[] input = new byte[1];
                InputStream inputStream = client.getInputStream();
                inputStream.read(input);
                String option = new String(input);
                if(option.equals("0")){
                    this.addClient(client);
                }else if(option.equals("1")){
                    input = new byte[500];
                    inputStream.read(input);
                    option = getDocumentRoot()+ new String(input).trim();
                    new ServerFileTransfer(true, client, option, tmpFolder);
                }else if(option.equals("2")){
                    input = new byte[500];
                    inputStream.read(input);
                    option = getDocumentRoot()+ new String(input).trim();
                    new ServerFileTransfer(false, client, option, tmpFolder);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    public void close() throws IOException{
        localTcpServer.close();
    }

    public static void main(String[] args) {
        try {
            TCP_Server server = new TCP_Server("src/main/resources/configRoot/");
            server.listenConnection();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
