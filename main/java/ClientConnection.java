import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;

public class ClientConnection implements Runnable{
    private Socket clientSocket;
    private TCP_Server handler;
    private Thread connectionThread;
    private int commandLen = 200;

    public ClientConnection(Socket clientSocket, TCP_Server handler) {
        this.clientSocket = clientSocket;
        this.handler = handler;
        connectionThread = new Thread(this);
    }

    @Override
    public void run() {
        try {
            byte[] command = new byte[commandLen];
            String commandText;
            OutputStream out = clientSocket.getOutputStream();
            InputStream in = clientSocket.getInputStream();
            while (true){
                in.read(command);
                commandText = new String(command).trim();
                if(commandText.equals("closeConnection")){
                    closeConnection();
                    break;
                }else if(commandText.startsWith("upload")){
                    //Upload Command
                    uploadFile(handler.getDocumentRoot() + commandText.split(" ")[1]);
                }else if(commandText.startsWith("download")){
                    //Download Command
                    downloadFile(handler.getDocumentRoot() + commandText.split(" ")[1]);
                }else if(commandText.startsWith("rename")){
                    commandText.split(" ");
                }else if(commandText.startsWith("ls")){
                    //List Command
                    try {
                        String content =listContent(handler.getDocumentRoot() + commandText.split(" ")[1]);
                        out.write("200".getBytes(StandardCharsets.UTF_8));
                        out.write(content.getBytes(StandardCharsets.UTF_8));
                    }catch (IndexOutOfBoundsException|NullPointerException e){
                        e.printStackTrace();
                        out.write("500".getBytes(StandardCharsets.UTF_8));
                    }
                }else if(commandText.startsWith("isReadable")){
                    String file = commandText.split(" ")[1];
                    File readFile = new File(handler.getDocumentRoot()+file);
                    if(!readFile.canRead()){
                        out.write("500".getBytes(StandardCharsets.UTF_8));
                        continue;
                    }
                    out.write("100".getBytes(StandardCharsets.UTF_8));
                }else if(commandText.startsWith("isWriteable")){
                    String file = commandText.split(" ")[1];
                    File writeFile = new File(handler.getDocumentRoot()+file);
                    if(!writeFile.canWrite()){
                        out.write("500".getBytes(StandardCharsets.UTF_8));
                        continue;
                    }
                    out.write("100".getBytes(StandardCharsets.UTF_8));
                }
                //TODO other Options
            }
        }catch (IOException e){
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
     * Closes the TCP Connection to the Client
     */
    public void closeConnection(){
        try {
            handler.removeClient(this);
            clientSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
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
            OutputStream tcpOut = clientSocket.getOutputStream();
            byte[] inByte = new byte[4096];
            int readBytes = 0;

            //Code 200 = OK
            tcpOut.write("200".getBytes(StandardCharsets.UTF_8));
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
        }catch (FileNotFoundException e){
            //Send 500 when File not found
            e.printStackTrace();
            try {
                OutputStream tcpOut = clientSocket.getOutputStream();
                tcpOut.write("500".getBytes(StandardCharsets.UTF_8));
            }catch (IOException d){
                d.printStackTrace();
            }
            return -1;
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

            out.write("200".getBytes(StandardCharsets.UTF_8));
            while ((sentBytes = in.read(outBytes)) != -1){
                if(sentBytes != 4096){
                    outBytes = Arrays.copyOfRange(outBytes, 0, sentBytes);
                }
                out.write(outBytes);
                totalSentBytes += sentBytes;
                outBytes = new byte[4096];
            }
        }catch (FileNotFoundException e){
            //Send 500 when File not found
            e.printStackTrace();
            try {
                OutputStream tcpOut = clientSocket.getOutputStream();
                tcpOut.write("500".getBytes(StandardCharsets.UTF_8));
            }catch (IOException d){
                d.printStackTrace();
            }
            return -1;
        }catch (IOException e){
            e.printStackTrace();
            return -1;
        }
        System.out.println("Finished download");
        return totalSentBytes;
    }

    /**
     * Lists all Directories and Files in the directory and formats them as String
     * @param path the Path of the Directory to list its contents
     * @return a formatted String of the Content
     */
    public String listContent(String path){
        String result = "";
        File dir = new File(path);
        TreeSet <String> content = new TreeSet<>();
        File tmp;
        for(String name: dir.list()){
            tmp = new File(dir,name);
            if(tmp.isDirectory()){
                content.add("d:"+name+" ");
            }else{
                content.add("f:" + name + " ");
            }
        }
        result = content.stream().reduce("",String::concat);
        return result.substring(0, result.length()-1)+";";
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
