package app.humanize.controller;

import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.model.Vaga;
import app.humanize.repository.VagaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VagasController {

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtCargo;
    @FXML
    private TextField txtStatus;
    @FXML
    private TextField txtSalario;
    @FXML
    private TableView<Vaga> tblVagas;
    @FXML
    private TableColumn<Vaga,Integer> colId;
    @FXML
    private TableColumn<Vaga, String> colDepartamento;
    @FXML
    private TableColumn<Vaga,String> colCargo;
    @FXML
    private TableColumn<Vaga,String> colSalario;
    @FXML
    private TableColumn<Vaga, String> colStatus;
    @FXML
    private TableColumn<Vaga, String> colRequisitos;

    private final VagaRepository vagaRepository = VagaRepository.getInstance();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colDepartamento.setCellValueFactory(new PropertyValueFactory<>("departamento"));
        colSalario.setCellValueFactory(new PropertyValueFactory<>("salario"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRequisitos.setCellValueFactory(new PropertyValueFactory<>("requisitos"));
        carregarTabela();
    }

    private void carregarTabela(){
        ObservableList<Vaga> dados = FXCollections.observableArrayList(vagaRepository.getTodasVagas());
        tblVagas.setItems(dados);
        tblVagas.refresh();
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

    @FXML
    public void filtra(){
        carregarFiltro();
    }

    private void carregarFiltro() {
        List<Vaga> vagas = vagaRepository.getTodasVagas();

        Stream<Vaga> stream = vagas.stream();

        String cargoFiltro = txtCargo.getText().trim();
        if (!cargoFiltro.isEmpty()) {
            stream = stream.filter(vaga ->
                    vaga.getCargo().toLowerCase().contains(cargoFiltro.toLowerCase())
            );
        }

        String idFiltro = txtId.getText().trim();
        if (!idFiltro.isEmpty()) {
            try {
                int id = Integer.parseInt(idFiltro);
                stream = stream.filter(vaga -> vaga.getId() == id);
            } catch (NumberFormatException e) {
                System.err.println("Filtro de ID inválido, ignorado.");
            }
        }

        String statusFiltro = txtStatus.getText().trim();
        if (!statusFiltro.isEmpty()) {
            stream = stream.filter(vaga ->
                    vaga.getStatus().name().toLowerCase().contains(statusFiltro.toLowerCase())
            );
        }

        String salarioFiltro = txtSalario.getText().trim();
        if (!salarioFiltro.isEmpty()) {
            stream = stream.filter(vaga ->
                    vaga.getSalario().toLowerCase().contains(salarioFiltro.toLowerCase())
            );
        }

        List<Vaga> vagasFiltrados = stream.collect(Collectors.toList());
        tblVagas.setItems(FXCollections.observableArrayList(vagasFiltrados));
        tblVagas.refresh();
    }
}
