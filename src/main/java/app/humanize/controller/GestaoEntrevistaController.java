package app.humanize.controller;

import app.humanize.model.Perfil;
import app.humanize.model.Vaga;
import app.humanize.repository.CandidatoRepository;
import app.humanize.repository.UsuarioRepository;
import app.humanize.repository.VagaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class GestaoEntrevistaController {

    @FXML
    private TextField txtId;
    @FXML
    private ComboBox cbCandidato;
    @FXML
    private ComboBox cbRecrutador;
    @FXML
    private ComboBox cbVaga;
    @FXML
    private TableView<Vaga> tblEntrevista;
    @FXML
    private TableColumn<Vaga, Integer> colId;
    @FXML
    private TableColumn<Vaga, String> colCandidato;
    @FXML
    private TableColumn<Vaga, String> colRecrutador;
    @FXML
    private TableColumn<Vaga, String> colVaga;

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

    @FXML
    public void initialize() {
        //chamar os metodos no initialize
        carregarVagas();
        carregarCandidatos();
        carregarRecrutadores();
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

    }

    @FXML
    public void filtra() {

    }
}
