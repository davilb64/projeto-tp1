package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.model.Perfil;
import app.humanize.model.Vaga;
import app.humanize.repository.UsuarioRepository;
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

public class FuncionarioController {

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtCargo;
    @FXML
    private ComboBox<Perfil> cbPerfil;
    @FXML
    private TableView<Funcionario> tblFuncionarios;
    @FXML
    private TableColumn<Funcionario,Integer> colId;
    @FXML
    private TableColumn<Funcionario,String> colNome;
    @FXML
    private TableColumn<Funcionario,String> colCargo;
    @FXML
    private TableColumn<Funcionario, Perfil> colPerfil;
    @FXML
    private TableColumn<Funcionario, String> colSalario;

    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        //colSalario.setCellValueFactory(new PropertyValueFactory<>("salario"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colPerfil.setCellValueFactory(new PropertyValueFactory<>("perfil"));
        carregarTabela();
        //populando a lista de perfil
        cbPerfil.getItems().clear();
        cbPerfil.getItems().addAll(Perfil.values());
    }

    private void carregarTabela(){

    }

    @FXML
    private void contratarFuncionario() throws IOException {
    }

    @FXML
    private void editarFuncionario() throws IOException {

    }

    @FXML
    private void demitirFuncionario() {

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

    }

}
