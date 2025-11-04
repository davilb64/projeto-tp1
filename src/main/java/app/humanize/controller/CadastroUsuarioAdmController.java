package app.humanize.controller;

import app.humanize.exceptions.CpfInvalidoException;
import app.humanize.exceptions.EmailInvalidoException;
import app.humanize.exceptions.SenhaInvalidaException;
import app.humanize.model.*;
import app.humanize.repository.UsuarioRepository;
import app.humanize.service.validacoes.ValidaCpf;
import app.humanize.service.validacoes.ValidaEmail;
import app.humanize.service.validacoes.ValidaSenha;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Random;

public class CadastroUsuarioAdmController {

    @FXML private ImageView imgFotoPerfil;
    @FXML private Button btnEscolherFoto;
    @FXML private Label lblId;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCpf;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenhaOculta;
    @FXML private TextField txtSenhaVisivel;
    @FXML private ToggleButton btnMostrarSenha;
    @FXML private ComboBox<Perfil> perfilCombo;
    @FXML private Label lblEndereco;
    @FXML private TextField txtMatricula;
    @FXML private TextField txtPeriodo;
    @FXML private TextField txtCargo;
    @FXML private TextField txtDepartamento;
    @FXML private ComboBox<Regime> regimeCombo;
    @FXML private TextField txtSalario;
    @FXML private TextField txtReceita;
    @FXML private TextField txtDespesas;


    private Endereco enderecoDoOutroController;
    private final UsuarioRepository usuarioRepository = UsuarioRepository.getInstance();
    private final ValidaCpf validaCpf = new ValidaCpf();
    private final ValidaSenha validaSenha = new ValidaSenha();
    private final ValidaEmail validaEmail = new ValidaEmail();
    private Usuario usuarioParaEditar;

    private String caminhoFotoSelecionada;
    private static final String DIRETORIO_FOTOS = "src/main/resources/fotos_perfil/";


    @FXML
    public void initialize() {
        if (usuarioParaEditar == null) {
            lblId.setText(String.valueOf(usuarioRepository.getProximoId()));
            carregarFotoPokemonAleatorio();
        }
        perfilCombo.getItems().setAll(Perfil.values());
        regimeCombo.getItems().setAll(Regime.values());

        txtSenhaVisivel.textProperty().bindBidirectional(txtSenhaOculta.textProperty());
        txtSenhaVisivel.visibleProperty().bind(btnMostrarSenha.selectedProperty());
        txtSenhaOculta.visibleProperty().bind(btnMostrarSenha.selectedProperty().not());

        try {
            Files.createDirectories(Paths.get(DIRETORIO_FOTOS));
        } catch (IOException e) {
            System.err.println("Falha ao criar diretório de fotos: " + e.getMessage());
        }
    }

