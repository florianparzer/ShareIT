import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;


import java.io.File;

public class Main extends Application{

    /**
     * Starts log in interface for the user.
     * @param primaryStage the Stage to visualize
     */
    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Share_IT");
        primaryStage.centerOnScreen();
        primaryStage.setMinHeight(300);
        primaryStage.setMinWidth(300);

        Image image = new Image(new File("src/main/resources/logo.png").toURI().toString());
        ImageView logoview = new ImageView(image);

        TextField ipAndPortTextfield = new TextField();
        Label label = new Label("IP:Port -> ");

        Button connectButton = new Button("Connect");
        connectButton.setPrefWidth(120);

        logoview.setX(50);
        logoview.setY(25);
        logoview.setFitHeight(200);
        logoview.setFitWidth(200);
        logoview.setPreserveRatio(true);

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);

        vbox.setSpacing(10);
        hbox.setSpacing(10);
        vbox.setPadding(new Insets(10));
        hbox.setPadding(new Insets(10));

        vbox.getChildren().addAll(logoview, hbox, connectButton);
        hbox.getChildren().addAll(label, ipAndPortTextfield);
        Scene loginScene = new Scene(vbox, 300, 300);
        primaryStage.setScene(loginScene);
        primaryStage.show();



        EventHandler<ActionEvent> connectToServer = new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                try{
                    String ip = ipAndPortTextfield.getText().split(":")[0];
                    int port = Integer.parseInt(ipAndPortTextfield.getText().split(":")[1]);
                    ClientGui clientGui = new ClientGui(ip, port);
                    clientGui.startFilesharing(primaryStage);
                }
                catch(Exception e){
                    e.printStackTrace();
                    ClientGui.errorPopup("Connection could not be established");
                }
            }
        };


        connectButton.setOnAction(connectToServer);


    }

    public static void main(String[] args) {
        launch(args);



    }
}
