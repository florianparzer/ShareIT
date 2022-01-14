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
import java.io.IOException;
import java.util.List;

public class ClientGui {
    private TCP_Client tcp_client;
    private String path = "/";
    private Label currentPath;

    public ClientGui(String ip, int port) throws IOException{
        tcp_client = new TCP_Client(ip, port);
    }



    public void errorPopup(String message){
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

    public String renamePopUp(){
        Stage window = new Stage();
        window.centerOnScreen();
        window.setMinHeight(150);
        window.setMinWidth(250);

        window.initModality(Modality.APPLICATION_MODAL); // Block other windows, until this one finished
        window.setTitle("Rename");


        TextField newName = new TextField();
        Button btnConfirm = new Button("OK");
        btnConfirm.setOnAction(event -> window.close());

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
            if (temp.length() == 0) {
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

        System.out.println(temp.length() + " . " + temp);
        return temp;

    }

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

            Button btnRename = new Button("rename");
            Button btnDelete = new Button("delete");
            name.setMaxWidth(Double.MAX_VALUE);
            //item.setHgrow(name, Priority.ALWAYS);

            item.setSpacing(10);

            filelist.getChildren().add(item);


            EventHandler<ActionEvent> rename = new EventHandler<>() {
                @Override
                public void handle(ActionEvent event) {
                    String newName = renamePopUp();
                    if(newName == null){
                        return;
                    }
                    if(tcp_client.rename(path + name.getText(), path + newName) == 0) {
                        //für verschieben ändern auf: tcp_client.rename(path + name.getText(), newName);
                        createGUIElements(filelist);
                    }else {
                        errorPopup("Could not rename file");
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

            btnDelete.setOnAction(delete);
            btnRename.setOnAction(rename);

            if(isFile){
                EventHandler<ActionEvent> download = new EventHandler<>() {
                    @Override
                    public void handle(ActionEvent event) {

                    }
                };
                Button btnDownload = new Button("download");
                item.getChildren().addAll(name, btnDownload, btnRename, btnDelete);
                btnDownload.setOnAction(download);
            }
            else{
                name.setTextFill(Color.DARKBLUE);
                EventHandler<ActionEvent> enter = new EventHandler<>() {
                    @Override
                    public void handle(ActionEvent event) {
                        path = path + name.getText() + "/";
                        currentPath.setText(path);
                        createGUIElements(filelist);
                    }
                };
                Button btnEnter = new Button("enter");
                item.getChildren().addAll(name, btnEnter, btnRename, btnDelete);
                btnEnter.setOnAction(enter);
            }
        }




    }

    public void startFilesharing(Stage primaryStage){
        primaryStage.setTitle("Share_IT");
        primaryStage.centerOnScreen();
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(650);

        VBox vbox = new VBox();
        HBox hbox = new HBox();
        HBox hbox1 = new HBox();
        HBox hbox2 = new HBox();
        HBox hbox3 = new HBox();
        hbox3.setAlignment(Pos.CENTER);
        VBox filelist = new VBox();
        filelist.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(filelist);
        scrollPane.setPannable(true); // it means that the user should be able to pan the viewport by using the mouse.

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
                System.out.println(file);
                // String bearbeiten, leerzeichen mit unterstrich ersetzen, kein slash, backslash etc
                int i = file.lastIndexOf("/");
                String remotePath = path + file.substring(i + 1, file.length());
                // auf der GUI anpassen, createlements funkiton
                tcp_client.uploadFile(file, remotePath);
                createGUIElements(filelist);


            }
        });

        TextField uploadFrom = new TextField();

        currentPath = new Label(path);
        currentPath.setFont(Font.font("Verdana", FontWeight.BOLD, 13));

        Button btn_up = new Button("Upload");
        Button btn_disconnect = new Button("Disconnect");
        Button btn_back = new Button("<-");

        Button selectFileUp = new Button();
        //Button [] buttons = {selectFileUp, selectFolderUp, selectFileDown, selectFolderDown};

        Image image = new Image(new File("src/main/resources/f.png").toURI().toString());
        ImageView view1 = new ImageView(image);
        view1.setFitHeight(20);
        view1.setFitWidth(20);
        /*ImageView view2 = new ImageView(image);
        ImageView view3 = new ImageView(image);
        ImageView view4 = new ImageView(image);
        ImageView [] views = {view1, view2, view3, view4};


        for(ImageView imageView : views){
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
        }
        */

        /*
        int i = 0;
        for(Button button : buttons){
            button.setGraphic(views[i]);
            i++;
        }
         */
        selectFileUp.setGraphic(view1);

        btn_up.setPrefWidth(120);
        btn_disconnect.setPrefWidth(120);

        vbox.getChildren().addAll(hbox, scrollPane, hbox1, hbox2, hbox3);
        hbox.getChildren().addAll(btn_back, currentPath);
        hbox1.getChildren().addAll(btn_up, uploadFrom, selectFileUp);
        //hbox2.getChildren().addAll(btn_down, downloadFrom, selectFileDown, label2, downloadTo, selectFolderDown);
        hbox3.getChildren().add(btn_disconnect);
        vbox.setSpacing(10);
        hbox.setSpacing(20);
        hbox1.setSpacing(10);
        hbox2.setSpacing(10);
        hbox3.setSpacing(10);

        vbox.setPadding(new Insets(10));
        hbox.setPadding(new Insets(10));
        hbox1.setPadding(new Insets(10));
        hbox2.setPadding(new Insets(10));
        hbox3.setPadding(new Insets(10));

        EventHandler<ActionEvent> popFileWindow = new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                File f = fileChooser.showOpenDialog(null);

                try{
                    uploadFrom.setText(f.getAbsolutePath());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        /*
        EventHandler<ActionEvent> popFolderWindow = new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File d = directoryChooser.showDialog(null);
                try{
                    if(event.getSource() == selectFolderDown){
                        downloadTo.setText(d.getAbsolutePath());
                    }
                    else if(event.getSource() == selectFolderUp){
                        uploadTo.setText(d.getAbsolutePath());
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
         */

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

        selectFileUp.setOnAction(popFileWindow);
        /*selectFolderUp.setOnAction(popFolderWindow);
        selectFileDown.setOnAction(popFileWindow);
        selectFolderDown.setOnAction(popFolderWindow);
         */
        btn_back.setOnAction(stepBack);
        btn_disconnect.setOnAction(disconnectFromServer);

        createGUIElements(filelist);
        Scene guiScene = new Scene(vbox, 500, 600);
        primaryStage.setScene(guiScene);
        primaryStage.show();

    }






}
