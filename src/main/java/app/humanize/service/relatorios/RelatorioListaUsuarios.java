package app.humanize.service.relatorios;
// ... (imports)

import app.humanize.model.Perfil;
import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

public class RelatorioListaUsuarios implements IGeradorRelatorio {

    private final UsuarioRepository usuarioRepo = UsuarioRepository.getInstance();

    @Override
    public String getNome() {
        return "Lista Completa de Usuários";
    }

    @Override
    public ReportData coletarDados() {
        String tituloRelatorio = "Lista Completa de Usuários";

        List<String> headers = List.of("ID", "Nome", "Email", "Login", "Perfil");

        List<List<String>> rows = new ArrayList<>();
        List<Usuario> todosUsuarios = usuarioRepo.getTodosUsuarios();
        for (Usuario u : todosUsuarios) {
            rows.add(List.of(
                    String.valueOf(u.getId()),
                    u.getNome(),
                    u.getEmail(),
                    u.getLogin(),
                    u.getPerfil().toString()
            ));
        }

        // 4. Retorna o DTO com o título
        return new ReportData(tituloRelatorio, headers, rows); // <-- Passa o título
    }

    @Override
    public boolean podeGerar(Usuario usuarioLogado) {
        return usuarioLogado.getPerfil() == Perfil.ADMINISTRADOR;
    }
}