package app.humanize.controller;

import app.humanize.model.Perfil;
import app.humanize.model.Vaga;
import app.humanize.repository.VagaRepository;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class VagasController {

    @FXML
    private TableView<Vaga> tblVagas;
    @FXML
    private TableColumn<Vaga,Integer> colId;
    @FXML
    private TableColumn<Vaga,String> colCargo;
    @FXML
    private TableColumn<Vaga,String> colSalario;
    @FXML
    private TableColumn<Vaga, Perfil> colStatus;

    private final VagaRepository vagaRepository = VagaRepository.getInstance();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colSalario.setCellValueFactory(new PropertyValueFactory<>("salario"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        carregarTabela();
    }

    private void carregarTabela(){
        ObservableList<Vaga> dados = FXCollections.observableArrayList(vagaRepository.getTodasVagas());
        tblVagas.setItems(dados);
    }

    @FXML
    private void cadastrarVaga() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CriarVaga.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Cadastrar Vaga");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        carregarTabela();
    }

    @FXML
    private void editarVaga() throws IOException {
        Vaga vaga = tblVagas.getSelectionModel().getSelectedItem();
        if (vaga == null) {
            mostrarAlerta("Nenhum vaga selecionada para editar.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CriarVaga.fxml"));
        Parent root = loader.load();

        CriarVagaController controllerDoCadastro = loader.getController();

        controllerDoCadastro.prepararParaEdicao(vaga);

        Stage stage = new Stage();
        stage.setTitle("Editar Usuário");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner((Stage) tblVagas.getScene().getWindow());
        stage.showAndWait();

        carregarTabela();
    }

    @FXML
    private void excluirVaga() {
        Vaga vagaSelecionado = tblVagas.getSelectionModel().getSelectedItem();

        if (vagaSelecionado != null) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar Exclusão");
            confirmacao.setHeaderText("Excluir vaga: " + vagaSelecionado.getCargo());
            confirmacao.setContentText("Você tem certeza que deseja excluir esta vaga? Esta ação não pode ser desfeita.");

            confirmacao.showAndWait().ifPresent(resposta -> {
                if (resposta == ButtonType.OK) {
                    try {
                        vagaRepository.excluirVaga(vagaSelecionado);
                    } catch (IOException e) {
                        mostrarAlerta("Erro ao excluir vaga do arquivo.");
                        e.printStackTrace();
                    }
                    carregarTabela();
                }
            });

        } else {
            mostrarAlerta("Nenhuma vaga foi selecionada para excluir.");
        }
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
