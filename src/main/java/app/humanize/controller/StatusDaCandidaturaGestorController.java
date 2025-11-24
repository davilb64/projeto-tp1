package app.humanize.controller;

import app.humanize.model.Candidatura;
import app.humanize.model.StatusCandidatura;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ResourceBundle;


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

    private String getTraducaoStatus(StatusCandidatura status) {
        if (status == null) return "";
        String key = "statusCandidatura." + status.name();
        return bundle.containsKey(key) ? bundle.getString(key) : status.name().replace("_", " ");
    }

    @FXML
    private void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        colCandidato.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCandidato().getNome()));
        colCargo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVaga().getCargo()));

        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        getTraducaoStatus(cellData.getValue().getStatus())
                ));

        listaCandidaturas.addAll(
                candidaturaRepository.getTodas().stream()
                        .filter(c -> c.getStatus() == app.humanize.model.StatusCandidatura.EM_ANALISE
                                || c.getStatus() == app.humanize.model.StatusCandidatura.APROVADO)
                        .toList()
        );

        tableCandidaturas.setItems(listaCandidaturas);

        // chama o filtro ao clicar no botão
        btnFiltrar.setOnAction(e -> filtrarCandidaturas());
    }

    private void filtrarCandidaturas() {
        String filtro = txtFiltro.getText().toLowerCase().trim();

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

}