package org.example.uet_library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.uet_library.Controllers.UserHomeController;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/Login.fxml"));
        Parent root = loader.load();
//        UserHomeController userHomeController = loader.getController();
//        userHomeController.setPrimaryStage(primaryStage);
        Font.loadFont(
            getClass().getResource("/Fonts/BlackOpsOne-Regular.ttf").toExternalForm(),
            14
        );
        Font.loadFont(
            getClass().getResource("/Fonts/Alata-Regular.ttf").toExternalForm(),
            14
        );
        Font.loadFont(
            getClass().getResource("/Fonts/blackadder-itc/BITCBLKAD.ttf").toExternalForm(),
            14
        );
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
