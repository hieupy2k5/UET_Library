package org.example.uet_library;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class TestHelper extends Application {
    private static final Object LOCK = new Object();
    private static boolean isToolkitInitialized = false;

    @Override
    public void start(Stage primaryStage) {
    }

    public static void initToolkit() {
        synchronized (LOCK) {
            if (!isToolkitInitialized) {
                new Thread(() -> Application.launch(TestHelper.class)).start();
                isToolkitInitialized = true;
            }
        }
    }

    public static void runLater(Runnable task) {
        Platform.runLater(task);
    }
}
