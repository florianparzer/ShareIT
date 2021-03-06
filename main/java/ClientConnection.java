import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;

public class ClientConnection implements Runnable{
    private Socket clientSocket;
    private TCP_Server handler;
    private Thread connectionThread;
    private int commandLen = 500;
    private String ack = "200";
    private String error = "500";

    /**
     * Creates a new ClientConnection
     * @param clientSocket is the TCP Socket for the Client connection
     * @param handler is the TCP Server that called the Method
     */
    public ClientConnection(Socket clientSocket, TCP_Server handler) {
        this.clientSocket = clientSocket;
        this.handler = handler;
        connectionThread = new Thread(this);
    }

    @Override
    /**
     * Is the CommandListener for the Client Connection, it reads the tcp for incoming commands and executes them
     */
    public void run() {
        try {
            byte[] command = new byte[commandLen];
            String commandText;
            OutputStream out = clientSocket.getOutputStream();
            InputStream in = clientSocket.getInputStream();
            while (true){
                command = new byte[commandLen];
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
                    int result = rename(handler.getDocumentRoot() + commandText.split(" ")[1], handler.getDocumentRoot() + commandText.split(" ")[2]);
                    if(result == 0){
                        out.write(ack.getBytes(StandardCharsets.UTF_8));
                    }else{
                        out.write(error.getBytes(StandardCharsets.UTF_8));
                    }
                }else if(commandText.startsWith("ls")){
                    //List Command
                    try {
                        String content =listContent(handler.getDocumentRoot() + commandText.split(" ")[1]);
                        out.write(ack.getBytes(StandardCharsets.UTF_8));
                        out.write(content.getBytes(StandardCharsets.UTF_8));
                    }catch (IndexOutOfBoundsException|NullPointerException e){
                        e.printStackTrace();
                        out.write(error.getBytes(StandardCharsets.UTF_8));
                    }
                }else if(commandText.startsWith("isReadable")){
                    String file = commandText.split(" ")[1];
                    File readFile = new File(handler.getDocumentRoot()+file);
                    if(!readFile.canRead()){
                        out.write(error.getBytes(StandardCharsets.UTF_8));
                        continue;
                    }
                    out.write(ack.getBytes(StandardCharsets.UTF_8));
                }else if(commandText.startsWith("isWriteable")){
                    String file = commandText.split(" ")[1];
                    File writeFile = new File(handler.getDocumentRoot()+file);
                    if(writeFile.exists()){
                        out.write(error.getBytes(StandardCharsets.UTF_8));
                        continue;
                    }
                    out.write(ack.getBytes(StandardCharsets.UTF_8));
                }else if(commandText.startsWith("delete")){
                    int result =  delete(handler.getDocumentRoot() + commandText.split(" ")[1]);
                    if(result == 0){
                        out.write(ack.getBytes(StandardCharsets.UTF_8));
                    }else {
                        out.write(error.getBytes(StandardCharsets.UTF_8));
                    }
                }else if(commandText.startsWith("mkdir")){
                    int result = createDir(handler.getDocumentRoot() + commandText.split(" ")[1]);
                    if(result == 0){
                        out.write(ack.getBytes(StandardCharsets.UTF_8));
                    }else if(result == -1){
                        out.write(error.getBytes(StandardCharsets.UTF_8));
                    }else {
                        out.write("501".getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
            closeConnection();
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
        if(result.equals("")){
            return ";";
        }
        return result.substring(0, result.length()-1)+";";
    }

    /**
     * Renames a file
     * @param path the File which should be renamed
     * @param newPath the new Name of the File
     * @return 0 if rename was successfull and -1 if not
     */
    public int rename(String path, String newPath){
        File file = new File(path);
        File file2 = new File(newPath);

        if (file2.exists()) {
            return 1;
        }

        boolean success = file.renameTo(file2);
        try{
            if (success) {
                clientSocket.getOutputStream().write("200".getBytes(StandardCharsets.UTF_8));
                return 0;
            }
            clientSocket.getOutputStream().write("500".getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e){
            e.printStackTrace();
            return -1;
        }
        return -1;
    }

    /**
     * Deletes a file
     * @param path the path of the file as String
     * @return 0 if deletion was successful and -1 if it failed
     */
    public int delete(String path){
        try{
            File f = new File(path);
            boolean hasWorked = false;

            if(f.isDirectory()){
                hasWorked = deleteDir(f);
            }else{
                hasWorked = f.delete();
            }
            if(hasWorked){
                return 0;
            }else {
                return -1;
            }

        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Deletes a Directory by recursively calling the method for subdirectories
     * @param folder the folder which should be deleted as File
     * @return true if it was successful and false if it failed
     */
    public static boolean deleteDir(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteDir(f);
                } else {
                    f.delete();
                }
            }
        }
        return folder.delete();
    }

    /**
     * Creates a new directory
     * @param path the path of the directory
     * @return errorcode
     */
    public int createDir(String path){
        File newDir = new File(path);
        if(newDir.exists()){
            return -2;
        }
        if(!newDir.mkdir()){
            return -1;
        }
        return 0;
    }

    @Override
    /**
     * Tests if this Object is equals with o
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientConnection that = (ClientConnection) o;
        return Objects.equals(clientSocket, that.clientSocket) && Objects.equals(handler, that.handler);
    }

    @Override
    /**
     * Generates a hashcode of the ClientConnection
     */
    public int hashCode() {
        return Objects.hash(clientSocket, handler);
    }
}
