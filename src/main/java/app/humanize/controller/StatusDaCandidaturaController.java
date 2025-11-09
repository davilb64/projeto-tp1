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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static app.humanize.model.StatusCandidatura.PENDENTE;

public class StatusDaCandidaturaController {

    @FXML private TableView<Candidatura> tableCandidaturas;
    @FXML private TableColumn<Candidatura, String> colCandidato;
    @FXML private TableColumn<Candidatura, String> colCargo;
    @FXML private TableColumn<Candidatura, String> colStatus;

    private final CandidaturaRepository candidaturaRepository = CandidaturaRepository.getInstance();
    private final ObservableList<Candidatura> listaCandidaturas = FXCollections.observableArrayList();

    private ResourceBundle bundle;

    // Método auxiliar para traduzir o status
    private String getTraducaoStatus(StatusCandidatura status) {
        if (status == null) return "";
        // Busca a chave, ex: "statusCandidatura.PENDENTE"
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
                new javafx.beans.property.SimpleStringProperty(getTraducaoStatus(cellData.getValue().getStatus())));

        listaCandidaturas.addAll(candidaturaRepository.getTodas());
        tableCandidaturas.setItems(listaCandidaturas);
    }

    @FXML
    private void excluirCandidatura() {
        Candidatura candidaturaSelecionada = tableCandidaturas.getSelectionModel().getSelectedItem();

        if (candidaturaSelecionada == null) {
            mostrarAlerta(bundle.getString("applicationStatus.alert.noSelectionDelete"));
            return;
        }
        if(candidaturaSelecionada.getStatus() == PENDENTE){
            String content = bundle.getString("applicationStatus.alert.confirmDeleteContent1") + " " +
                    candidaturaSelecionada.getCandidato().getNome() + " " +
                    bundle.getString("applicationStatus.alert.confirmDeleteContent2") + " " +
                    candidaturaSelecionada.getVaga().getCargo() + "?";

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);
            confirmacao.setTitle(bundle.getString("applicationStatus.alert.confirmDeleteTitle"));

            confirmacao.showAndWait().ifPresent(resposta -> {
                if (resposta == ButtonType.YES) {
                    try {
                        candidaturaRepository.remover(candidaturaSelecionada);
                        listaCandidaturas.remove(candidaturaSelecionada);
                        tableCandidaturas.refresh();

                        Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                        sucesso.setTitle(bundle.getString("alert.success.title"));
                        sucesso.setHeaderText(null);
                        sucesso.setContentText(bundle.getString("applicationStatus.alert.deleteSuccess"));
                        sucesso.showAndWait();
                    } catch (IOException e) {
                        Alert erro = new Alert(Alert.AlertType.ERROR);
                        erro.setTitle(bundle.getString("alert.error.reload.title"));
                        erro.setHeaderText(null);
                        erro.setContentText(bundle.getString("applicationStatus.alert.deleteError") + " " + e.getMessage());
                        erro.showAndWait();
                    }
                }
            });
        }
        else{
            mostrarAlerta(bundle.getString("applicationStatus.alert.onlyPendingDelete"));
            return;
        }
    }

    @FXML
    private void editarCandidato() {
        Candidatura selecionada = tableCandidaturas.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            mostrarAlerta(bundle.getString("applicationStatus.alert.noSelectionEdit"));
            return;
        }

        try {
            URL resource = getClass().getResource("/view/TrocarStatus.fxml");
            if (resource == null) {
                throw new IOException(bundle.getString("applicationStatus.exception.fxmlNotFound.trocarStatus"));
            }

            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Parent root = loader.load();

            TrocarStatusController controller = loader.getController();
            controller.setCandidatura(selecionada);
            controller.setOnStatusAlterado(() -> {
                tableCandidaturas.refresh();
            });

            Stage popupStage = new Stage();
            popupStage.setTitle(bundle.getString("changeStatus.title"));
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(bundle.getString("applicationStatus.alert.errorLoadEdit") + " " + e.getMessage());
        }
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(bundle.getString("userManagement.alert.attention"));
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}