package app.humanize.repository;

import app.humanize.model.*;
import app.humanize.util.EstadosBrasileiros;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {

    private static final UsuarioRepository instance = new UsuarioRepository();
    private final String arquivoCsv = "./src/main/resources/usuarios.csv";
    private final List<Usuario> usuariosEmMemoria;

    private UsuarioRepository() {
        this.usuariosEmMemoria = new ArrayList<>();
        this.carregarUsuariosDoCSV();
    }

    public static UsuarioRepository getInstance() {
        return instance;
    }

    public List<Usuario> getTodosUsuarios() {
        return new ArrayList<>(this.usuariosEmMemoria);
    }

    public Optional<Usuario> buscaUsuarioPorLogin(String login) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getLogin().equalsIgnoreCase(login))
                .findFirst();
    }


    public Optional<Usuario> buscaPorCpf(String cpf) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getCpf().equalsIgnoreCase(cpf))
                .findFirst();
    }

    public List<Usuario> buscaPorPerfil(Perfil perfil) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getPerfil().equals(perfil))
                .toList();
    }

    public int getQtdUsuarios(){
        return this.usuariosEmMemoria.size();
    }

    public void escreveUsuarioNovo(Usuario usuario) throws IOException {
        int proximoId = getProximoId();
        usuario.setId(proximoId);

        this.usuariosEmMemoria.add(usuario);
        this.persistirAlteracoesNoCSV();
    }

    public int getProximoId() {
        return this.usuariosEmMemoria.stream()
                .mapToInt(Usuario::getId)
                .max()
                .orElse(0)
                + 1;
    }

    private void carregarUsuariosDoCSV() {
        File arquivo = new File(arquivoCsv);
        if (!arquivo.exists()) {
            return;
        }
        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
            leitor.readLine(); // Pula o cabeçalho
            String linha;
            while ((linha = leitor.readLine()) != null) {
                Usuario usuario = parseUsuarioDaLinhaCsv(linha);
                if (usuario != null) {
                    this.usuariosEmMemoria.add(usuario);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar usuários do arquivo CSV: " + e.getMessage());
        }
    }

    private void persistirAlteracoesNoCSV() throws IOException {
        try (FileWriter escritor = new FileWriter(arquivoCsv, false)) {
            escritor.write("ID;Nome;CPF;Email;Endereco;Login;Senha;Perfil;Matricula;Periodo;DataEmissao;Receita;Despesas;Salario;\n");
            for (Usuario usuario : this.usuariosEmMemoria) {
                escritor.write(formatarUsuarioParaCSV(usuario));
            }
        }
    }

    private Usuario parseUsuarioDaLinhaCsv(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length < 14) return null;

        try {
            int id = Integer.parseInt(campos[0]);
            Perfil perfil = Perfil.valueOf(campos[7]);

            Endereco endereco = new Endereco.EnderecoBuilder()
                    .logradouro(campos[4].split(",")[0].trim())
                    .numero(Integer.parseInt(campos[4].split(",")[1].trim()))
                    .bairro(campos[4].split(",")[2].trim())
                    .cidade(campos[4].split(",")[3].trim())
                    .estado(EstadosBrasileiros.valueOf(campos[4].split(",")[4].trim()))
                    .cep(campos[4].split(",")[5].trim())
                    .build();

            Usuario usuario;
            if (perfil == Perfil.FUNCIONARIO) {
                usuario = new Funcionario.FuncionarioBuilder()
                        .matricula(Integer.parseInt(campos[8]))
                        .periodo(Integer.parseInt(campos[9]))
                        .dataEmissao(LocalDate.parse(campos[10]))
                        .receita(Double.parseDouble(campos[11]))
                        .despesas(Double.parseDouble(campos[12]))
                        .salario(Double.parseDouble(campos[13]))
                        .build();
            } else if (perfil == Perfil.ADMINISTRADOR) {
                usuario = new Administrador.AdministradorBuilder().build();
            } else if (perfil == Perfil.GESTOR) {
                usuario = new Gestor.GestorBuilder().build();
            } else { // RECRUTADOR
                usuario = new Recrutador.RecrutadorBuilder().build();
            }

            usuario.setId(id);
            usuario.setNome(campos[1]);
            usuario.setCpf(campos[2]);
            usuario.setEmail(campos[3]);
            usuario.setEndereco(endereco);
            usuario.setLogin(campos[5]);
            usuario.setSenha(campos[6]);
            usuario.setPerfil(perfil);

            return usuario;

        } catch (Exception e) {
            System.err.println("Falha ao parsear linha do CSV: '" + linha + "'. Erro: " + e.getMessage());
            return null;
        }
    }

    private String formatarUsuarioParaCSV(Usuario usuario) {
        StringBuilder sb = new StringBuilder();
        sb.append(usuario.getId()).append(";");
        sb.append(usuario.getNome()).append(";");
        sb.append(usuario.getCpf()).append(";");
        sb.append(usuario.getEmail()).append(";");

        if (usuario.getEndereco() != null) {
            Endereco endereco = usuario.getEndereco();
            sb.append(String.join( ",", endereco.getLogradouro(), String.valueOf(endereco.getNumero()), endereco.getBairro(), endereco.getCidade(), String.valueOf(endereco.getEstado()) , endereco.getCep()));
        }
        sb.append(";");

        sb.append(usuario.getLogin()).append(";");
        sb.append(usuario.getSenha()).append(";");
        sb.append(usuario.getPerfil()).append(";");

        if (usuario instanceof Funcionario f) {
            sb.append(f.getMatricula()).append(";");
            sb.append(f.getPeriodo()).append(";");
            sb.append(f.getDataEmissao()).append(";");
            sb.append(f.getReceita()).append(";");
            sb.append(f.getDespesas()).append(";");
            sb.append(f.getSalario()).append(";");
        } else {
            sb.append(";;;;;;");
        }
        sb.append("\n");
        return sb.toString();
    }

    public void excluirUsuario(Usuario usuarioParaExcluir) throws IOException {
        if (usuarioParaExcluir == null) {
            return;
        }
        boolean removido = this.usuariosEmMemoria.removeIf(usuario -> usuario.getId() == usuarioParaExcluir.getId());
        if (removido) {
            persistirAlteracoesNoCSV();
        }
    }

    public void atualizarUsuario() throws IOException {
        persistirAlteracoesNoCSV();
    }
}