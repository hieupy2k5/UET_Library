package org.example.uet_library;

import java.sql.Connection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.uet_library.controllers.LogInController;
import org.example.uet_library.database.Database;

public class App extends Application {
    Connection connection = new Database().getConnection();

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/LogIn.fxml"));
        Scene scene = new Scene(loader.load());

        LogInController logInController = loader.getController();
        logInController.setStage(primaryStage);

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
        Font.loadFont(
                getClass().getResource("/Fonts/PottaOne-Regular.ttf").toExternalForm(), 14
        );

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
