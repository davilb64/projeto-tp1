package app.humanize.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ScreenController {
    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void changeScene(String fxmlPath) {
        Image image = new Image("/humanize-logo.png");
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(ScreenController.class.getResource(fxmlPath)));
            stage.setTitle("Humanize");
            stage.getIcons().add(image);
            stage.setScene(new Scene(root, 800, 650));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
