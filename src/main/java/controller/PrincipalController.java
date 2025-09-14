package controller;

import javafx.fxml.FXML;
import util.ScreenController;

public class PrincipalController {
    @FXML
    private void sair() {
        ScreenController.changeScene("/view/LoginView.fxml");
    }
}
