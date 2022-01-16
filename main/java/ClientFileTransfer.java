import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClientFileTransfer implements Runnable{
    private int transferredBytes;
    private Thread fileTransferThread;
    private File localFile;
    private Socket serverSocket;
    private boolean isDownload;
    private int blockSize = 4096;

    /**
     * Generates a ClientFileTransfer object
     * @param file the local file that should be uploaded or where the download should be saved
     * @param remotePath the remote path where the file should be uploaded or from where it should be downloaded
     * @param ip the IP of the Server to which will be connected
     * @param port the port of the Server to which will be connected
     * @param isDownload boolean if the FileTransfer is a download (true) or upload (false)
     * @throws IOException when new Sockets could not be established
     */
    public ClientFileTransfer(File file, String remotePath, String ip, int port, boolean isDownload) throws IOException {
        transferredBytes = 0;
        fileTransferThread = new Thread(this);
        localFile = file;
        this.isDownload = isDownload;
        this.serverSocket = new Socket(ip, port);
        if(isDownload) {
            serverSocket.getOutputStream().write("1".getBytes(StandardCharsets.UTF_8));
        }else {
            serverSocket.getOutputStream().write("2".getBytes(StandardCharsets.UTF_8));
        }
        serverSocket.getOutputStream().write(remotePath.getBytes(StandardCharsets.UTF_8));
        fileTransferThread.start();
    }

    @Override
    /**
     * The method of the thread which calls ether a download method or upload method
     */
    public void run() {
        if(isDownload){
            downloadFile();
        }else {
            uploadFile();
        }
        try {
            serverSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * This method opens a local file and writes the data coming from the tcp stream to it
     */
    public void downloadFile(){
        try(
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localFile))
        ){
            int readBytes = 0;
            byte[] inByte = new byte[blockSize];
            InputStream in = serverSocket.getInputStream();
            //Read from InputStream
            while ((readBytes = in.read(inByte)) != -1) {
                if (readBytes != blockSize) {
                    //Enter if block is not full
                    //Copy only written bytes
                    inByte = Arrays.copyOfRange(inByte, 0, readBytes);
                    out.write(inByte);
                    transferredBytes += readBytes;
                    break;
                }
                out.write(inByte);
                transferredBytes += readBytes;
                inByte = new byte[blockSize];
            }
        }catch (IOException e){
            e.printStackTrace();
            transferredBytes = -1;
        }
    }

    /**
     * This method opens a local File and writes the data in blocks out to the tcp stream
     */
    public void uploadFile(){
        try(
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(localFile))
        ) {
            int sentBytes = 0;
            byte [] outBytes = new byte[blockSize];
            OutputStream out = serverSocket.getOutputStream();
            while ((sentBytes = in.read(outBytes)) != -1){
                if(sentBytes != blockSize){
                    outBytes = Arrays.copyOfRange(outBytes, 0, sentBytes);
                }
                out.write(outBytes);
                outBytes = new byte[blockSize];
            }
            //out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