    private void carregarFotoPokemonAleatorio() {
        btnEscolherFoto.setDisable(true);

        Task<Image> loadPokemonTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                try {
                    Random random = new Random();
                    int id = random.nextInt(1000) + 1;

                    String spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png";

                    return new Image(spriteUrl);

                } catch (Exception e) {
                    System.err.println("Falha ao buscar Pokémon da API: " + e.getMessage());
                    return carregarAvatarLocal();
                }
            }
        };

        loadPokemonTask.setOnSucceeded(event -> {
            imgFotoPerfil.setImage(loadPokemonTask.getValue());
            btnEscolherFoto.setDisable(false);
        });

        loadPokemonTask.setOnFailed(event -> {
            System.err.println("Falha na Task de carregar Pokémon.");
            imgFotoPerfil.setImage(carregarAvatarLocal());
            btnEscolherFoto.setDisable(false);
        });

        new Thread(loadPokemonTask).start();
    }

    private Image carregarAvatarLocal() {
        try {
            return new Image(new FileInputStream(DIRETORIO_FOTOS + "default_avatar.png"));
        } catch (FileNotFoundException e) {
            System.err.println("Foto padrão não encontrada!");
            return null;
        }
    }

    public void prepararParaEdicao(Usuario usuario) {
        this.usuarioParaEditar = usuario;
        Funcionario func = (Funcionario) usuario;

        lblId.setText(String.valueOf(func.getId()));
        txtNome.setText(func.getNome());
        txtEmail.setText(func.getEmail());
        txtCpf.setText(func.getCpf());
        txtLogin.setText(func.getLogin());
        perfilCombo.setValue(func.getPerfil());

        this.enderecoDoOutroController = func.getEndereco();
        if (this.enderecoDoOutroController != null) {
            lblEndereco.setText(enderecoDoOutroController.enderecoReduzido());
        }

        txtMatricula.setText(String.valueOf(func.getMatricula()));
        txtPeriodo.setText(String.valueOf(func.getPeriodo()));
        txtCargo.setText(func.getCargo());
        txtDepartamento.setText(func.getDepartamento());
        regimeCombo.setValue(func.getRegime());
        txtSalario.setText(String.valueOf(func.getSalario()));
        txtReceita.setText(String.valueOf(func.getReceita()));
        txtDespesas.setText(String.valueOf(func.getDespesas()));

        this.caminhoFotoSelecionada = func.getCaminhoFoto();
        if (this.caminhoFotoSelecionada != null && !this.caminhoFotoSelecionada.isEmpty()) {
            try {
                Image foto = new Image(new FileInputStream(this.caminhoFotoSelecionada));
                imgFotoPerfil.setImage(foto);
            } catch (FileNotFoundException e) {
                System.err.println("Foto de perfil não encontrada: " + this.caminhoFotoSelecionada);
                imgFotoPerfil.setImage(carregarAvatarLocal()); // Usa o fallback
            }
        } else {
            imgFotoPerfil.setImage(carregarAvatarLocal()); // Usa o fallback
        }

        txtSenhaOculta.setPromptText("Digite apenas se desejar alterar a senha");
        txtSenhaVisivel.setPromptText("Digite apenas se desejar alterar a senha");
    }

    @FXML
    private void escolherFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.png", "*.jpeg")
        );

        File arquivoSelecionado = fileChooser.showOpenDialog(imgFotoPerfil.getScene().getWindow());

        if (arquivoSelecionado != null) {
            try {
                String cpf = txtCpf.getText().replaceAll("[^0-9]", "");
                if (cpf.isEmpty() && usuarioParaEditar == null) {
                    mostrarAlerta("CPF Necessário", "Por favor, preencha o CPF antes de selecionar a foto.", null);
                    return;
                }

                String nomeBaseArquivo = (usuarioParaEditar != null) ? String.valueOf(usuarioParaEditar.getId()) : cpf;

                String extensao = getExtensaoArquivo(arquivoSelecionado.getName());
                String novoNomeArquivo = nomeBaseArquivo + "." + extensao;

                Path caminhoDestino = Paths.get(DIRETORIO_FOTOS, novoNomeArquivo);

                Files.copy(arquivoSelecionado.toPath(), caminhoDestino, StandardCopyOption.REPLACE_EXISTING);

                this.caminhoFotoSelecionada = caminhoDestino.toString();

                Image foto = new Image(new FileInputStream(this.caminhoFotoSelecionada));
                imgFotoPerfil.setImage(foto);

            } catch (IOException e) {
                mostrarAlerta("Erro ao Salvar Foto", "Não foi possível copiar a imagem.", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String getExtensaoArquivo(String nomeArquivo) {
        int lastIndex = nomeArquivo.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        }
        return nomeArquivo.substring(lastIndex + 1).toLowerCase();
    }


    @FXML
    private void cadastrarEndereco() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CadastroEndereco.fxml"));
            Parent root = loader.load();
            CadastroEnderecoController enderecoController = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Cadastro de Endereço");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(txtNome.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            this.enderecoDoOutroController = enderecoController.getEnderecoSalvo();
            if (this.enderecoDoOutroController != null) {
                lblEndereco.setText(enderecoDoOutroController.enderecoReduzido());
            }
        } catch (IOException e) {
            mostrarAlerta("Erro Crítico", "Não foi possível carregar a tela de endereço.", "Verifique se o arquivo FXML está no local correto: /view/CadastroEndereco.fxml");
        }
    }

    private boolean validarCampos() {
        if (txtNome.getText().isBlank() || txtCpf.getText().isBlank() || txtLogin.getText().isBlank() || txtEmail.getText().isBlank()) {
            mostrarAlerta("Campos Obrigatórios", "Nome, CPF, E-mail e Login devem ser preenchidos.", null);
            return false;
        }

        if (usuarioParaEditar == null && txtSenhaOculta.getText().isBlank()) {
            mostrarAlerta("Campos Obrigatórios", "O campo Senha deve ser preenchido para novos usuários.", null);
            return false;
        }

        if (perfilCombo.getSelectionModel().isEmpty()) {
            mostrarAlerta("Seleção Obrigatória", "Por favor, selecione um perfil para o usuário.", null);
            return false;
        }
        if (enderecoDoOutroController == null) {
            mostrarAlerta("Endereço Obrigatório", "Por favor, cadastre um endereço para o usuário.", null);
            return false;
        }

        Optional<Usuario> usuarioComEsteLogin = usuarioRepository.buscaUsuarioPorLogin(txtLogin.getText());
        if (usuarioComEsteLogin.isPresent()) {
            if (usuarioParaEditar == null || usuarioParaEditar.getId() != usuarioComEsteLogin.get().getId()) {
                mostrarAlerta("Login Inválido", "Este login já está em uso por outro usuário.", null);
                return false;
            }
        }
        return true;
    }

    @FXML
    private void salvarUsuario() {
        if (!validarCampos()) {
            return;
        }

        String senha = txtSenhaOculta.getText();
        String cpf = txtCpf.getText();
        String email = txtEmail.getText();
        String hash;
        Perfil perfil = perfilCombo.getValue();

        try {
            boolean cpfFoiAlterado = (usuarioParaEditar != null && !usuarioParaEditar.getCpf().equals(cpf));
            if (usuarioParaEditar == null || cpfFoiAlterado) {
                validaCpf.validaCpf(cpf);
            }
            validaEmail.validaEmail(email);

            if (usuarioParaEditar == null) {
                validaSenha.validaSenha(senha);
                hash = BCrypt.hashpw(senha, BCrypt.gensalt());
            } else if (!senha.isBlank()) {
                validaSenha.validaSenha(senha);
                hash = BCrypt.hashpw(senha, BCrypt.gensalt());
            } else {
                hash = usuarioParaEditar.getSenha();
            }

            int matricula = Integer.parseInt(txtMatricula.getText().isBlank() ? "0" : txtMatricula.getText());
            int periodo = Integer.parseInt(txtPeriodo.getText().isBlank() ? "0" : txtPeriodo.getText());
            double salario = Double.parseDouble(txtSalario.getText().isBlank() ? "0.0" : txtSalario.getText());
            double receita = Double.parseDouble(txtReceita.getText().isBlank() ? "0.0" : txtReceita.getText());
            double despesas = Double.parseDouble(txtDespesas.getText().isBlank() ? "0.0" : txtDespesas.getText());
            String cargo = txtCargo.getText();
            String departamento = txtDepartamento.getText();
            Regime regime = regimeCombo.getValue();

            if(this.caminhoFotoSelecionada == null && usuarioParaEditar == null) {
                this.caminhoFotoSelecionada = DIRETORIO_FOTOS + "default_avatar.png";
            }

            if (usuarioParaEditar == null) {
                Usuario usuario;
                switch (perfil) {
                    case ADMINISTRADOR -> usuario = new Administrador.AdministradorBuilder()
                            .caminhoFoto(caminhoFotoSelecionada)
                            .nome(txtNome.getText()).cpf(cpf).email(email).endereco(enderecoDoOutroController)
                            .login(txtLogin.getText()).senha(hash).perfil(perfil)
                            .matricula(matricula).periodo(periodo).departamento(departamento)
                            .receita(receita).despesas(despesas).salario(salario)
                            .cargo(cargo).regime(regime)
                            .build();
                    case GESTOR -> usuario = new Gestor.GestorBuilder()
                            .caminhoFoto(caminhoFotoSelecionada)
                            .nome(txtNome.getText()).cpf(cpf).email(email).endereco(enderecoDoOutroController)
                            .login(txtLogin.getText()).senha(hash).perfil(perfil)
                            .matricula(matricula).periodo(periodo).departamento(departamento)
                            .receita(receita).despesas(despesas).salario(salario)
                            .cargo(cargo).regime(regime)
                            .build();
                    case RECRUTADOR -> usuario = new Recrutador.RecrutadorBuilder()
                            .caminhoFoto(caminhoFotoSelecionada)
                            .nome(txtNome.getText()).cpf(cpf).email(email).endereco(enderecoDoOutroController)
                            .login(txtLogin.getText()).senha(hash).perfil(perfil)
                            .matricula(matricula).periodo(periodo).departamento(departamento)
                            .receita(receita).despesas(despesas).salario(salario)
                            .cargo(cargo).regime(regime)
                            .build();
                    default ->
                            usuario = new Funcionario.FuncionarioBuilder()
                                    .caminhoFoto(caminhoFotoSelecionada)
                                    .nome(txtNome.getText()).cpf(cpf).email(email).endereco(enderecoDoOutroController)
                                    .login(txtLogin.getText()).senha(hash).perfil(perfil)
                                    .matricula(matricula).periodo(periodo).departamento(departamento)
                                    .receita(receita).despesas(despesas).salario(salario)
                                    .cargo(cargo).regime(regime)
                                    .build();
                }
                usuarioRepository.escreveUsuarioNovo(usuario);

            } else {
                Funcionario func = (Funcionario) usuarioParaEditar;
                func.setCaminhoFoto(caminhoFotoSelecionada);
                func.setNome(txtNome.getText());
                func.setCpf(cpf);
                func.setEmail(email);
                func.setLogin(txtLogin.getText());
                func.setPerfil(perfil);
                func.setEndereco(enderecoDoOutroController);
                func.setSenha(hash);
                func.setMatricula(matricula);
                func.setPeriodo(periodo);
                func.setCargo(cargo);
                func.setDepartamento(departamento);
                func.setSalario(salario);
                func.setReceita(receita);
                func.setDespesas(despesas);
                func.setRegime(regime);

                usuarioRepository.atualizarUsuario(func);
            }

            fecharJanela();

        } catch (CpfInvalidoException | SenhaInvalidaException | EmailInvalidoException | NumberFormatException e) {
            mostrarAlerta("Erro de Validação", "Dados inválidos.", e.getMessage());
        } catch (IOException e) {
            mostrarAlerta("Erro de Salvamento", "Falha ao salvar no arquivo CSV.", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta("Erro inesperado", "Tente novamente", e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(mensagem);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        stage.close();
    }
}