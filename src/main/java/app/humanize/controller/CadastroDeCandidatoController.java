package app.humanize.controller;

import app.humanize.model.Candidato;
import app.humanize.model.Vaga;
import app.humanize.repository.CandidatoRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import app.humanize.repository.VagaRepository;


import java.io.IOException;
import java.time.LocalDate;

public class CadastroDeCandidatoController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtFormacao;
    @FXML private TextField txtDisponibilidade;
    @FXML private TextField txtPretencao;
    @FXML private TextArea txtExperiencia;
    @FXML private ComboBox<Vaga> comboVaga;
    @FXML private Button btnUpload;
    @FXML private Button btnSalvar;
    @FXML private Button btnVisualizar;

    @FXML private javafx.scene.control.Label lblArquivo;

    private CandidatosAdmController controllerPai;

    public void setControllerPai(CandidatosAdmController controllerPai) {
        this.controllerPai = controllerPai;
    }


    private String caminhoDocumentoAtual = null;


    private Candidato candidatoEmEdicao = null; // 🔹 usado quando estiver editando

    @FXML
    private void initialize() {
        try {
            // Busca todas as vagas do CSV via repositório
            VagaRepository vagaRepo = VagaRepository.getInstance();
            comboVaga.getItems().addAll(vagaRepo.getTodasVagas());

            // Define como cada vaga será mostrada (ex: apenas o cargo)
            comboVaga.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Vaga vaga, boolean empty) {
                    super.updateItem(vaga, empty);
                    setText((empty || vaga == null) ? null : vaga.getCargo());
                }
            });
            comboVaga.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Vaga vaga, boolean empty) {
                    super.updateItem(vaga, empty);
                    setText((empty || vaga == null) ? null : vaga.getCargo());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao carregar vagas do arquivo CSV: " + e.getMessage());
        }
    }


    private Vaga criarVaga(String nome) {
        Vaga v = new Vaga();
        v.setCargo(nome);
        return v;
    }

    // 🔹 chamado quando clicamos em "Editar" na tabela
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
        comboVaga.setValue(candidato.getVaga());
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
                    txtExperiencia.getText().trim().isEmpty() ||
                    comboVaga.getValue() == null) {

                mostrarErro("Por favor, preencha todos os campos antes de salvar.");
                return;
            }

            double pretencao = txtPretencao.getText().isEmpty() ? 0.0 : Double.parseDouble(txtPretencao.getText());

            if (candidatoEmEdicao == null) {
                // ➕ novo candidato
                Candidato novo = new Candidato.CandidatoBuilder()
                        .nome(txtNome.getText())
                        .cpf(txtCpf.getText())
                        .email(txtEmail.getText())
                        .telefone(txtTelefone.getText())
                        .formacao(txtFormacao.getText())
                        .disponibilidade(txtDisponibilidade.getText())
                        .pretencaoSalarial(pretencao)
                        .experiencia(txtExperiencia.getText())
                        .vaga(comboVaga.getValue())
                        .dataCadastro(LocalDate.now())
                        .build();

                novo.setCaminhoDocumento(caminhoDocumentoAtual);
                CandidatoRepository.getInstance().adicionar(novo);

                mostrarAlerta("Cadastro realizado com sucesso!");
                if (controllerPai != null) {
                    controllerPai.showStatus();
                }
            } else {
                // ✏️ edição de candidato existente
                candidatoEmEdicao.setNome(txtNome.getText());
                candidatoEmEdicao.setCpf(txtCpf.getText());
                candidatoEmEdicao.setEmail(txtEmail.getText());
                candidatoEmEdicao.setTelefone(txtTelefone.getText());
                candidatoEmEdicao.setFormacao(txtFormacao.getText());
                candidatoEmEdicao.setDisponibilidade(txtDisponibilidade.getText());
                candidatoEmEdicao.setPretencaoSalarial(pretencao);
                candidatoEmEdicao.setExperiencia(txtExperiencia.getText());
                candidatoEmEdicao.setVaga(comboVaga.getValue());

                CandidatoRepository.getInstance().atualizar();

                mostrarAlerta("Alterações salvas com sucesso!");
                if (controllerPai != null) {
                    controllerPai.showStatus();
                }

            }



        } catch (IOException e) {
            mostrarErro("Erro ao salvar candidato: " + e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro inesperado: " + e.getMessage());
        }
    }

    @FXML
    private void uploadDocumentos() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Selecionar Documento");

            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Documentos PDF", "*.pdf"),
                    new javafx.stage.FileChooser.ExtensionFilter("Documentos Word", "*.docx"),
                    new javafx.stage.FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"),
                    new javafx.stage.FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
            );

            java.io.File arquivoSelecionado = fileChooser.showOpenDialog(btnUpload.getScene().getWindow());
            if (arquivoSelecionado != null) {
                // Salva em pasta externa (fora do JAR) para garantir que exista sempre como arquivo
                java.io.File pastaDestino = new java.io.File(System.getProperty("user.home"), "humanize_uploads");
                if (!pastaDestino.exists()) pastaDestino.mkdirs();

                java.nio.file.Path destino = java.nio.file.Paths.get(pastaDestino.getAbsolutePath(), arquivoSelecionado.getName());
                java.nio.file.Files.copy(arquivoSelecionado.toPath(), destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // guarda em memória/controller e também no candidato em edição (se houver)
                caminhoDocumentoAtual = destino.toString();

                if (candidatoEmEdicao != null) {
                    candidatoEmEdicao.setCaminhoDocumento(caminhoDocumentoAtual);
                    CandidatoRepository.getInstance().atualizar(); // 🔹 grava o novo caminho no CSV
                }


                lblArquivo.setText("Arquivo: " + arquivoSelecionado.getName());
                btnUpload.setVisible(false); btnUpload.setManaged(false);
                btnVisualizar.setVisible(true); btnVisualizar.setManaged(true);

                mostrarAlerta("Arquivo '" + arquivoSelecionado.getName() + "' salvo com sucesso em:\n" + destino.toString());
            } else {
                mostrarErro("Nenhum arquivo foi selecionado.");
            }
        } catch (java.nio.file.FileAlreadyExistsException e) {
            mostrarErro("Já existe um arquivo com esse nome na pasta de uploads.");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao salvar arquivo: " + e.getMessage());
        }
    }



    @FXML
    private void visualizarDocumento() {
        try {
            if (caminhoDocumentoAtual == null) {
                mostrarErro("Nenhum documento anexado para este candidato.");
                return;
            }

            java.io.File arquivo = new java.io.File(caminhoDocumentoAtual);
            if (!arquivo.exists() || !arquivo.canRead()) {
                mostrarErro("Arquivo não encontrado ou inacessível: " + caminhoDocumentoAtual);
                return;
            }

            // Tenta usar java.awt.Desktop primeiro (Windows/Mac/Linux com GUI)
            if (java.awt.Desktop.isDesktopSupported()) {
                try {
                    java.awt.Desktop.getDesktop().open(arquivo);
                    return;
                } catch (UnsupportedOperationException | IOException | SecurityException ex) {
                    // segue para fallback
                    ex.printStackTrace();
                }
            }

            // Fallback multiplataforma para abrir o arquivo
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb = null;

            if (os.contains("win")) {
                // Windows: 'cmd /c start "" "file"'
                pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", arquivo.getAbsolutePath());
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", arquivo.getAbsolutePath());
            } else {
                // Linux / others: tenta xdg-open
                pb = new ProcessBuilder("xdg-open", arquivo.getAbsolutePath());
            }

            Process p = pb.start();
            // opcional: não bloquear; se o comando falhar, avisar
            int exit = p.waitFor();
            if (exit != 0) {
                mostrarErro("Não foi possível abrir o arquivo com o comando do sistema (exit=" + exit + ").");
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            mostrarErro("A ação foi interrompida.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            mostrarErro("Erro ao tentar abrir o arquivo: " + ioe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro inesperado ao abrir o arquivo: " + e.getMessage());
        }
    }




    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void irParaTelaStatusCandidato() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/StatusDaCandidatura.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnSalvar.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Status das Candidaturas");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de status: " + e.getMessage());
        }
    }

}
