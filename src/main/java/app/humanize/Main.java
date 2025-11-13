package app.humanize;

import app.humanize.util.ScreenController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        ScreenController.setStage(stage);
        ScreenController.changeScene("/view/LoginView.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

