package com.dcroyal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * DC Royal Printing — Desktop Management System
 * Main entry point of the application
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Load the Login screen first
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/dcroyal/view/login.fxml")
        );

        // Create scene with login screen (900x600 size)
        Scene scene = new Scene(loader.load(), 900, 600);

        // Apply the CSS stylesheet to the scene
        scene.getStylesheets().add(
                Main.class.getResource("/com/dcroyal/css/style.css").toExternalForm()
        );

        // Set window title and show
        stage.setTitle("DC Royal Printing — Staff Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
