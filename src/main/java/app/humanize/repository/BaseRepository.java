package app.humanize.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Classe base para todos os repositórios.
 * Centraliza a lógica de onde os arquivos de dados (CSV, JSON, etc.) são salvos.
 * Garante que os dados sejam salvos na pasta 'home' do usuário,
 * permitindo que o .jar funcione corretamente.
 */
public abstract class BaseRepository {

    /**
     * Retorna o File (arquivo) de persistência.
     * O arquivo estará localizado em: [Pasta Home do Usuário]/.humanize-app-data/[nomeDoArquivo]
     *
     * @param nomeDoArquivo O nome do arquivo (ex: "usuarios.csv")
     * @return Um objeto File apontando para o local de salvamento correto.
     */
    protected File getArquivoDePersistencia(String nomeDoArquivo) {
        String userHome = System.getProperty("user.home");
        File pastaDeDados = new File(userHome, ".humanize-app-data");
        if (!pastaDeDados.exists()) {
            pastaDeDados.mkdirs();
        }
        return new File(pastaDeDados, nomeDoArquivo);
    }

    protected void copiarArquivoDefaultDeResources(String nomeArquivoResource, File arquivoDestino) throws IOException {
        String resourcePath = "/" + nomeArquivoResource;

        InputStream streamFonte = getClass().getResourceAsStream(resourcePath);

        if (streamFonte == null) {
            throw new IOException("Arquivo de resource padrão não encontrado no JAR: " + resourcePath);
        }

        Files.copy(streamFonte, arquivoDestino.toPath());

        streamFonte.close();
    }
}