import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ServerFileTransfer implements Runnable{
    private Thread fileTransferThread;
    private Socket clientSocket;
    private String localPath;
    private String tmpPath;
    private boolean isDownload;
    private int blockSize = 4096;

    public ServerFileTransfer(boolean isDownload, Socket server, String localPath, String tmp) {
        this.isDownload = isDownload;
        clientSocket = server;
        this.localPath = localPath;
        tmpPath = tmp + localPath.substring(localPath.lastIndexOf("/"));
        fileTransferThread = new Thread(this);
        fileTransferThread.start();
    }

    @Override
    public void run() {
        if(isDownload){
            downloadFile();
        }else {
            uploadFile();
        }
        try {
            clientSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void downloadFile(){
        try(
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmpPath))
        ) {
            OutputStream out = clientSocket.getOutputStream();
            byte [] outBytes = new byte[blockSize];
            int sentBytes = 0;

            while ((sentBytes = in.read(outBytes)) != -1){
                if(sentBytes != blockSize){
                    outBytes = Arrays.copyOfRange(outBytes, 0, sentBytes);
                }
                out.write(outBytes);
                outBytes = new byte[blockSize];
            }

            new File(tmpPath).renameTo(new File(localPath));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void uploadFile(){
        try(
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localPath))
        ){
            int readBytes = 0;
            byte[] inByte = new byte[blockSize];
            InputStream in = clientSocket.getInputStream();
            //Read from InputStream
            while ((readBytes = in.read(inByte)) != -1) {
                if (readBytes != blockSize) {
                    //Enter if block is not full
                    //Copy only written bytes
                    inByte = Arrays.copyOfRange(inByte, 0, readBytes);
                    out.write(inByte);
                    break;
                }
                out.write(inByte);
                inByte = new byte[blockSize];
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
