package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.model.Candidatura;
import app.humanize.model.StatusCandidatura;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

import static app.humanize.model.StatusCandidatura.PENDENTE;

public class StatusDaCandidaturaGestorController {

    @FXML private TableView<Candidatura> tableCandidaturas;
    @FXML private TableColumn<Candidatura, String> colCandidato;
    @FXML private TableColumn<Candidatura, String> colCargo;
    @FXML private TableColumn<Candidatura, String> colStatus;
    @FXML private TextField txtFiltro;
    @FXML private Button btnFiltrar;

    private final CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();
    private final ObservableList<Candidatura> listaCandidaturas = FXCollections.observableArrayList();

    private ResourceBundle bundle;

    // Método auxiliar para traduzir o status
    private String getTraducaoStatus(StatusCandidatura status) {
        if (status == null) return "";
        // Busca a chave, ex: "statusCandidatura.EM_ANALISE"
        String key = "statusCandidatura." + status.name();
        // Retorna a tradução se existir, senão o nome do enum formatado
        return bundle.containsKey(key) ? bundle.getString(key) : status.name().replace("_", " ");
    }

    @FXML
    private void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        colCandidato.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCandidato().getNome()));
        colCargo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVaga().getCargo()));

        // Coluna de Status agora usa o método de tradução
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        getTraducaoStatus(cellData.getValue().getStatus())
                ));

        // 🔹 adiciona apenas candidaturas com status EM_ANALISE ou APROVADO
        listaCandidaturas.addAll(
                candidaturaRepository.getTodas().stream()
                        .filter(c -> c.getStatus() == app.humanize.model.StatusCandidatura.EM_ANALISE
                                || c.getStatus() == app.humanize.model.StatusCandidatura.APROVADO)
                        .toList()
        );

        tableCandidaturas.setItems(listaCandidaturas);

        // Chama o filtro ao clicar no botão
        btnFiltrar.setOnAction(e -> filtrarCandidaturas());
    }

    private void filtrarCandidaturas() {
        String filtro = txtFiltro.getText().toLowerCase().trim();

        // 🔹 sempre parte apenas das candidaturas EM_ANALISE ou APROVADAS
        var baseFiltrada = candidaturaRepository.getTodas().stream()
                .filter(c -> c.getStatus() == StatusCandidatura.EM_ANALISE
                        || c.getStatus() == StatusCandidatura.APROVADO)
                .toList();

        if (filtro.isEmpty()) {
            tableCandidaturas.setItems(FXCollections.observableArrayList(baseFiltrada));
            return;
        }

        ObservableList<Candidatura> filtradas = FXCollections.observableArrayList(
                baseFiltrada.stream()
                        .filter(c -> {
                            String nome = c.getCandidato().getNome().toLowerCase();
                            String cargo = c.getVaga().getCargo().toLowerCase();
                            String status = getTraducaoStatus(c.getStatus()).toLowerCase();
                            return nome.contains(filtro) || cargo.contains(filtro) || status.contains(filtro);
                        })
                        .toList()
        );

        tableCandidaturas.setItems(filtradas);
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(bundle.getString("userManagement.alert.attention"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}