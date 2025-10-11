package app.humanize.controller;

import app.humanize.util.ScreenController;
import javafx.fxml.FXML;

public class LoginController {
    @FXML
    private void entrarAdm() {
        ScreenController.changeScene("/view/telaPrincipalAdministrador.fxml");
    }
    @FXML
    private void entrarGestor() {
        ScreenController.changeScene("/view/TelaPrincipalGestor.fxml");
    }
    @FXML
    private void entrarRecrutador() {
        ScreenController.changeScene("/view/TelaPrincipalRecrutador.fxml");
    }
    @FXML
    private void entrarFuncionario() {
        ScreenController.changeScene("/view/TelaPrincipalFuncionario.fxml");
    }

}
