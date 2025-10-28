package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.repository.CandidatoRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import app.humanize.repository.CandidaturaRepository;
import app.humanize.model.Candidatura;

import java.io.IOException;

public class StatusDaCandidaturaController {

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
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().name().replace("_", " ")));

        listaCandidaturas.addAll(candidaturaRepository.getTodas());
        tableCandidaturas.setItems(listaCandidaturas);
    }


    private CandidatosAdmController controllerPai;

    public void setControllerPai(CandidatosAdmController controllerPai) {
        this.controllerPai = controllerPai;
    }

    private final CandidatoRepository candidatoRepository = CandidatoRepository.getInstance();
    private final ObservableList<Candidato> listaCandidatos = FXCollections.observableArrayList();


    public static void adicionarCandidato(Candidato candidato) {
        try {
            CandidatoRepository.getInstance().adicionar(candidato);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void excluirCandidato() {
        Candidatura candidaturaSelecionada = tableCandidaturas.getSelectionModel().getSelectedItem();

        if (candidaturaSelecionada == null) {
            mostrarAlerta("Selecione uma candidatura para excluir!");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente excluir a candidatura de " +
                        candidaturaSelecionada.getCandidato().getNome() +
                        " para a vaga " + candidaturaSelecionada.getVaga().getCargo() + "?",
                ButtonType.YES, ButtonType.NO);

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.YES) {
                try {
                    candidaturaRepository.remover(candidaturaSelecionada);
                    listaCandidaturas.remove(candidaturaSelecionada);
                    tableCandidaturas.refresh();

                    Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                    sucesso.setTitle("Sucesso");
                    sucesso.setHeaderText(null);
                    sucesso.setContentText("Candidatura excluída com sucesso!");
                    sucesso.showAndWait();
                } catch (IOException e) {
                    Alert erro = new Alert(Alert.AlertType.ERROR);
                    erro.setTitle("Erro");
                    erro.setHeaderText(null);
                    erro.setContentText("Erro ao excluir candidatura: " + e.getMessage());
                    erro.showAndWait();
                }
            }
        });
    }


    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
