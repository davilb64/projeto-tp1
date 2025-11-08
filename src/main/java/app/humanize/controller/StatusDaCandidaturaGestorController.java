package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.repository.CandidatoRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.model.Candidatura;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

import static app.humanize.model.StatusCandidatura.PENDENTE;

public class StatusDaCandidaturaGestorController {

    @FXML private TableView<Candidatura> tableCandidaturas;
    @FXML private TableColumn<Candidatura, String> colCandidato;
    @FXML private TableColumn<Candidatura, String> colCargo;
    @FXML private TableColumn<Candidatura, String> colStatus;

    private final CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();
    private final ObservableList<Candidatura> listaCandidaturas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colCandidato.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCandidato().getNome()));
        colCargo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVaga().getCargo()));
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStatus().name().replace("_", " ")
                ));

        // 🔹 adiciona apenas candidaturas com status EM_ANALISE ou APROVADO
        listaCandidaturas.addAll(
                candidaturaRepository.getTodas().stream()
                        .filter(c -> c.getStatus() == app.humanize.model.StatusCandidatura.EM_ANALISE
                                || c.getStatus() == app.humanize.model.StatusCandidatura.APROVADO)
                        .toList()
        );

        tableCandidaturas.setItems(listaCandidaturas);
    }




    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
