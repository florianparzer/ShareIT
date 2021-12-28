import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TCP_Client {
    private Socket serverSocket;

    public TCP_Client(String ip, int port) throws IOException {
        this.serverSocket = new Socket(ip, port);
        System.out.println("Client: connected to " + serverSocket.getInetAddress());
    }
    //TODO Arsani
    /*
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

    public int uploadFile(String path){
        int totalSentBytes = 0;
        //Declare Streams
        try(
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)))
        ){
            OutputStream out = serverSocket.getOutputStream();
            byte [] outBytes = new byte[4096];
            int sentBytes = 0;
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

    public int downloadFile(String path){
        //Declaration of Streams and variables
        int totalReadBytes = 0;
        try(
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path)))
        ) {
            InputStream in = serverSocket.getInputStream();
            byte[] inByte = new byte[4096];
            int readBytes = 0;

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

    public void close() throws IOException{
        serverSocket.close();
    }
}
