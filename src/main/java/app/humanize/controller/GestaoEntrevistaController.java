package app.humanize.controller;

import app.humanize.model.*;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.EntrevistaRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;
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

    @FXML
    public void initialize() {
        //chamar os metodos no initialize
        carregarVagas();
        carregarCandidatos();
        carregarRecrutadores();
        //carregar dados da tabela
        carregarTabela();
        //inicializar as colunas da tabela
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCandidato.setCellValueFactory(new PropertyValueFactory<>("candidato"));
        colRecrutador.setCellValueFactory(new PropertyValueFactory<>("recrutador"));
        colVaga.setCellValueFactory(new PropertyValueFactory<>("vaga"));
    }

    private void carregarTabela(){
        ObservableList<Entrevista> dados = FXCollections.observableArrayList(entrevistaRepository.getTodasEntrevistas());
        tblEntrevista.setItems(dados);
        tblEntrevista.refresh();
    }

    private void carregarVagas() {
        cbVaga.getItems().clear(); //pegar o nome do fx:id do choice box e limpar os itens
        cbVaga.getItems().addAll(vagaRepository.getTodasVagas()); // //adicionar todas as vagas no choice box
        //note que estou usando o metodo getTodasVagas da classe  VagaRepository
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
    public void cadastrarEntrevista() {

    }

    @FXML
    public void editarEntrevista() {

    }

    @FXML
    public void excluirEntrevista() {
        Entrevista entrevistaSelecionada = tblEntrevista.getSelectionModel().getSelectedItem();

        if (entrevistaSelecionada != null) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar Exclusão");
            confirmacao.setHeaderText("Excluir entrevista: " + entrevistaSelecionada.getDataEntrevista());
            confirmacao.setContentText("Você tem certeza que deseja excluir esta entrevista? Esta ação não pode ser desfeita.");

            confirmacao.showAndWait().ifPresent(resposta -> {
                if (resposta == ButtonType.OK) {
                    try {
                        entrevistaRepository.excluirEntrevista(entrevistaSelecionada);
                    } catch (IOException e) {
                        mostrarAlerta("Erro ao excluir entrevista do arquivo.");
                        e.printStackTrace();
                    }
                    carregarTabela();
                }
            });
        } else {
            mostrarAlerta("Nenhuma vaga foi selecionada para excluir.");
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
                System.err.println("Filtro de ID inválido, ignorado.");
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
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
