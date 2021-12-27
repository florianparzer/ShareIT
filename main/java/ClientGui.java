import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientGui extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("TCP_Client");
        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane, 300, 275);
        primaryStage.setScene(scene);
        TCP_Client client = new TCP_Client("127.0.0.1", 1234);

        TextArea input = new TextArea();
        pane.setCenter(input);
        VBox buttons = new VBox();
        pane.setLeft(buttons);
        Button sendInput = new Button("Send");
        Button exitClient = new Button("Close");
        buttons.getChildren().add(sendInput);
        buttons.getChildren().add(exitClient);
        primaryStage.show();

        /*
        //DUMMY_CODE
        sendInput.setOnAction((a) -> {
            try {
                client.write(input.getText());
                input.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        exitClient.setOnAction((a) -> {
            try {
                client.close();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
         */

    }

    public static void main(String[] args) {
        launch(args);
    }
}