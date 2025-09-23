package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleGroup;

public class ConfiguracoesAdmController {
    public ToggleGroup tema;
    @FXML
    private ComboBox<String> idiomaCombo;

    @FXML
    private ComboBox<String> fusoCombo;

    @FXML
    public void initialize() {
        idiomaCombo.getItems().addAll("Português", "Inglês", "Espanhol");
        idiomaCombo.setValue("Português");

        fusoCombo.getItems().addAll("GMT-3", "GMT-5", "UTC");
        fusoCombo.setValue("GMT-3");
    }

}
