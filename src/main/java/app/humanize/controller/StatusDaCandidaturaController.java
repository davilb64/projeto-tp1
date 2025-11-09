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


  /*  private final CandidatoRepository candidatoRepository = CandidatoRepository.getInstance();
    private final ObservableList<Candidato> listaCandidatos = FXCollections.observableArrayList();


    public static void adicionarCandidato(Candidato candidato) {
        try {
            CandidatoRepository.getInstance().adicionar(candidato);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    @FXML
    private void excluirCandidatura() {
        Candidatura candidaturaSelecionada = tableCandidaturas.getSelectionModel().getSelectedItem();

        if (candidaturaSelecionada == null) {
            mostrarAlerta("Selecione uma candidatura para excluir!");
            return;
        }
        if(candidaturaSelecionada.getStatus() == PENDENTE){
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
        else{
            mostrarAlerta("Só é permitido excluir candidaturas pendentes!");
            return;
        }
    }

    @FXML
    private void editarCandidato() {
        Candidatura selecionada = tableCandidaturas.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            mostrarAlerta("Selecione uma candidatura para editar o status!");
            return;
        }

        try {
            // Carrega o popup FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TrocarStatus.fxml"));
            Parent root = loader.load();

            // Passa a candidatura selecionada para o controller do popup
            TrocarStatusController controller = loader.getController();
            controller.setCandidatura(selecionada);
            controller.setOnStatusAlterado(() -> {
                // 🔄 Atualiza a tabela depois da alteração
                tableCandidaturas.refresh();
            });

            // Cria e mostra o popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Alterar Status da Candidatura");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro ao abrir tela de troca de status: " + e.getMessage());
        }
    }



    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
