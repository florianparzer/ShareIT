import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TCP_Client {
    private Socket serverSocket;
    private int commandLen = 100;

    public TCP_Client(String ip, int port) throws IOException {
        this.serverSocket = new Socket(ip, port);
        System.out.println("Client: connected to " + serverSocket.getInetAddress());
    }
    //TODO Arsani
    /*
    //>>>"rename <oldPath> <newPath>"
    public int rename(String path, String newPath){
        try {
            serverSocket.getOutputStream().write("");
        }catch (IOException e){
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    */

    public int uploadFile(String localPath, String remotePath) {
        int totalSentBytes = 0;
        //Declare Streams
        try(
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(localPath)))
        ){
            OutputStream out = serverSocket.getOutputStream();
            InputStream tcpIn = serverSocket.getInputStream();
            byte [] inByte = new byte[3];
            byte [] outBytes = new byte[4096];
            int sentBytes = 0;

            out.write("upload ".concat(remotePath).getBytes(StandardCharsets.UTF_8));
            tcpIn.read(inByte);
            if(new String(inByte).equals("500")){
                return -1;
            }
            //Read from File and write to TCP-Stream
            while ((sentBytes = in.read(outBytes)) != -1){
                if(sentBytes != 4096){
                    //If block is not full only copy written bytes
                    outBytes = Arrays.copyOfRange(outBytes, 0, sentBytes);
                }
                out.write(outBytes);
                totalSentBytes += sentBytes;
                outBytes = new byte[4096];
            }
        }catch (IOException e){
            e.printStackTrace();
            return -1;
        }
        System.out.println("Finished upload");
        return totalSentBytes;
    }

    public int downloadFile(String localPath, String remotePath){
        //Declaration of Streams and variables
        int totalReadBytes = 0;
        try(
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(localPath)))
        ) {
            InputStream in = serverSocket.getInputStream();
            OutputStream tcpOut = serverSocket.getOutputStream();
            byte[] inByte = new byte[3];
            int readBytes = 0;

            //Send Command to Server
            tcpOut.write("download ".concat(remotePath).getBytes(StandardCharsets.UTF_8));

            in.read(inByte);
            if(new String(inByte).equals("500")){
                return -1;
            }
            inByte = new byte[4096];
            //Read from InputStream
            while ((readBytes = in.read(inByte)) != -1) {
                if (readBytes != 4096) {
                    //Enter if block is not full
                    //Copy only written bytes
                    inByte = Arrays.copyOfRange(inByte, 0, readBytes);
                    out.write(inByte);
                    totalReadBytes += readBytes;
                    break;
                }
                out.write(inByte);
                totalReadBytes += readBytes;
                inByte = new byte[4096];
            }
        }catch (IOException e){
            e.printStackTrace();
            return -1;
        }
        System.out.println("Finished download");
        return totalReadBytes;
    }

    public String listContent(String path){
        if(path.isEmpty()){
            path = ".";
        }
        if(path.startsWith("/")){
            path = path.substring(1);
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
     * Closes the TCP-Connection
     * @throws IOException
     */
    public void close() throws IOException{
        //TODO Send Close CMD
        OutputStream out = serverSocket.getOutputStream();
        out.write("closeConnection".getBytes(StandardCharsets.UTF_8));
        serverSocket.close();
    }
}
