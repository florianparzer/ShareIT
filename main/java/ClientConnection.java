import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

public class ClientConnection implements Runnable{
    private Socket clientSocket;
    private TCP_Server handler;
    private Thread connectionThread;

    public ClientConnection(Socket clientSocket, TCP_Server handler) {
        this.clientSocket = clientSocket;
        this.handler = handler;
        connectionThread = new Thread(this);
    }

    @Override
    public void run() {
        try {
            uploadFile("src/main/resources/server.conf");
            System.out.println("sleep1");
            Thread.sleep(20000);
            System.out.println("sleep1 fin");
            downloadFile("src/main/resources/configRoot/server.conf");
            System.out.println("sleep1");
            Thread.sleep(20000);
            System.out.println("sleep1 fin");
            clientSocket.close();
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * Starts the thread of the ClientConnection
     */
    public void start(){
        connectionThread.start();
    }

    /**
     * Reads a File from the Socket and writes it as File to the defined path
     * @param path the path of the destination file
     * @return the number of bytes read
     */
    public int uploadFile(String path){
        //Declaration of Streams and variables
        int totalReadBytes = 0;
        try(
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path)))
        ) {
            InputStream in = clientSocket.getInputStream();
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
        System.out.println("Finished upload");
        return totalReadBytes;
    }

    /**
     * Reads the file from the disk and writes it to the socket
     * @param path The path of the file
     * @return number of bytes sent
     */
    public int downloadFile(String path){
        int totalSentBytes = 0;
        try(
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)))
        ){
            OutputStream out = clientSocket.getOutputStream();
            byte [] outBytes = new byte[4096];
            int sentBytes = 0;
            while ((sentBytes = in.read(outBytes)) != -1){
                if(sentBytes != 4096){
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
        System.out.println("Finished download");
        return totalSentBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientConnection that = (ClientConnection) o;
        return Objects.equals(clientSocket, that.clientSocket) && Objects.equals(handler, that.handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientSocket, handler);
    }
}
