package app.humanize.util;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

/**
 * Classe utilitária para gerir e trocar as cenas.
 * <p>
 * Essa classe mantém uma referência estática ao stage principal da aplicação
 * e fornece um métoo para carregar e exibir as diferentes views FXML.
 * </p>
 */
public class ScreenController {
    /**
     * Janela principal da aplicação.
     */
    private static Stage stage;

    /**
     * Define a janela principal a ser usada.
     *
     * @param s O Stage principal da aplicação JavaFX.
     */
    public static void setStage(Stage s) {
        stage = s;
    }

    /**
     * Carrega um novo arquivo FXML e o define como a cena atual no palco principal.
     * <p>
     * Também define o título e o ícone da janela. Se o arquivo FXML não puder ser carregado,
     * imprime um rastreamento de erro no console.
     * </p>
     *
     * @param fxmlPath O caminho para o arquivo FXML.
     */
    public static void changeScene(String fxmlPath) {
        Image image = new Image("/humanize-logo.png");
        try {
            if (stage == null) {
                System.err.println("Erro: O Stage não foi definido no ScreenController.");
                return;
            }
            Parent root = FXMLLoader.load(Objects.requireNonNull(ScreenController.class.getResource(fxmlPath), "Não foi possível carregar o arquivo FXML: " + fxmlPath));
            stage.setTitle("Humanize");

            if (stage.getIcons().isEmpty()) {
                stage.getIcons().add(image);
            }
            stage.setScene(new Scene(root, 1080, 700));
            stage.show();

        } catch (IOException e) {
            System.err.println("Falha ao trocar a cena para: " + fxmlPath);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Recurso FXML não encontrado: " + fxmlPath);
            e.printStackTrace();
        }
    }
}