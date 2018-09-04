package com;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("myTools.fxml"));
        Scene scene = new Scene(parent, 846, 584);
        primaryStage.setScene(scene);
//         set icon
        URL resource = getClass().getResource("tool.png");
        Image img = new Image(String.valueOf(resource));
        primaryStage.getIcons().add(img);
        // set title
        primaryStage.setTitle("Tools");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
