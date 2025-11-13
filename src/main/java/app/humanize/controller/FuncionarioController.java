package app.humanize.controller;

import app.humanize.model.Funcionario;
import app.humanize.model.Perfil;
import app.humanize.repository.UsuarioRepository;
import app.humanize.util.UserSession;
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
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
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

    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colSalario.setCellValueFactory(new PropertyValueFactory<>("salario"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colPerfil.setCellValueFactory(new PropertyValueFactory<>("perfil"));
        carregarTabela();

        cbPerfil.getItems().clear();
        cbPerfil.getItems().addAll(Perfil.values());
    }

    private void carregarTabela(){
        ObservableList<Funcionario> dados = FXCollections.observableArrayList(usuarioRepository.getUsuariosInstanceofFuncionario());
        tblFuncionarios.setItems(dados);
        tblFuncionarios.refresh();
    }

    @FXML
    private void contratarFuncionario() throws IOException {
        URL resource = getClass().getResource("/view/ContratarFuncionario.fxml");
        if (resource == null) {
            mostrarAlerta(bundle.getString("employeeManagement.alert.fxmlHireNotFound"));
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource, bundle);
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(bundle.getString("employeeHire.title"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        carregarTabela();
    }

    @FXML
    private void editarFuncionario() throws IOException {
        Funcionario funcionarioSelecionado = tblFuncionarios.getSelectionModel().getSelectedItem();
        if (funcionarioSelecionado == null) {
            mostrarAlerta(bundle.getString("employeeManagement.alert.noSelectionEdit"));
            return;
        }

        URL resource = getClass().getResource("/view/CadastroUsuarioAdm.fxml");
        if (resource == null) {
            mostrarAlerta(bundle.getString("employeeManagement.alert.fxmlEditNotFound"));
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource, bundle);
        Parent root = loader.load();

        CadastroUsuarioAdmController controllerDoCadastro = loader.getController();
        controllerDoCadastro.prepararParaEdicao(funcionarioSelecionado);

        Stage stage = new Stage();
        stage.setTitle(bundle.getString("userManagement.alert.editUserTitle"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(tblFuncionarios.getScene().getWindow());
        stage.showAndWait();

        carregarTabela();
    }

    @FXML
    private void demitirFuncionario() {
        Funcionario funcionarioSelecionado = tblFuncionarios.getSelectionModel().getSelectedItem();
        if (funcionarioSelecionado == null) {
            mostrarAlerta(bundle.getString("employeeManagement.alert.noSelectionDelete"));
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle(bundle.getString("userManagement.alert.confirmDeleteTitle"));
        confirmacao.setHeaderText(bundle.getString("employeeManagement.alert.confirmDeleteHeader") + " " + funcionarioSelecionado.getNome());
        confirmacao.setContentText(bundle.getString("userManagement.alert.confirmDeleteContent"));

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    usuarioRepository.excluirUsuario(funcionarioSelecionado);
                } catch (IOException e) {
                    mostrarAlerta(bundle.getString("employeeManagement.alert.deleteError"));
                    e.printStackTrace();
                }
                carregarTabela();
            }
        });
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(bundle.getString("userManagement.alert.attention"));
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    public void filtra() {
        List<Funcionario> funcionarios = usuarioRepository.getUsuariosInstanceofFuncionario();
        Stream<Funcionario> stream = funcionarios.stream();

        String nomeFiltro = txtNome.getText().trim().toLowerCase();
        if (!nomeFiltro.isEmpty()) {
            stream = stream.filter(func -> func.getNome().toLowerCase().contains(nomeFiltro));
        }

        String idFiltro = txtId.getText().trim();
        if (!idFiltro.isEmpty()) {
            try {
                int id = Integer.parseInt(idFiltro);
                stream = stream.filter(func -> func.getId() == id);
            } catch (NumberFormatException e) {
                System.err.println(bundle.getString("log.error.invalidIdFilter"));
            }
        }

        String cargoFiltro = txtCargo.getText().trim().toLowerCase();
        if (!cargoFiltro.isEmpty()) {
            stream = stream.filter(func -> func.getCargo().toLowerCase().contains(cargoFiltro));
        }

        Perfil perfilFiltro = cbPerfil.getValue();
        if (perfilFiltro != null) {
            stream = stream.filter(func -> func.getPerfil() == perfilFiltro);
        }

        List<Funcionario> filtrados = stream.collect(Collectors.toList());
        tblFuncionarios.setItems(FXCollections.observableArrayList(filtrados));
        tblFuncionarios.refresh();
    }
}