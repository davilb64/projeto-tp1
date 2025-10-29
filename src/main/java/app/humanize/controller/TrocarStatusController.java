package app.humanize.controller;

import app.humanize.model.Candidatura;
import app.humanize.model.StatusCandidatura;
import app.humanize.repository.CandidaturaRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class TrocarStatusController {

    @FXML private TextField txtCandidato;
    @FXML private TextField txtVaga;
    @FXML private ChoiceBox<String> choiceStatus;
    @FXML private Button btnSalvar;

    private Candidatura candidatura;
    private Runnable onStatusAlterado; // callback para atualizar tabela
    private final CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();

    @FXML
    private void initialize() {
        choiceStatus.setItems(FXCollections.observableArrayList(
                "PENDENTE", "EM_ANALISE", "APROVADO", "REPROVADO"
        ));
    }

    public void setCandidatura(Candidatura candidatura) {
        this.candidatura = candidatura;
        txtCandidato.setText(candidatura.getCandidato().getNome());
        txtVaga.setText(candidatura.getVaga().getCargo());
        choiceStatus.setValue(candidatura.getStatus().name());

        txtVaga.setEditable(false);
        txtCandidato.setEditable(false);
    }

    public void setOnStatusAlterado(Runnable callback) {
        this.onStatusAlterado = callback;
    }

    @FXML
    private void salvarCandidato() {
        if (candidatura == null) {
            mostrarErro("Nenhuma candidatura selecionada.");
            return;
        }

        String novoStatus = choiceStatus.getValue();
        if (novoStatus == null || novoStatus.isEmpty()) {
            mostrarErro("Selecione um status válido.");
            return;
        }

        candidatura.setStatus(StatusCandidatura.valueOf(novoStatus));

        try {
            candidaturaRepository.salvarOuAtualizar(candidatura);
            mostrarInfo("Status atualizado com sucesso!");

            if (onStatusAlterado != null)
                onStatusAlterado.run(); // atualiza tabela na tela principal

            fecharJanela();

        } catch (IOException e) {
            mostrarErro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnSalvar.getScene().getWindow();
        stage.close();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
