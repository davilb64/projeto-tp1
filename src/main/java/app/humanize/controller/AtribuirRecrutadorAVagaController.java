package app.humanize.controller;

import app.humanize.model.Vaga;
import app.humanize.repository.VagaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class AtribuirRecrutadorAVagaController {
    @FXML private ComboBox<Vaga> comboVaga;

    VagaRepository vagaRepository = VagaRepository.getInstance();

    @FXML
    private void initialize(){
        comboVaga.getItems().addAll(vagaRepository.getTodasVagas());
    }
}
