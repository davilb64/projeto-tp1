package app.humanize.controller;

import app.humanize.exceptions.CpfInvalidoException;
import app.humanize.exceptions.EmailInvalidoException;
import app.humanize.exceptions.SenhaInvalidaException;
import app.humanize.model.*;
import app.humanize.repository.UsuarioRepository;
import app.humanize.service.validacoes.ValidaCpf;
import app.humanize.service.validacoes.ValidaEmail;
import app.humanize.service.validacoes.ValidaSenha;
import app.humanize.util.UserSession;
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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List; // Importar List
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.io.ByteArrayInputStream;

public class CadastroUsuarioAdmController {

    @FXML private ImageView imgFotoPerfil;
    @FXML private Button btnEscolherFoto;
    @FXML private Button btnGerarPokemon;
    // ... (outros FXML)
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
    @FXML private DatePicker dpDataAdmissao;
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

    private String caminhoFotoAtualSalva;
    private File arquivoFotoSelecionado = null;
    private byte[] bytesFotoPokemon = null;

    // REMOVIDO: DIRETORIO_FOTOS
    private ResourceBundle bundle;

    /**
     * Retorna o caminho absoluto para a pasta de fotos do aplicativo (fora do JAR).
     * Ex: C:\Users\SeuNome\.humanize-app-data\fotos_perfil
     */
    private Path getPathParaFotos() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".humanize-app-data", "fotos_perfil");
    }

    /**
     * Copia todas as fotos padrão de /resources/fotos_perfil (de dentro do JAR)
     * para a pasta de dados externa (user.home), se elas ainda não existirem lá.
     */
    private void seedDefaultPhotos() {
        Path externalPhotoDir = getPathParaFotos();

        // Lista de todas as suas fotos padrão
        List<String> defaultPhotos = List.of(
                "2.png", "3.png", "4.png", "5.png", "6.png", "7.png", "8.png",
                "9.png", "10.png", "12.png", "04935825170.png",
                "08650999107.png", "default_avatar.png"
        );

        for (String photoName : defaultPhotos) {
            File externalFile = externalPhotoDir.resolve(photoName).toFile();

            // Só copia se o arquivo NÃO existir na pasta externa
            if (!externalFile.exists()) {
                // Caminho DENTRO do JAR (na raiz do 'resources')
                String resourcePath = "/fotos_perfil/" + photoName;
                try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        System.err.println("Foto de semeadura não encontrada no JAR: " + resourcePath);
                        continue;
                    }
                    // Copia do JAR para a pasta externa
                    Files.copy(is, externalFile.toPath());
                } catch (IOException e) {
                    System.err.println("Falha ao semear foto: " + photoName + " - " + e.getMessage());
                }
            }
        }
    }


    @FXML
    public void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        try {
            // 1. Cria o diretório externo (ex: C:\Users\Nome\.humanize-app-data\fotos_perfil)
            Path externalPhotoDir = getPathParaFotos();
            Files.createDirectories(externalPhotoDir);

            // 2. Semeia (copia) as fotos padrão do JAR para lá, se necessário
            seedDefaultPhotos();

        } catch (IOException e) {
            System.err.println(bundle.getString("log.error.photoDirFailed") + e.getMessage());
        }

        if (usuarioParaEditar == null) {
            lblId.setText(String.valueOf(usuarioRepository.getProximoId()));
            imgFotoPerfil.setImage(carregarAvatarLocal()); // Carrega o fallback do JAR
            dpDataAdmissao.setValue(LocalDate.now());
        }
        perfilCombo.getItems().setAll(Perfil.values());
        regimeCombo.getItems().setAll(Regime.values());

        txtSenhaVisivel.textProperty().bindBidirectional(txtSenhaOculta.textProperty());
        txtSenhaVisivel.visibleProperty().bind(btnMostrarSenha.selectedProperty());
        txtSenhaOculta.visibleProperty().bind(btnMostrarSenha.selectedProperty().not());
    }

    /**
     * Carrega o avatar padrão DE DENTRO do JAR (resources) como um fallback.
     */
    private Image carregarAvatarLocal() {
        try (InputStream is = getClass().getResourceAsStream("/fotos_perfil/default_avatar.png")) {
            if (is == null) {
                throw new FileNotFoundException("Avatar padrão não encontrado nos resources.");
            }
            return new Image(is);
        } catch (Exception e) {
            System.err.println(bundle.getString("log.error.photoDefaultNotFound"));
            return null;
        }
    }

    @FXML
    private void gerarFotoPokemon() {
        // ... (método não muda) ...
        btnEscolherFoto.setDisable(true);
        btnGerarPokemon.setDisable(true);

        Task<byte[]> loadPokemonTask = new Task<>() {
            @Override
            protected byte[] call() throws Exception {
                try {
                    Random random = new Random();
                    int id = random.nextInt(1000) + 1;
                    String spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png";

                    InputStream in = new URL(spriteUrl).openStream();
                    byte[] imageBytes = in.readAllBytes();
                    in.close();
                    return imageBytes;
                } catch (Exception e) {
                    System.err.println(bundle.getString("log.error.pokemonApiFailed") + e.getMessage());
                    throw e;
                }
            }
        };

        loadPokemonTask.setOnSucceeded(event -> {
            this.bytesFotoPokemon = loadPokemonTask.getValue();
            this.arquivoFotoSelecionado = null;
            imgFotoPerfil.setImage(new Image(new ByteArrayInputStream(this.bytesFotoPokemon)));
            btnEscolherFoto.setDisable(false);
            btnGerarPokemon.setDisable(false);
        });

        loadPokemonTask.setOnFailed(event -> {
            System.err.println(bundle.getString("log.error.pokemonTaskFailed"));
            imgFotoPerfil.setImage(carregarAvatarLocal());
            btnEscolherFoto.setDisable(false);
            btnGerarPokemon.setDisable(false);
        });

        new Thread(loadPokemonTask).start();
    }

    public void prepararParaEdicao(Usuario usuario) {
        this.usuarioParaEditar = usuario;
        Funcionario func = (Funcionario) usuario;

        // ... (resto dos setTexts) ...
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
        dpDataAdmissao.setValue(func.getDataAdmissao());
        txtPeriodo.setText(String.valueOf(func.getPeriodo()));
        txtCargo.setText(func.getCargo());
        txtDepartamento.setText(func.getDepartamento());
        regimeCombo.setValue(func.getRegime());
        txtSalario.setText(String.valueOf(func.getSalario()));
        txtReceita.setText(String.valueOf(func.getReceita()));
        txtDespesas.setText(String.valueOf(func.getDespesas()));

        // Este caminho agora é o caminho ABSOLUTO para a pasta externa
        this.caminhoFotoAtualSalva = func.getCaminhoFoto();

        if (this.caminhoFotoAtualSalva != null && !this.caminhoFotoAtualSalva.isEmpty()) {
            try {
                // Tenta carregar a foto do caminho EXTERNO
                File file = new File(this.caminhoFotoAtualSalva);
                Image foto = new Image(file.toURI().toString());
                imgFotoPerfil.setImage(foto);
            } catch (Exception e) {
                System.err.println(bundle.getString("log.error.profilePhotoNotFound") + this.caminhoFotoAtualSalva);
                imgFotoPerfil.setImage(carregarAvatarLocal()); // Fallback para o JAR
            }
        } else {
            imgFotoPerfil.setImage(carregarAvatarLocal()); // Fallback para o JAR
        }

        String promptSenhaEdicao = bundle.getString("userRegistration.prompt.passwordEdit");
        txtSenhaOculta.setPromptText(promptSenhaEdicao);
        txtSenhaVisivel.setPromptText(promptSenhaEdicao);
    }

    @FXML
    private void escolherFoto() {
        // ... (método não muda) ...
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("userRegistration.fileChooser.title"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(bundle.getString("userRegistration.fileChooser.filterImages"), "*.jpg", "*.png", "*.jpeg")
        );
        File arquivo = fileChooser.showOpenDialog(imgFotoPerfil.getScene().getWindow());
        if (arquivo != null) {
            this.arquivoFotoSelecionado = arquivo;
            this.bytesFotoPokemon = null;
            try {
                Image foto = new Image(arquivo.toURI().toString());
                imgFotoPerfil.setImage(foto);
            } catch (Exception e) {
                mostrarAlerta(
                        bundle.getString("userRegistration.alert.photoSaveError.title"),
                        bundle.getString("userRegistration.alert.photoSaveError.header"),
                        e.getMessage()
                );
            }
        }
    }

    private String getExtensaoArquivo(String nomeArquivo) {
        // ... (método não muda) ...
        int lastIndex = nomeArquivo.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        }
        return nomeArquivo.substring(lastIndex + 1).toLowerCase();
    }

    @FXML
    private void cadastrarEndereco() {
        // ... (método não muda) ...
        try {
            URL resource = getClass().getResource("/view/CadastroEndereco.fxml");
            if (resource == null) {
                throw new IOException(bundle.getString("exception.fxmlNotFound.cadastroEndereco"));
            }
            FXMLLoader loader = new FXMLLoader(resource, bundle);
            Parent root = loader.load();
            CadastroEnderecoController enderecoController = loader.getController();
            Stage stage = new Stage();
            stage.setTitle(bundle.getString("addressRegistration.title"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(txtNome.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            this.enderecoDoOutroController = enderecoController.getEnderecoSalvo();
            if (this.enderecoDoOutroController != null) {
                lblEndereco.setText(enderecoDoOutroController.enderecoReduzido());
            }
        } catch (IOException e) {
            mostrarAlerta(
                    bundle.getString("alert.error.unexpected.title"),
                    bundle.getString("userRegistration.alert.loadAddressError.header"),
                    e.getMessage()
            );
        }
    }

    private boolean validarCampos() {
        // ... (método não muda) ...
        if (txtNome.getText().isBlank() || txtCpf.getText().isBlank() || txtLogin.getText().isBlank() || txtEmail.getText().isBlank()) {
            mostrarAlerta(
                    bundle.getString("userRegistration.alert.validation.requiredFields.title"),
                    bundle.getString("userRegistration.alert.validation.requiredFields.header1"),
                    null
            );
            return false;
        }
        if (usuarioParaEditar == null && txtSenhaOculta.getText().isBlank()) {
            mostrarAlerta(
                    bundle.getString("userRegistration.alert.validation.requiredFields.title"),
                    bundle.getString("userRegistration.alert.validation.passwordRequired.header"),
                    null
            );
            return false;
        }
        if (perfilCombo.getSelectionModel().isEmpty()) {
            mostrarAlerta(
                    bundle.getString("userRegistration.alert.validation.profileRequired.title"),
                    bundle.getString("userRegistration.alert.validation.profileRequired.header"),
                    null
            );
            return false;
        }
        if (enderecoDoOutroController == null) {
            mostrarAlerta(
                    bundle.getString("userRegistration.alert.validation.addressRequired.title"),
                    bundle.getString("userRegistration.alert.validation.addressRequired.header"),
                    null
            );
            return false;
        }
        Optional<Usuario> usuarioComEsteLogin = usuarioRepository.buscaUsuarioPorLogin(txtLogin.getText());
        if (usuarioComEsteLogin.isPresent()) {
            if (usuarioParaEditar == null || usuarioParaEditar.getId() != usuarioComEsteLogin.get().getId()) {
                mostrarAlerta(
                        bundle.getString("userRegistration.alert.validation.loginInUse.title"),
                        bundle.getString("userRegistration.alert.validation.loginInUse.header"),
                        null
                );
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
        // ... (lógica de validação não muda) ...
        String senha = txtSenhaOculta.getText();
        String cpf = txtCpf.getText();
        String email = txtEmail.getText();
        String hash;
        Perfil perfil = perfilCombo.getValue();

        String caminhoFotoFinalParaSalvar = this.caminhoFotoAtualSalva;

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
            LocalDate dataAdmissao = dpDataAdmissao.getValue();
            if (dataAdmissao == null) {
                dataAdmissao = (usuarioParaEditar instanceof Funcionario && ((Funcionario) usuarioParaEditar).getDataAdmissao() != null)
                        ? ((Funcionario) usuarioParaEditar).getDataAdmissao()
                        : LocalDate.now();
            }
            int periodo = Integer.parseInt(txtPeriodo.getText().isBlank() ? "0" : txtPeriodo.getText());
            double salario = Double.parseDouble(txtSalario.getText().isBlank() ? "0.0" : txtSalario.getText());
            double receita = Double.parseDouble(txtReceita.getText().isBlank() ? "0.0" : txtReceita.getText());
            double despesas = Double.parseDouble(txtDespesas.getText().isBlank() ? "0.0" : txtDespesas.getText());
            String cargo = txtCargo.getText();
            String departamento = txtDepartamento.getText();
            Regime regime = regimeCombo.getValue();


            // Pega o diretório EXTERNO (user.home/.humanize-app-data/fotos_perfil)
            Path diretorioFotosExterno = getPathParaFotos();

            String nomeBaseArquivo;
            if (usuarioParaEditar != null) {
                nomeBaseArquivo = String.valueOf(usuarioParaEditar.getId());
            } else {
                nomeBaseArquivo = cpf.replaceAll("[^0-9]", "");
            }
            if (this.arquivoFotoSelecionado != null) {
                String extensao = getExtensaoArquivo(this.arquivoFotoSelecionado.getName());
                String novoNomeArquivo = nomeBaseArquivo + "." + extensao;
                Path caminhoDestino = diretorioFotosExterno.resolve(novoNomeArquivo);

                Files.copy(this.arquivoFotoSelecionado.toPath(), caminhoDestino, StandardCopyOption.REPLACE_EXISTING);
                caminhoFotoFinalParaSalvar = caminhoDestino.toString(); // Salva o caminho absoluto EXTERNO

            } else if (this.bytesFotoPokemon != null) {
                String novoNomeArquivo = nomeBaseArquivo + ".png";
                Path caminhoDestino = diretorioFotosExterno.resolve(novoNomeArquivo);

                Files.write(caminhoDestino, this.bytesFotoPokemon);
                caminhoFotoFinalParaSalvar = caminhoDestino.toString(); // Salva o caminho absoluto EXTERNO

            } else if (caminhoFotoFinalParaSalvar == null || caminhoFotoFinalParaSalvar.isEmpty()) {
                // Se não há foto selecionada e não havia foto antiga, salva como ""
                // O repo salvará "" e os leitores usarão o avatarPadrão do JAR.
                caminhoFotoFinalParaSalvar = "";
            }

            if (usuarioParaEditar == null) {
                Usuario usuario;
                switch (perfil) {
                    case ADMINISTRADOR -> usuario = new Administrador.AdministradorBuilder()
                            .caminhoFoto(caminhoFotoFinalParaSalvar)
                            // ... (resto do builder) ...
                            .idiomaPreferencial(UserSession.getInstance().getLocale().toLanguageTag().replace("-", "_"))
                            .nome(txtNome.getText()).cpf(cpf).email(email).endereco(enderecoDoOutroController)
                            .login(txtLogin.getText()).senha(hash).perfil(perfil)
                            .matricula(matricula).dataAdmissao(dataAdmissao).periodo(periodo).departamento(departamento)
                            .receita(receita).despesas(despesas).salario(salario)
                            .cargo(cargo).regime(regime)
                            .build();
                    case GESTOR -> usuario = new Gestor.GestorBuilder()
                            .caminhoFoto(caminhoFotoFinalParaSalvar)
                            // ... (resto do builder) ...
                            .idiomaPreferencial(UserSession.getInstance().getLocale().toLanguageTag().replace("-", "_"))
                            .nome(txtNome.getText()).cpf(cpf).email(email).endereco(enderecoDoOutroController)
                            .login(txtLogin.getText()).senha(hash).perfil(perfil)
                            .matricula(matricula).dataAdmissao(dataAdmissao).periodo(periodo).departamento(departamento)
                            .receita(receita).despesas(despesas).salario(salario)
                            .cargo(cargo).regime(regime)
                            .build();
                    case RECRUTADOR -> usuario = new Recrutador.RecrutadorBuilder()
                            .caminhoFoto(caminhoFotoFinalParaSalvar)
                            // ... (resto do builder) ...
                            .idiomaPreferencial(UserSession.getInstance().getLocale().toLanguageTag().replace("-", "_"))
                            .nome(txtNome.getText()).cpf(cpf).email(email).endereco(enderecoDoOutroController)
                            .login(txtLogin.getText()).senha(hash).perfil(perfil)
                            .matricula(matricula).dataAdmissao(dataAdmissao).periodo(periodo).departamento(departamento)
                            .receita(receita).despesas(despesas).salario(salario)
                            .cargo(cargo).regime(regime)
                            .build();
                    default ->
                            usuario = new Funcionario.FuncionarioBuilder()
                                    .caminhoFoto(caminhoFotoFinalParaSalvar)
                                    // ... (resto do builder) ...
                                    .idiomaPreferencial(UserSession.getInstance().getLocale().toLanguageTag().replace("-", "_"))
                                    .nome(txtNome.getText()).cpf(cpf).email(email).endereco(enderecoDoOutroController)
                                    .login(txtLogin.getText()).senha(hash).perfil(perfil)
                                    .matricula(matricula).dataAdmissao(dataAdmissao).periodo(periodo).departamento(departamento)
                                    .receita(receita).despesas(despesas).salario(salario)
                                    .cargo(cargo).regime(regime)
                                    .build();
                }
                usuarioRepository.escreveUsuarioNovo(usuario);

            } else {
                Funcionario func = (Funcionario) usuarioParaEditar;
                func.setCaminhoFoto(caminhoFotoFinalParaSalvar);
                // ... (resto dos 'set') ...
                func.setNome(txtNome.getText());
                func.setCpf(cpf);
                func.setEmail(email);
                func.setLogin(txtLogin.getText());
                func.setPerfil(perfil);
                func.setEndereco(enderecoDoOutroController);
                func.setSenha(hash);
                func.setMatricula(matricula);
                func.setDataAdmissao(dataAdmissao);
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
            mostrarAlerta(
                    bundle.getString("userRegistration.alert.validation.error.title"),
                    bundle.getString("userRegistration.alert.validation.error.header"),
                    e.getMessage()
            );
        } catch (IOException e) {
            mostrarAlerta(
                    bundle.getString("userRegistration.alert.saveError.title"),
                    bundle.getString("userRegistration.alert.saveError.header"),
                    e.getMessage()
            );
        } catch (Exception e) {
            mostrarAlerta(
                    bundle.getString("alert.error.unexpected.title"),
                    bundle.getString("alert.error.unexpected.header"),
                    e.getMessage()
            );
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