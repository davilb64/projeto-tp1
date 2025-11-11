package app.humanize.controller;

import app.humanize.model.Relatorio;
import app.humanize.model.TipoRelatorio;
import app.humanize.model.Usuario;
import app.humanize.repository.RelatorioRepository;
import app.humanize.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class GerarRelatorioController {
    @FXML private Label lblId;
    @FXML private Label lblData;
    @FXML private Label lblUser;
    @FXML private ComboBox<TipoRelatorio> tipoCombo;

    private Usuario usuarioLogado;
    private final RelatorioRepository relatorioRepository = RelatorioRepository.getInstance();
    private ResourceBundle bundle;

    @FXML public void initialize(){
        this.bundle = UserSession.getInstance().getBundle();
        usuarioLogado = UserSession.getInstance().getUsuarioLogado();

        if (relatorioRepository != null) {
            lblId.setText(String.valueOf(relatorioRepository.getProximoId()));
        } else {
            lblId.setText(bundle.getString("report.label.error"));
            System.err.println(bundle.getString("log.error.repoNotInitialized.report"));
        }

        lblData.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (usuarioLogado != null) {
            lblUser.setText(usuarioLogado.getNome());
        } else {
            lblUser.setText(bundle.getString("report.label.unknownUser"));
            System.err.println(bundle.getString("log.error.sessionUserNull.report"));
            mostrarAlerta(
                    bundle.getString("report.alert.criticalError.title"),
                    bundle.getString("report.alert.criticalError.header"),
                    bundle.getString("report.alert.criticalError.content")
            );
            return;
        }

        if (tipoCombo != null) {
            // Popula o ComboBox com os valores do ENUM ATUALIZADO
            tipoCombo.getItems().setAll(TipoRelatorio.values());

            // Configura a Célula para exibir o nome traduzido
            tipoCombo.setCellFactory(lv -> new ListCell<TipoRelatorio>() {
                @Override
                protected void updateItem(TipoRelatorio item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : getTraducaoTipoRelatorio(item));
                }
            });
            // Configura o Botão (o que aparece quando está selecionado)
            tipoCombo.setButtonCell(new ListCell<TipoRelatorio>() {
                @Override
                protected void updateItem(TipoRelatorio item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : getTraducaoTipoRelatorio(item));
                }
            });
        } else {
            System.err.println(bundle.getString("log.error.fxmlInject.tipoCombo"));
        }
    }

    /**
     * Busca a tradução do tipo de relatório.
     * Ex: "report.type.LISTA_USUARIOS" -> "Lista de Usuários"
     */
    private String getTraducaoTipoRelatorio(TipoRelatorio tipo) {
        if (tipo == null) return null;
        // O padrão da chave é "report.type." + NOME_DO_ENUM
        String key = "report.type." + tipo.name();
        return bundle.containsKey(key) ? bundle.getString(key) : tipo.name();
    }

    @FXML public void cancelar(){
        fecharJanela();
    }

    @FXML public void salvarRegistroRelatorio(){
        TipoRelatorio tipoSelecionado = tipoCombo.getValue();

        if (tipoSelecionado == null) {
            mostrarAlerta(
                    bundle.getString("report.alert.invalidSelection.title"),
                    bundle.getString("report.alert.invalidSelection.header"),
                    null
            );
            return;
        }
        if (usuarioLogado == null) {
            mostrarAlerta(
                    bundle.getString("report.alert.criticalError.title"),
                    bundle.getString("report.alert.criticalError.header"),
                    bundle.getString("report.alert.criticalError.contentSave")
            );
            return;
        }

        try{
            Relatorio novoRegistro = new Relatorio();
            novoRegistro.setId(Integer.parseInt(lblId.getText()));
            novoRegistro.setTipoRelatorio(tipoSelecionado);
            novoRegistro.setDataGeracao(LocalDate.now());
            novoRegistro.setResponsavel(usuarioLogado);

            // --- MUDANÇA AQUI ---
            // Este switch é para lógicas futuras (ex: parâmetros)
            // Por enquanto, apenas registramos que eles existem.
            switch (tipoSelecionado){
                case LISTA_USUARIOS:
                    // Nenhuma lógica extra necessária
                    break;
                case CONTRACHEQUE_GERAL: // NOVO
                    // Nenhuma lógica extra necessária por enquanto
                    break;
                case FINANCEIRO_GERAL: // NOVO
                    // Nenhuma lógica extra necessária por enquanto
                    break;
                default:
                    System.out.println(bundle.getString("log.info.paramsNotImplemented") + tipoSelecionado);
                    break;
            }
            // --- FIM DA MUDANÇA ---

            relatorioRepository.escreverRelatorioNovo(novoRegistro);
            System.out.println(bundle.getString("log.info.reportSaved"));
            fecharJanela();

        } catch (IOException e) {
            mostrarAlerta(
                    bundle.getString("report.alert.saveError.title"),
                    bundle.getString("report.alert.saveError.header"),
                    e.getMessage()
            );
            e.printStackTrace();
        } catch (NumberFormatException e){
            mostrarAlerta(
                    bundle.getString("report.alert.internalError.title"),
                    bundle.getString("report.alert.internalError.header"),
                    e.getMessage()
            );
        } catch (Exception e) {
            mostrarAlerta(
                    bundle.getString("report.alert.unexpectedError.title"),
                    bundle.getString("report.alert.unexpectedError.header"),
                    e.getMessage()
            );
            e.printStackTrace();
        }
    }

    @FXML
    private void fecharJanela() {
        if (lblId != null && lblId.getScene() != null && lblId.getScene().getWindow() != null) {
            Stage stage = (Stage) lblId.getScene().getWindow();
            stage.close();
        } else {
            System.err.println(bundle.getString("log.error.closeWindowError.report"));
        }
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}