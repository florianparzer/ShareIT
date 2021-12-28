package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.*;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        VBox vbox = new VBox();
        HBox hbox1 = new HBox();
        HBox hbox2 = new HBox();
        //ObservableList<String> listFolders = FXCollections.observableArrayList();
        ListView<String> listFolders = new ListView<>();

        TextField uploadFrom = new TextField();
        TextField downloadFrom = new TextField();
        TextField uploadTo = new TextField();
        TextField downloadTo = new TextField();

        Label label1 = new Label("to");
        Label label2 = new Label("to");

        Button btn_up = new Button("Upload");
        Button btn_down = new Button("Download");

        Button selectFileUp = new Button();
        Button selectFolderUp = new Button();
        Button selectFileDown = new Button();
        Button selectFolderDown = new Button();
        Button [] buttons = {selectFileUp, selectFolderUp, selectFileDown, selectFolderDown};

        Image image = new Image(getClass().getResourceAsStream("f.png"));
        ImageView view1 = new ImageView(image);
        ImageView view2 = new ImageView(image);
        ImageView view3 = new ImageView(image);
        ImageView view4 = new ImageView(image);
        ImageView [] views = {view1, view2, view3, view4};

        for(ImageView imageView : views){
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
        }

        int i = 0;
        for(Button button : buttons){
            button.setGraphic(views[i]);
            i++;
        }

        btn_down.setPrefWidth(120);
        btn_up.setPrefWidth(120);

        vbox.getChildren().addAll(listFolders, hbox1, hbox2);
        hbox1.getChildren().addAll(btn_up, uploadFrom, selectFileUp, label1, uploadTo, selectFolderUp);
        hbox2.getChildren().addAll(btn_down, downloadFrom, selectFileDown, label2, downloadTo, selectFolderDown);
        vbox.setSpacing(10);
        hbox1.setSpacing(10);
        hbox2.setSpacing(10);

        vbox.setPadding(new Insets(10));
        hbox1.setPadding(new Insets(10));
        hbox2.setPadding(new Insets(10));

        EventHandler<ActionEvent> popFileWindow = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                File f = fileChooser.showOpenDialog(null);

                if(event.getSource() == selectFileUp){
                    uploadFrom.setText(f.getAbsolutePath());
                }
                else if(event.getSource() == selectFileDown){
                    downloadFrom.setText(f.getAbsolutePath());
                }
                else{
                    //Exceptionhandling
                }
            }
        };

        EventHandler<ActionEvent> popFolderWindow = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File d = directoryChooser.showDialog(null);

                if(event.getSource() == selectFolderDown){
                    downloadTo.setText(d.getAbsolutePath());
                }
                else if(event.getSource() == selectFolderUp){
                    uploadTo.setText(d.getAbsolutePath());
                }

            }
        };

        selectFileUp.setOnAction(popFileWindow);
        selectFolderUp.setOnAction(popFolderWindow);
        selectFileDown.setOnAction(popFileWindow);
        selectFolderDown.setOnAction(popFolderWindow);


        Scene scene = new Scene(vbox, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);



    }
}
