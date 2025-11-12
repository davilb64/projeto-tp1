package app.humanize.controller;

import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;
import app.humanize.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
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
    private TableColumn<Usuario, String> colFoto;
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

    private ResourceBundle bundle;
    private Image avatarPadrao;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        // carrega o avatar padrão
        try (InputStream is = getClass().getResourceAsStream("/fotos_perfil/default_avatar.png")) {
            if (is == null) throw new FileNotFoundException("Avatar padrão não encontrado nos resources.");
            this.avatarPadrao = new Image(is);
        } catch (Exception e) {
            System.err.println(bundle.getString("log.error.avatarDefaultNotFound"));
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPerfil.setCellValueFactory(new PropertyValueFactory<>("perfil"));

        colFoto.setCellValueFactory(new PropertyValueFactory<>("caminhoFoto"));

        colFoto.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String caminho, boolean empty) {
                super.updateItem(caminho, empty);

                if (empty) {
                    setGraphic(null);
                } else if (caminho == null || caminho.isEmpty()) {
                    imageView.setImage(avatarPadrao);
                    setGraphic(imageView);
                } else {
                    try {
                        File file = new File(caminho);
                        Image img = new Image(file.toURI().toString());
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        System.err.println(bundle.getString("log.error.photoNotFound") + caminho);
                        imageView.setImage(avatarPadrao);
                        setGraphic(imageView);
                    }
                }
            }
        });

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
                System.err.println(bundle.getString("log.error.invalidIdFilter"));
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
        URL resource = getClass().getResource("/view/CadastroUsuarioAdm.fxml");
        FXMLLoader loader = new FXMLLoader(resource, bundle);

        Parent root = loader.load();
        Stage stage = new Stage();

        stage.setTitle(bundle.getString("userRegistration.title"));

        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        carregarFiltro();
    }

    @FXML
    private void editarUsuario() throws IOException {
        Usuario usuarioSelecionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSelecionado == null) {
            mostrarAlerta(bundle.getString("userManagement.alert.noUserSelectedEdit"));
            return;
        }

        URL resource = getClass().getResource("/view/CadastroUsuarioAdm.fxml");
        FXMLLoader loader = new FXMLLoader(resource, bundle);

        Parent root = loader.load();

        CadastroUsuarioAdmController controllerDoCadastro = loader.getController();
        controllerDoCadastro.prepararParaEdicao(usuarioSelecionado);

        Stage stage = new Stage();
        stage.setTitle(bundle.getString("userManagement.alert.editUserTitle"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(tblUsuarios.getScene().getWindow());
        stage.showAndWait();

        carregarFiltro();
    }

    @FXML
    private void excluirUsuario() {
        Usuario usuarioSelecionado = tblUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSelecionado != null) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);

            confirmacao.setTitle(bundle.getString("userManagement.alert.confirmDeleteTitle"));
            confirmacao.setHeaderText(bundle.getString("userManagement.alert.confirmDeleteHeader") + " " + usuarioSelecionado.getNome());
            confirmacao.setContentText(bundle.getString("userManagement.alert.confirmDeleteContent"));

            confirmacao.showAndWait().ifPresent(resposta -> {
                if (resposta == ButtonType.OK) {
                    try {
                        usuarioRepository.excluirUsuario(usuarioSelecionado);
                    } catch (IOException e) {
                        mostrarAlerta(bundle.getString("userManagement.alert.deleteError"));
                        e.printStackTrace();
                    }
                    carregarFiltro();
                }
            });

        } else {
            mostrarAlerta(bundle.getString("userManagement.alert.noUserSelectedDelete"));
        }
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(bundle.getString("userManagement.alert.attention"));
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}