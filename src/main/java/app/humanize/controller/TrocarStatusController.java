package app.humanize.controller;

import app.humanize.model.Candidatura;
import app.humanize.model.StatusCandidatura;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ResourceBundle;

public class TrocarStatusController {

    @FXML private TextField txtCandidato;
    @FXML private TextField txtVaga;
    @FXML private ChoiceBox<StatusCandidatura> choiceStatus; // Alterado de String para StatusCandidatura
    @FXML private Button btnSalvar;

    private Candidatura candidatura;
    private Runnable onStatusAlterado; // callback para atualizar tabela
    private final CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();
    private ResourceBundle bundle;

    // Método auxiliar para traduzir o status
    private String getTraducaoStatus(StatusCandidatura status) {
        if (status == null) return null;
        String key = "statusCandidatura." + status.name();
        return bundle.containsKey(key) ? bundle.getString(key) : status.name();
    }

    @FXML
    private void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        // Popula o ChoiceBox com os Enums
        choiceStatus.setItems(FXCollections.observableArrayList(
                StatusCandidatura.PENDENTE,
                StatusCandidatura.EM_ANALISE,
                StatusCandidatura.APROVADO,
                StatusCandidatura.REPROVADO
        ));

        // Usa um StringConverter para exibir os nomes traduzidos
        choiceStatus.setConverter(new StringConverter<StatusCandidatura>() {
            @Override
            public String toString(StatusCandidatura status) {
                return getTraducaoStatus(status);
            }

            @Override
            public StatusCandidatura fromString(String string) {
                // Mapeia a string traduzida de volta para o Enum
                if (string == null) return null;
                for (StatusCandidatura s : choiceStatus.getItems()) {
                    if (getTraducaoStatus(s).equals(string)) {
                        return s;
                    }
                }
                return null;
            }
        });
    }

    public void setCandidatura(Candidatura candidatura) {
        this.candidatura = candidatura;
        txtCandidato.setText(candidatura.getCandidato().getNome());
        txtVaga.setText(candidatura.getVaga().getCargo());
        choiceStatus.setValue(candidatura.getStatus()); // Define o Enum diretamente

        txtVaga.setEditable(false);
        txtCandidato.setEditable(false);
    }

    public void setOnStatusAlterado(Runnable callback) {
        this.onStatusAlterado = callback;
    }

    @FXML
    private void salvarCandidato() {
        if (candidatura == null) {
            mostrarErro(bundle.getString("changeStatus.alert.noSelection"));
            return;
        }

        StatusCandidatura novoStatus = choiceStatus.getValue(); // Pega o Enum diretamente
        if (novoStatus == null) {
            mostrarErro(bundle.getString("changeStatus.alert.invalidStatus"));
            return;
        }

        candidatura.setStatus(novoStatus);

        try {
            candidaturaRepository.salvarOuAtualizar(candidatura);
            mostrarInfo(bundle.getString("changeStatus.alert.saveSuccess"));

            if (onStatusAlterado != null)
                onStatusAlterado.run(); // atualiza tabela na tela principal

            fecharJanela();

        } catch (IOException e) {
            mostrarErro(bundle.getString("changeStatus.alert.saveError") + " " + e.getMessage());
        }
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnSalvar.getScene().getWindow();
        stage.close();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("alert.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("alert.success.title"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}