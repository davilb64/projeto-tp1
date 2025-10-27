package app.humanize.controller;

import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;
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

public class UsuariosController {

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtEmail;
    @FXML
    private TableView<Usuario> tblUsuarios;
    @FXML
    private TableColumn<Usuario,Integer> colId;
    @FXML
    private TableColumn<Usuario,String> colNome;
    @FXML
    private TableColumn<Usuario,String> colEmail;
    @FXML
    private TableColumn<Usuario, Perfil> colPerfil;
    @FXML
    private ComboBox<Perfil> comboPerfil;


    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPerfil.setCellValueFactory(new PropertyValueFactory<>("perfil"));
        comboPerfil.getItems().setAll(Perfil.values());
        carregarTabela();
    }

    private void carregarTabela(){
        ObservableList<Usuario> dados = FXCollections.observableArrayList(usuarioRepository.getTodosUsuarios());
        tblUsuarios.setItems(dados);
    }

    private void carregarFiltro() {
        List<Usuario> usuarios = usuarioRepository.getTodosUsuarios();

        Stream<Usuario> stream = usuarios.stream();

        String nomeFiltro = txtNome.getText().trim();
        if (!nomeFiltro.isEmpty()) {
            stream = stream.filter(usuario ->
                    usuario.getNome().toLowerCase().contains(nomeFiltro.toLowerCase())
            );
        }

        String idFiltro = txtId.getText().trim();
        if (!idFiltro.isEmpty()) {
            try {
                int id = Integer.parseInt(idFiltro);
                stream = stream.filter(usuario -> usuario.getId() == id);
            } catch (NumberFormatException e) {
                System.err.println("Filtro de ID inválido, ignorado.");
            }
        }

        String emailFiltro = txtEmail.getText().trim();
        if (!emailFiltro.isEmpty()) {
            stream = stream.filter(usuario ->
                    usuario.getEmail().toLowerCase().contains(emailFiltro.toLowerCase())
            );
        }

        Perfil perfilFiltro = comboPerfil.getSelectionModel().getSelectedItem();
        if (perfilFiltro != null) {
            stream = stream.filter(usuario -> usuario.getPerfil() == perfilFiltro);
        }

        List<Usuario> usuariosFiltrados = stream.collect(Collectors.toList());
        tblUsuarios.setItems(FXCollections.observableArrayList(usuariosFiltrados));
        tblUsuarios.refresh();
    }

    @FXML
    public void filtra(){
        carregarFiltro();
    }

    @FXML
    private void cadastrarUsuario() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroUsuarioAdm.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Cadastrar Usuário");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        carregarFiltro();
    }

    @FXML
    private void editarUsuario() throws IOException {
        Usuario usuarioSelecionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSelecionado == null) {
            mostrarAlerta("Nenhum usuário selecionado para editar.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroUsuarioAdm.fxml"));
        Parent root = loader.load();

        CadastroUsuarioAdmController controllerDoCadastro = loader.getController();

        controllerDoCadastro.prepararParaEdicao(usuarioSelecionado);

        Stage stage = new Stage();
        stage.setTitle("Editar Usuário");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(tblUsuarios.getScene().getWindow());
        stage.showAndWait();

        // --- CORREÇÃO AQUI ---
        carregarFiltro();
    }

    @FXML
    private void excluirUsuario() {
        Usuario usuarioSelecionado = tblUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSelecionado != null) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar Exclusão");
            confirmacao.setHeaderText("Excluir usuário: " + usuarioSelecionado.getNome());
            confirmacao.setContentText("Você tem certeza que deseja excluir este usuário? Esta ação não pode ser desfeita.");

            confirmacao.showAndWait().ifPresent(resposta -> {
                if (resposta == ButtonType.OK) {
                    try {
                        usuarioRepository.excluirUsuario(usuarioSelecionado);
                    } catch (IOException e) {
                        mostrarAlerta("Erro ao excluir usuário do arquivo.");
                        e.printStackTrace();
                    }

                    carregarFiltro();
                }
            });

        } else {
            mostrarAlerta("Nenhum usuário foi selecionado para excluir.");
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