package app.humanize.controller;

import app.humanize.model.Usuario;
import app.humanize.model.Vaga;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class AtribuirRecrutadorAVagaController {
    @FXML private ComboBox<Vaga> comboVaga;
    @FXML private ComboBox<Usuario> comboRecrutador;

    VagaRepository vagaRepository = VagaRepository.getInstance();
    UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();

    @FXML
    private void initialize(){
        comboRecrutador.getItems().addAll(usuarioRepository.getRecrutadores());
        comboVaga.getItems().addAll(vagaRepository.getTodasVagas());
    }
}
