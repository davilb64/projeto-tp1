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

import java.io.IOException;

public class StatusDaCandidaturaController {

    @FXML private TableView<Candidato> tableCandidaturas;
    @FXML private TableColumn<Candidato, String> colCandidato;
    @FXML private TableColumn<Candidato, String> colCargo;
    @FXML private TableColumn<Candidato, String> colStatus;

    private final CandidatoRepository candidatoRepository = CandidatoRepository.getInstance();
    private final ObservableList<Candidato> listaCandidatos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colCandidato.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCargo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getVaga() != null ? cellData.getValue().getVaga().getCargo() : ""
                )
        );
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Em análise")
        );

        listaCandidatos.addAll(candidatoRepository.getTodos());
        tableCandidaturas.setItems(listaCandidatos);
    }

    public static void adicionarCandidato(Candidato candidato) {
        try {
            CandidatoRepository.getInstance().adicionar(candidato);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void editarCandidato() {
        Candidato candidato = tableCandidaturas.getSelectionModel().getSelectedItem();
        if (candidato == null) {
            mostrarAlerta("Selecione um candidato para editar!");
            return;
        }

        // Exemplo simples: alterar o status diretamente (poderia abrir outra janela)
        candidato.setDisponibilidade("Atualizada");
        try {
            candidatoRepository.atualizar();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tableCandidaturas.refresh();
    }

    @FXML
    private void excluirCandidato() {
        Candidato candidato = tableCandidaturas.getSelectionModel().getSelectedItem();
        if (candidato == null) {
            mostrarAlerta("Selecione um candidato para excluir!");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente excluir o candidato " + candidato.getNome() + "?",
                ButtonType.YES, ButtonType.NO);

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.YES) {
                try {
                    candidatoRepository.remover(candidato);
                    listaCandidatos.remove(candidato);
                } catch (IOException e) {
                    e.printStackTrace();
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
