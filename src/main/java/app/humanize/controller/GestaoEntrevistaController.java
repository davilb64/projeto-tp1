package app.humanize.controller;

import app.humanize.model.*;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.EntrevistaRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
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

public class GestaoEntrevistaController {

    @FXML
    private TextField txtId;
    @FXML
    private ComboBox<Candidato> cbCandidato;
    @FXML
    private ComboBox<Usuario> cbRecrutador;
    @FXML
    private ComboBox<Vaga> cbVaga;
    @FXML
    private TableView<Entrevista> tblEntrevista;
    @FXML
    private TableColumn<Entrevista, Integer> colId;
    @FXML
    private TableColumn<Entrevista, String> colCandidato;
    @FXML
    private TableColumn<Entrevista, String> colRecrutador;
    @FXML
    private TableColumn<Entrevista, String> colVaga;
    @FXML
    private TableColumn<Entrevista, StatusEntrevista> colStatus;

    @FXML
    private Button btnFiltrar;
    @FXML
    private Button btnMarcarEntrevista;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnExcluir;

    private final VagaRepository vagaRepository = VagaRepository.getInstance();
    private final CandidatoRepository candidatoRepository = CandidatoRepository.getInstance();
    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final EntrevistaRepository entrevistaRepository = EntrevistaRepository.getInstance();

    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        carregarVagas();
        carregarCandidatos();
        carregarRecrutadores();
        carregarTabela();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCandidato.setCellValueFactory(new PropertyValueFactory<>("candidato"));
        colRecrutador.setCellValueFactory(new PropertyValueFactory<>("recrutador"));
        colVaga.setCellValueFactory(new PropertyValueFactory<>("vaga"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void carregarTabela(){
        ObservableList<Entrevista> dados = FXCollections.observableArrayList(entrevistaRepository.getTodasEntrevistas());
        tblEntrevista.setItems(dados);
        tblEntrevista.refresh();
    }

    private void carregarVagas() {
        cbVaga.getItems().clear();
        cbVaga.getItems().addAll(vagaRepository.getTodasVagas());
    }

    private void carregarCandidatos() {
        cbCandidato.getItems().clear();
        cbCandidato.getItems().addAll(candidatoRepository.getTodos());
    }

    private void carregarRecrutadores(){
        cbRecrutador.getItems().clear();
        cbRecrutador.getItems().addAll(usuarioRepository.buscaPorPerfil(Perfil.RECRUTADOR));
    }


    @FXML
    public void cadastrarEntrevista() throws IOException{
        URL resource = getClass().getResource("/view/MarcarEntrevista.fxml");
        if (resource == null) {
            mostrarAlerta(bundle.getString("alert.error.fxmlNotFound.scheduleInterview"));
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource, bundle);
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(bundle.getString("scheduleInterview.title"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        carregarTabela();
    }

    @FXML
    public void editarEntrevista() throws IOException {
        Entrevista entrevista = tblEntrevista.getSelectionModel().getSelectedItem();
        if (entrevista == null) {
            mostrarAlerta(bundle.getString("interviewManagement.alert.noSelectionEdit"));
            return;
        }

        URL resource = getClass().getResource("/view/MarcarEntrevista.fxml");
        if (resource == null) {
            mostrarAlerta(bundle.getString("alert.error.fxmlNotFound.scheduleInterview"));
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource, bundle);
        Parent root = loader.load();

        MarcarEntrevistaController controllerDoCadastro = loader.getController();
        controllerDoCadastro.prepararParaEdicao(entrevista);

        Stage stage = new Stage();
        stage.setTitle(bundle.getString("interviewManagement.alert.editTitle"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner((Stage) tblEntrevista.getScene().getWindow());
        stage.showAndWait();

        carregarTabela();
    }

    @FXML
    public void excluirEntrevista() {
        Entrevista entrevistaSelecionada = tblEntrevista.getSelectionModel().getSelectedItem();

        if (entrevistaSelecionada != null) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle(bundle.getString("interviewManagement.alert.confirmDeleteTitle"));
            confirmacao.setHeaderText(bundle.getString("interviewManagement.alert.confirmDeleteHeader") + " " + entrevistaSelecionada.getDataEntrevista());
            confirmacao.setContentText(bundle.getString("interviewManagement.alert.confirmDeleteContent"));

            confirmacao.showAndWait().ifPresent(resposta -> {
                if (resposta == ButtonType.OK) {
                    try {
                        entrevistaRepository.excluirEntrevista(entrevistaSelecionada);
                    } catch (IOException e) {
                        mostrarAlerta(bundle.getString("interviewManagement.alert.deleteError"));
                        e.printStackTrace();
                    }
                    carregarTabela();
                }
            });
        } else {
            mostrarAlerta(bundle.getString("interviewManagement.alert.noSelectionDelete"));
        }
    }

    @FXML
    public void filtra() {
        List<Entrevista> entrevistas = entrevistaRepository.getTodasEntrevistas();
        Stream<Entrevista> stream = entrevistas.stream();

        String idFiltro = txtId.getText().trim();
        if (!idFiltro.isEmpty()) {
            try {
                int id = Integer.parseInt(idFiltro);
                stream = stream.filter(entrevista -> entrevista.getId() == id);
            } catch (NumberFormatException e) {
                System.err.println(bundle.getString("log.error.invalidIdFilter"));
            }
        }

        Vaga vagaFiltro = cbVaga.getValue();
        if (vagaFiltro != null) {
            stream = stream.filter(entrevista ->
                    entrevista.getVaga().getId() == vagaFiltro.getId()
            );
        }

        Candidato candidatoFiltro = cbCandidato.getValue();
        if (candidatoFiltro != null) {
            stream = stream.filter(entrevista ->
                    entrevista.getCandidato().getId() == candidatoFiltro.getId()
            );
        }

        Usuario usuarioFiltro = cbRecrutador.getValue();
        if (usuarioFiltro != null) {
            stream = stream.filter(entrevista ->
                    entrevista.getRecrutador().getId() == usuarioFiltro.getId()
            );
        }

        List<Entrevista> entrevistasFiltradas = stream.collect(Collectors.toList());
        tblEntrevista.setItems(FXCollections.observableArrayList(entrevistasFiltradas));
        tblEntrevista.refresh();
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(bundle.getString("interviewManagement.alert.attention"));
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}