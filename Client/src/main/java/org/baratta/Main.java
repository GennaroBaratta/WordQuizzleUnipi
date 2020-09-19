package org.baratta;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.baratta.logic.WordQuizzleClient;

public class Main extends Application {
    Router router;
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new StackPane());
        router = new Router(scene);
        router.showLogin();
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Hello World");
    }
    @Override
    public  void stop() throws Exception{
        WordQuizzleClient.getInstance().logout(router.getUsername());
        System.exit(0);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
