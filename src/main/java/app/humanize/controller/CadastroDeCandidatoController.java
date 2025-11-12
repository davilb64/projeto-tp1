package app.humanize.controller;

import app.humanize.exceptions.CpfInvalidoException;
import app.humanize.model.Candidato;
import app.humanize.repository.CandidatoRepository;
import app.humanize.service.validacoes.ValidaCpf;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.stage.FileChooser;

public class CadastroDeCandidatoController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtFormacao;
    @FXML private TextField txtDisponibilidade;
    @FXML private TextField txtPretencao;
    @FXML private TextArea txtExperiencia;
    @FXML private Button btnUpload;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancel;
    @FXML private javafx.scene.control.Label lblArquivo;

    private final ValidaCpf validaCpf = new ValidaCpf();
    private String caminhoDocumentoAtual = null;
    private Candidato candidatoEmEdicao = null;
    private ResourceBundle bundle;

    @FXML
    private void initialize() {
        this.bundle = UserSession.getInstance().getBundle();

        lblArquivo.setText(bundle.getString("candidateRegistration.label.noFileSelected"));
    }

    public void prepararParaEdicao(Candidato candidato) {
        this.candidatoEmEdicao = candidato;
        txtNome.setText(candidato.getNome());
        txtCpf.setText(candidato.getCpf());
        txtEmail.setText(candidato.getEmail());
        txtTelefone.setText(candidato.getTelefone());
        txtFormacao.setText(candidato.getFormacao());
        txtDisponibilidade.setText(candidato.getDisponibilidade());
        txtPretencao.setText(String.valueOf(candidato.getPretencaoSalarial()));
        txtExperiencia.setText(candidato.getExperiencia());

        this.caminhoDocumentoAtual = candidato.getCaminhoDocumento();
        atualizarLabelArquivo();

        txtCpf.setDisable(true);
    }

    public void prepararParaVisualizacao(Candidato candidato) {
        this.candidatoEmEdicao = candidato;
        txtNome.setText(candidato.getNome());
        txtCpf.setText(candidato.getCpf());
        txtEmail.setText(candidato.getEmail());
        txtTelefone.setText(candidato.getTelefone());
        txtFormacao.setText(candidato.getFormacao());
        txtDisponibilidade.setText(candidato.getDisponibilidade());
        txtPretencao.setText(String.valueOf(candidato.getPretencaoSalarial()));
        txtExperiencia.setText(candidato.getExperiencia());

        this.caminhoDocumentoAtual = candidato.getCaminhoDocumento();
        atualizarLabelArquivo();

        txtCpf.setEditable(false);
        txtNome.setEditable(false);
        txtEmail.setEditable(false);
        txtTelefone.setEditable(false);
        txtFormacao.setEditable(false);
        txtDisponibilidade.setEditable(false);
        txtPretencao.setEditable(false);
        txtExperiencia.setEditable(false);
    }

    private void atualizarLabelArquivo() {

    }

    @FXML
    private void salvarCandidato() {
        try {

            if (txtNome.getText().trim().isEmpty() ||
                    txtCpf.getText().trim().isEmpty() ||
                    txtEmail.getText().trim().isEmpty() ||
                    txtTelefone.getText().trim().isEmpty() ||
                    txtFormacao.getText().trim().isEmpty() ||
                    txtDisponibilidade.getText().trim().isEmpty() ||
                    txtPretencao.getText().trim().isEmpty() ||
                    txtExperiencia.getText().trim().isEmpty()) {

                mostrarErro("candidateRegistration.alert.fillError");
                return;
            }

            double pretencao = txtPretencao.getText().isEmpty() ? 0.0 : Double.parseDouble(txtPretencao.getText());

            if (candidatoEmEdicao == null) {
                String cpf = txtCpf.getText();
                validaCpf.validaCpf(cpf);
                Candidato novo = new Candidato.CandidatoBuilder()
                        .nome(txtNome.getText())
                        .cpf(cpf)
                        .email(txtEmail.getText())
                        .telefone(txtTelefone.getText())
                        .formacao(txtFormacao.getText())
                        .disponibilidade(txtDisponibilidade.getText())
                        .pretencaoSalarial(pretencao)
                        .experiencia(txtExperiencia.getText())
                        .dataCadastro(LocalDate.now())
                        .build();

                novo.setCaminhoDocumento(caminhoDocumentoAtual);
                CandidatoRepository.getInstance().adicionar(novo);

                mostrarAlerta("candidateRegistration.alert.registerComplete");
                fecharJanela();

            } else {
                String cpf = txtCpf.getText();
                validaCpf.validaCpf(cpf);
                candidatoEmEdicao.setNome(txtNome.getText());
                candidatoEmEdicao.setCpf(cpf);
                candidatoEmEdicao.setEmail(txtEmail.getText());
                candidatoEmEdicao.setTelefone(txtTelefone.getText());
                candidatoEmEdicao.setFormacao(txtFormacao.getText());
                candidatoEmEdicao.setDisponibilidade(txtDisponibilidade.getText());
                candidatoEmEdicao.setPretencaoSalarial(pretencao);
                candidatoEmEdicao.setExperiencia(txtExperiencia.getText());

                CandidatoRepository.getInstance().atualizar();

                mostrarAlerta("candidateRegistration.alert.changesSaved");
                fecharJanela();
            }

        }
        catch (CpfInvalidoException e) {
            mostrarErro("candidateRegistration.alert.invalidCPF", e.getMessage());
        }
        catch (IOException e) {
            mostrarErro("candidateRegistration.alert.saveError", e.getMessage());
        } catch (Exception e) {
            mostrarErro("alert.error.unexpected.header", e.getMessage());
        }
    }

    public void esconderBotaoEditar() {
        btnSalvar.setVisible(false);
        btnCancel.setVisible(false);
    }


    @FXML
    private void uploadDocumentos() {
        java.io.File arquivoSelecionado = null;
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(bundle.getString("candidateRegistration.fileChooser.title"));

            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(bundle.getString("candidateRegistration.fileChooser.pdf"), "*.pdf"),
                    new FileChooser.ExtensionFilter(bundle.getString("candidateRegistration.fileChooser.doc"), "*.docx"),
                    new FileChooser.ExtensionFilter(bundle.getString("candidateRegistration.fileChooser.img"), "*.png", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter(bundle.getString("candidateRegistration.fileChooser.all"), "*.*")
            );

            arquivoSelecionado = fileChooser.showOpenDialog(btnUpload.getScene().getWindow());
            if (arquivoSelecionado != null) {
                java.io.File pastaDestino = new java.io.File(System.getProperty("user.home"), "humanize_uploads");
                if (!pastaDestino.exists()) pastaDestino.mkdirs();

                java.nio.file.Path destino = java.nio.file.Paths.get(pastaDestino.getAbsolutePath(), arquivoSelecionado.getName());
                java.nio.file.Files.copy(arquivoSelecionado.toPath(), destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                caminhoDocumentoAtual = destino.toString();

                if (candidatoEmEdicao != null) {
                    candidatoEmEdicao.setCaminhoDocumento(caminhoDocumentoAtual);
                    CandidatoRepository.getInstance().atualizar();
                }

                String successMsg = bundle.getString("candidateRegistration.alert.uploadSuccess1") + " '" +
                        arquivoSelecionado.getName() + "' " +
                        bundle.getString("candidateRegistration.alert.uploadSuccess2") + "\n" + destino;

                mostrarAlerta(successMsg, true);
                atualizarLabelArquivo();

            } else {
                mostrarErro("candidateRegistration.alert.noFileSelected");
            }
        } catch (java.nio.file.FileAlreadyExistsException e) {
            mostrarErro("candidateRegistration.alert.fileExists");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("candidateRegistration.alert.saveError", e.getMessage());
        }
    }

    @FXML
    private void visualizarDocumento() {
        try {
            if (caminhoDocumentoAtual == null) {
                mostrarErro("candidateRegistration.alert.noDocAttached");
                return;
            }

            java.io.File arquivo = new java.io.File(caminhoDocumentoAtual);
            if (!arquivo.exists() || !arquivo.canRead()) {
                mostrarErro("candidateRegistration.alert.fileNotFound", caminhoDocumentoAtual);
                return;
            }

            if (java.awt.Desktop.isDesktopSupported()) {
                try {
                    java.awt.Desktop.getDesktop().open(arquivo);
                    return;
                } catch (UnsupportedOperationException | IOException | SecurityException ex) {
                    ex.printStackTrace();
                }
            }

            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", arquivo.getAbsolutePath());
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", arquivo.getAbsolutePath());
            } else {
                pb = new ProcessBuilder("xdg-open", arquivo.getAbsolutePath());
            }

            Process p = pb.start();
            int exit = p.waitFor();
            if (exit != 0) {
                mostrarErro("candidateRegistration.alert.openError", String.valueOf(exit));
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            mostrarErro("candidateRegistration.alert.interrupted");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            mostrarErro("candidateRegistration.alert.openIOError", ioe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("candidateRegistration.alert.openErrorUnexpected", e.getMessage());
        }
    }


    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String bundleKey) {
        mostrarAlerta(bundleKey, true);
    }

    private void mostrarAlerta(String bundleKey, boolean isInfo) {
        Alert.AlertType type = isInfo ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING;
        Alert alert = new Alert(type);
        alert.setTitle(isInfo ? bundle.getString("alert.success.title") : bundle.getString("userManagement.alert.attention"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString(bundleKey));
        alert.showAndWait();
    }

    private void mostrarErro(String bundleKey) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("alert.error.reload.title"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString(bundleKey));
        alert.showAndWait();
    }

    private void mostrarErro(String bundleKey, String context) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("alert.error.reload.title"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString(bundleKey) + " " + context);
        alert.showAndWait();
    }

}