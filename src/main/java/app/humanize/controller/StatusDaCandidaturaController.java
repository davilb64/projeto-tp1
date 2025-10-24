package app.humanize.controller;

import app.humanize.model.Candidato;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class StatusDaCandidaturaController {

    @FXML private TableView<Candidato> tableCandidaturas;
    @FXML private TableColumn<Candidato, String> colCandidato;
    @FXML private TableColumn<Candidato, String> colCargo;
    @FXML private TableColumn<Candidato, String> colStatus;

    private static final ObservableList<Candidato> listaCandidatos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colCandidato.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCargo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getVaga() != null ? cellData.getValue().getVaga().getCargo() : ""
                )
        );
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Em análise") // status fixo por enquanto
        );

        tableCandidaturas.setItems(listaCandidatos);
    }

    public static void adicionarCandidato(Candidato candidato) {
        listaCandidatos.add(candidato);
    }
}
