import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TCP_Client {
    private Socket serverSocket;
    private int commandLen = 500;

    /**
     * Generates a new TCP_Client
     * @param ip the IP of the Server to which the connection will be established as a String
     * @param port the port of the Server to which the connection will be established as int
     * @throws IOException when an error occurred when establishing the connection
     */
    public TCP_Client(String ip, int port) throws IOException {
        this.serverSocket = new Socket(ip, port);
        System.out.println("Client: connected to " + serverSocket.getInetAddress());
        serverSocket.getOutputStream().write("0".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Renames or moves a File or Directory
     * @param path the relative path of the File or Directory that should be renamed
     * @param newPath the new relative path of the File or Directory
     * @return 0 if it was successful and -1 if it failed
     */
    public int rename(String path, String newPath){
        System.out.println(path);
        System.out.println(newPath);
        try {
            //get the name of file from server,
            String s = "rename " + path + " " + newPath;
            byte[] byteArr = new byte[3];
            serverSocket.getOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
            serverSocket.getInputStream().read(byteArr);
            String input = new String(byteArr);

            if(input.equals("500")){
                return -1;
            }
            else{
                return 0;
            }

        }catch (IOException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Deletes a File or Directory
     * @param path the relative path of the File or Directory that should be deleted
     * @return 0 if it was successful and -1 if it failed
     */
    public int delete(String path){
        try {
            //get the name of file from server,
            String s = "delete " + path;
            byte[] byteArr = new byte[3];
            serverSocket.getOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
            serverSocket.getInputStream().read(byteArr);
            String input = new String(byteArr);

            if(input.equals("500")){
                return -1;
            }
            else{
                return 0;
            }

        }catch (IOException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Uploads a File to the Server
     * @param localPath the path of the file to be uploaded
     * @param remotePath the relative path under the documentRoot where the file should be saved on the server
     * @return -1 in error or number of bytes transferred
     */
    public int uploadFile(String localPath, String remotePath) {
        File localFile = new File(localPath);
        if(!localFile.canRead()){
            return -1;
        }
        //Declare Streams
        try{
            OutputStream out = serverSocket.getOutputStream();
            InputStream in = serverSocket.getInputStream();
            byte [] inByte = new byte[3];

            out.write("isWriteable ".concat(remotePath).getBytes(StandardCharsets.UTF_8));
            in.read(inByte);
            if(new String(inByte) == "500"){
                return -1;
            }

            ClientFileTransfer fileTransfer = new ClientFileTransfer(localFile, remotePath,
                    serverSocket.getInetAddress().getHostAddress()
                    , serverSocket.getPort(), false);
        }catch (IOException e){
            e.printStackTrace();
            return -1;
        }
        System.out.println("Started upload");
        return 0;
    }

    /**
     * Downloads a File from the Server
     * @param localPath the path where the file should be saved
     * @param remotePath the relative path under the documentRoot of file on the server
     * @return -1 in error or number of bytes transferred
     */
    public int downloadFile(String localPath, String remotePath){
        //Declaration of Streams and variables
        File localFile = new File(localPath);
        if(localFile.exists()){
            return -1;
        }

        try{
            InputStream in = serverSocket.getInputStream();
            OutputStream tcpOut = serverSocket.getOutputStream();
            byte[] inByte = new byte[3];

            //Send Command to Server
            tcpOut.write("isReadable ".concat(remotePath).getBytes(StandardCharsets.UTF_8));

            in.read(inByte);
            if(new String(inByte).equals("500")){
                return -1;
            }
            ClientFileTransfer fileTransfer = new ClientFileTransfer(localFile, remotePath,
                    serverSocket.getInetAddress().getHostAddress()
                    , serverSocket.getPort(), true);
        }catch (IOException e){
            e.printStackTrace();
            return -1;
        }
        System.out.println("Finished started");
        return 0;
    }

    /**
     * Querys the server of the content of the current path
     * @param path The relative path on the server
     * @return a String containing the content of the Directory
     */
    public String listContent(String path){
        if(path.isEmpty()){
            path = "/";
        }

        String result = "";
        try {
            OutputStream out = serverSocket.getOutputStream();
            InputStream in = serverSocket.getInputStream();
            out.write("ls ".concat(path).getBytes(StandardCharsets.UTF_8));
            int readBytes = 0;
            byte[] inByte = new byte[3];


            in.read(inByte);
            if (new String(inByte).equals("500")) {
                return null;
            }
            inByte = new byte[commandLen];
            //Read from TCP_Stream
            while ((readBytes = in.read(inByte)) != -1) {
                if (readBytes != commandLen) {
                    //Enter if block is not full
                    //Copy only written bytes
                    inByte = Arrays.copyOfRange(inByte, 0, readBytes);
                    result += new String(inByte);
                    break;
                }
                result += new String(inByte);
                inByte = new byte[4096];
            }
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
        return result;
    }

    /**
     * Creates a new Directory
     * @param path the relative path of the new directory
     * @return 0 if it was successful -1 if the creation failed and -2 if the file already exists
     */
    public int createDir(String path){
        try {
            OutputStream out = serverSocket.getOutputStream();
            InputStream in = serverSocket.getInputStream();
            out.write("mkdir ".concat(path).getBytes(StandardCharsets.UTF_8));
            byte[] inByte = new byte[3];
            in.read(inByte);
            String input = new String(inByte);
            if(input.equals("200")){
                return 0;
            }else if(input.equals("500")){
                return -1;
            }else {
                return -2;
            }
        }catch (IOException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Closes the TCP-Connection
     * @throws IOException
     */
    public void close() throws IOException{
        OutputStream out = serverSocket.getOutputStream();
        out.write("closeConnection".getBytes(StandardCharsets.UTF_8));
        serverSocket.close();
    }
}
