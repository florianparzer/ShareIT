import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

public class ClientGui {
    private TCP_Client tcp_client;
    private String path = "/";
    private Label currentPath;

    /**
     * Constructer of the GUI
     * @param ip takes in IP to construct a TCP_Client
     * @param port takes in port to construct a TCP_Client
     * @throws IOException
     */
    public ClientGui(String ip, int port) throws IOException{
            tcp_client = new TCP_Client(ip, port);
    }

    /**
     * Converts a file into a string
     * @param file the file to convert
     * @return it returns a String with the complete content of the file
     * @throws FileNotFoundException
     */
    public static String fileToString(File file) throws FileNotFoundException{
        String fullstring = null;
        StringBuffer sb = new StringBuffer();
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()){
            fullstring = scanner.nextLine();
            sb.append(fullstring);
        }
        return sb.toString();
    }

    /**
     * If a restricted behaviour shows up, an error window with a message pops up
     * @param message the error message of the pop up window
     */
    public static void errorPopup(String message){
        Stage window = new Stage();
        window.centerOnScreen();
        window.setMinHeight(150);
        window.setMinWidth(250);

        window.initModality(Modality.APPLICATION_MODAL); // Block other windows, until this one finished
        window.setTitle("Error");

        Label errorMessage = new Label(message);
        errorMessage.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        errorMessage.setTextFill(Color.RED);
        Button btnClose = new Button("close");
        btnClose.setOnAction(event -> window.close());

        VBox vBox = new VBox();
        vBox.getChildren().addAll(errorMessage, btnClose);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));


        Scene scene = new Scene(vBox);
        window.setScene(scene);

        window.showAndWait();

    }

    /**
     * Takes in a new name from the user for the rename or create folder operation
     * @param addfolder if a new folder should be created, then this flag has to be true
     * @return String with the selected name from the user
     */
    public String renamePopUp(boolean addfolder){
        Stage window = new Stage();
        window.centerOnScreen();
        window.setMinHeight(150);
        window.setMinWidth(250);

        window.initModality(Modality.APPLICATION_MODAL); // Block other windows, until this one finished
        TextField newName = new TextField();
        Button btnConfirm = new Button("OK");
        btnConfirm.setOnAction(event -> window.close());
        if(addfolder){
            window.setTitle("Add Folder");
        }
        else{
            window.setTitle("Rename");
            newName.setText(currentPath.getText());
        }

        VBox vBox = new VBox();

        vBox.getChildren().addAll(newName, btnConfirm);
        vBox.setAlignment(Pos.CENTER);

        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);

        Scene scene = new Scene(vBox);
        window.setScene(scene);

        window.showAndWait();
        String temp = newName.getText();
        while(true) {
            if (temp.length() == 0 || temp.endsWith("/")) {
                window.close();
                return null;
            }
            if(temp.contains(" ")){
                errorPopup("No space allowed!");
                window.showAndWait();
                temp = newName.getText();
            }
            else{
                break;
            }
        }

        return temp;
    }

    /**
     * Lists all elements on the server in a folder structure with buttons
     * @param filelist the VBox to add the elements to
     */
    public void createGUIElements(VBox filelist){
        filelist.getChildren().clear();
        String input = tcp_client.listContent(path);
        if(input == null){
            errorPopup("Could not list Content");
            return;
        }
        input = input.substring(0, input.length()-1);
        boolean isFile;
        for(String element : input.split(" ")){
            isFile = element.startsWith("f");
            element = element.split(":")[1];
            HBox item = new HBox();
            Label name = new Label(element);

            Button btn_Rename = new Button("rename");
            Button btn_Delete = new Button("delete");
            Button btn_Fav = new Button();
            btn_Fav.setPrefWidth(33);

            Image image_Star = new Image(new File("src/main/resources/star.png").toURI().toString());
            ImageView view_Star = new ImageView(image_Star);
            view_Star.setFitHeight(15);
            view_Star.setFitWidth(15);

            name.setMaxWidth(Double.MAX_VALUE);
            item.setHgrow(name, Priority.ALWAYS);

            item.setSpacing(10);
            item.setPadding(new Insets(8, 8, 8, 15));
            filelist.getChildren().add(item);

            //check if files are marked as favorite
            try {
                String s = fileToString(new File("src/main/resources/Favorite_Files.txt"));
                if(s.contains(name.getText())){
                    btn_Fav.setGraphic(view_Star);
                }
                else{
                    btn_Fav.setGraphic(null);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            EventHandler<ActionEvent> rename = new EventHandler<>() {
                @Override
                public void handle(ActionEvent event) {
                    String newName = renamePopUp(false);
                    if(newName != null){
                        if(tcp_client.rename(path + name.getText(), newName) == 0) {
                            //für verschieben ändern auf: tcp_client.rename(path + name.getText(), newName);
                            createGUIElements(filelist);
                        }else {
                            errorPopup("Could not rename file");
                        }
                    }
                }
            };

            EventHandler<ActionEvent> delete = new EventHandler<>() {
                @Override
                public void handle(ActionEvent event) {
                    if(tcp_client.delete(path + name.getText()) == 0) {
                        createGUIElements(filelist);
                    }else{
                        errorPopup("Error with Deletion");
                    }
                }
            };

            EventHandler<ActionEvent> markAsFav = new EventHandler<>() {
                @Override
                public void handle(ActionEvent event) {
                    String selectedFile = name.getText();
                    String favFiles;
                    File textFile = new File("src/main/resources/Favorite_Files.txt");

                    try {
                        favFiles = fileToString(textFile);
                        PrintWriter writer = new PrintWriter(textFile);
                        if(favFiles.contains(selectedFile)){
                            favFiles = favFiles.replaceAll(selectedFile, "");

                            writer.append(favFiles);
                            writer.flush();
                            btn_Fav.setGraphic(null);
                        }
                        else{
                            favFiles = favFiles.concat(selectedFile);
                            writer.append(favFiles);
                            writer.flush();
                            btn_Fav.setGraphic(view_Star);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            };

            btn_Delete.setOnAction(delete);
            btn_Rename.setOnAction(rename);
            btn_Fav.setOnAction(markAsFav);

            if(isFile){
                EventHandler<ActionEvent> download = new EventHandler<>() {
                    @Override
                    public void handle(ActionEvent event) {
                        DirectoryChooser dirChooser = new DirectoryChooser();
                        File f = dirChooser.showDialog(null);
                        if(f != null){
                            tcp_client.downloadFile(f.getPath() + "\\" + name.getText(), path + name.getText());
                            createGUIElements(filelist);
                        }
                    }
                };
                Button btn_Download = new Button("download");
                item.getChildren().addAll(name, btn_Download, btn_Rename, btn_Delete, btn_Fav);
                btn_Download.setOnAction(download);
            }
            else{
                name.setTextFill(Color.DARKBLUE);
                name.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
                EventHandler<ActionEvent> enter = new EventHandler<>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try{
                            path = path + name.getText() + "/";
                            currentPath.setText(path);
                            createGUIElements(filelist);

                        }catch(ArrayIndexOutOfBoundsException e){

                        }
                    }
                };
                Button btn_Enter = new Button("enter");
                btn_Enter.setPrefWidth(70);
                item.getChildren().addAll(name, btn_Enter, btn_Rename, btn_Delete);
                btn_Enter.setOnAction(enter);
            }
        }

    }

    /**
     * Starts the file sharing GUI, after successful log in
     * @param primaryStage the stage to show
     */
    public void startFilesharing(Stage primaryStage){
        primaryStage.setTitle("Share_IT");
        primaryStage.centerOnScreen();
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(650);

        VBox vbox = new VBox();
        HBox hbox0 = new HBox();
        HBox hbox1 = new HBox();
        HBox hbox2 = new HBox();
        HBox hbox3 = new HBox();
        VBox filelist = new VBox();
        hbox3.setAlignment(Pos.CENTER);
        filelist.setAlignment(Pos.CENTER);


        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(filelist);
        scrollPane.setPannable(true); // it means that the user should be able to pan the viewport by using the mouse.

        TextField uploadFrom = new TextField();

        currentPath = new Label(path);
        currentPath.setFont(Font.font("Verdana", FontWeight.BOLD, 13));

        Button btn_upload = new Button("Upload");
        Button btn_disconnect = new Button("Disconnect");
        Button btn_back = new Button();
        Button btn_refresh = new Button();
        Button btn_selectFile = new Button();
        Button btn_createFolder = new Button("Create Directory");

        Image image_folder = new Image(new File("src/main/resources/f.png").toURI().toString());
        ImageView view_folder = new ImageView(image_folder);
        view_folder.setFitHeight(20);
        view_folder.setFitWidth(20);

        Image image_refresh = new Image(new File("src/main/resources/refresh.png").toURI().toString());
        ImageView view_refresh = new ImageView(image_refresh);
        view_refresh.setFitHeight(20);
        view_refresh.setFitWidth(20);

        Image image_back = new Image(new File("src/main/resources/back.png").toURI().toString());
        ImageView view_back = new ImageView(image_back);
        view_back.setFitHeight(20);
        view_back.setFitWidth(20);

        btn_selectFile.setGraphic(view_folder);
        btn_back.setGraphic(view_back);
        btn_refresh.setGraphic(view_refresh);
        btn_upload.setPrefWidth(120);
        btn_disconnect.setPrefWidth(120);

        vbox.getChildren().addAll(hbox0, scrollPane, hbox1, hbox2, hbox3);
        hbox0.getChildren().addAll(btn_back, btn_refresh, currentPath);
        hbox1.getChildren().addAll(btn_upload, uploadFrom, btn_selectFile, btn_createFolder);
        hbox3.getChildren().add(btn_disconnect);
        vbox.setSpacing(10);
        hbox0.setSpacing(20);
        hbox1.setSpacing(10);
        hbox2.setSpacing(10);
        hbox3.setSpacing(10);

        vbox.setPadding(new Insets(10));
        hbox0.setPadding(new Insets(10));
        hbox1.setPadding(new Insets(10));
        hbox2.setPadding(new Insets(10));
        hbox3.setPadding(new Insets(10));

        EventHandler<ActionEvent> popFileWindow = new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                uploadFrom.clear();
                FileChooser fileChooser = new FileChooser();
                File f = fileChooser.showOpenDialog(null);
                if(f != null){
                    uploadFrom.setText(f.getAbsolutePath());
                }
            }
        };


        EventHandler<ActionEvent> disconnectFromServer = new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    tcp_client.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
                primaryStage.close();
                System.exit(0);
            }
        };

        EventHandler<ActionEvent> stepBack = new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                if(path.length() != 1){
                    path = path.substring(0, path.lastIndexOf("/", path.length()-2) + 1);
                    currentPath.setText(path);
                    createGUIElements(filelist);
                }
            }
        };

        EventHandler<ActionEvent> makeRefresh = new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                createGUIElements(filelist);
            }
        };

        EventHandler<ActionEvent> addFolder = new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                String foldername = renamePopUp(true);
                if(foldername != null){
                    int result = tcp_client.createDir(path+foldername);
                    if(result == -1){
                        errorPopup("Internal Error occurred");
                    }else if(result == -2){
                        errorPopup("Already exists");
                    }
                    createGUIElements(filelist);
                }
            }
        };


        EventHandler<ActionEvent> doUpload = new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                String localpath = uploadFrom.getText();
                localpath = localpath.replaceAll("\\\\", "/");
                if(localpath.contains(" ")){
                    errorPopup("No space allowed!");
                }
                else if(localpath != null){

                    int i = localpath.lastIndexOf("/");
                    String remotePath = path + localpath.substring(i + 1);

                    tcp_client.uploadFile(localpath, remotePath);
                    createGUIElements(filelist);

                    uploadFrom.clear();
                    event.consume();
                }
            }
        };

        //drag and drop
        scrollPane.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if(event.getDragboard().hasFiles()){
                    event.acceptTransferModes(TransferMode.ANY); // + symbol, -> for accepting the data
                }
                event.consume();
            }
        });

        scrollPane.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                String file = event.getDragboard().getUrl();
                file = file.substring(file.indexOf("/") + 1);
                if(!file.contains(" ")){
                    int i = file.lastIndexOf("/");
                    String remotePath = path + file.substring(i + 1);
                    tcp_client.uploadFile(file, remotePath);
                    createGUIElements(filelist);
                    event.consume();
                }
            }
        });

        btn_upload.setOnAction(doUpload);
        btn_refresh.setOnAction(makeRefresh);
        btn_selectFile.setOnAction(popFileWindow);
        btn_back.setOnAction(stepBack);
        btn_disconnect.setOnAction(disconnectFromServer);
        btn_createFolder.setOnAction(addFolder);

        createGUIElements(filelist);
        Scene guiScene = new Scene(vbox, 500, 600);
        primaryStage.setScene(guiScene);
        primaryStage.show();

    }






}
