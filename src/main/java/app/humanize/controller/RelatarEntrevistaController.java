package app.humanize.controller;

import app.humanize.model.*;
import app.humanize.repository.*;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ResourceBundle;

public class RelatarEntrevistaController {

    @FXML
    private TextArea txtRelatorioEntrevista;
    @FXML
    private ChoiceBox<StatusEntrevista> cbStatus;
    @FXML
    private Button btnCancelar;

    private final EntrevistaRepository entrevistaRepository = EntrevistaRepository.getInstance();
    private final CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();

    private Entrevista entrevistaParaEditar;
    private ResourceBundle bundle;

    @FXML
    private void initialize() {
        this.bundle = UserSession.getInstance().getBundle();
        cbStatus.setItems(FXCollections.observableArrayList(StatusEntrevista.values()));
    }

    public void prepararParaEdicao(Entrevista entrevista) {
        this.entrevistaParaEditar = entrevista;
        txtRelatorioEntrevista.setText(entrevista.getRelatorioEntrevista());
        cbStatus.setValue(entrevista.getStatus());
    }

    private boolean validarCampos() {
        if (txtRelatorioEntrevista.getText() == null) {
            mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.title"), bundle.getString("scheduleInterview.alert.validation.candidate"), null, Alert.AlertType.WARNING);
            return false;
        }
        if (cbStatus.getValue() == null) {
            mostrarAlerta(bundle.getString("scheduleInterview.alert.validation.title"), bundle.getString("scheduleInterview.alert.validation.job"), null, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    @FXML
    private void salvarEntrevista() {
        if (!validarCampos()) {
            return;
        }
        if(this.entrevistaParaEditar != null) {
            try{
                Candidatura candidatura = entrevistaParaEditar.getCandidatura();
                entrevistaParaEditar.setRelatorioEntrevista(txtRelatorioEntrevista.getText());
                entrevistaParaEditar.setStatus(cbStatus.getValue());
                entrevistaRepository.atualizarEntrevista();
                //muda o status da candidatura
                if(cbStatus.getValue().equals(StatusEntrevista.Aprovado)){
                    candidatura.setStatus(StatusCandidatura.APROVADO);
                }
                if(cbStatus.getValue().equals(StatusEntrevista.Reprovado)){
                    candidatura.setStatus(StatusCandidatura.REPROVADO);
                }
                candidaturaRepository.salvarOuAtualizar(candidatura);
            }catch (Exception e){
                mostrarAlerta(
                        bundle.getString("scheduleInterview.alert.unexpectedError.title"),
                        bundle.getString("scheduleInterview.alert.unexpectedError.header"),
                        e.getMessage(),
                        Alert.AlertType.ERROR
                );
            }
        }
        fecharJanela();
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensagem, String detalhe, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titulo);
        alert.setHeaderText(mensagem);
        if (detalhe != null && !detalhe.isEmpty()) {
            alert.setContentText(detalhe);
        }
        alert.showAndWait();
    }
}
