package controller;

import util.ScreenController;
import javafx.fxml.FXML;

public class LoginController {
    @FXML
    private void entrar() {
        ScreenController.changeScene("/view/telaPrincipalAdministrador.fxml");
    }

}
