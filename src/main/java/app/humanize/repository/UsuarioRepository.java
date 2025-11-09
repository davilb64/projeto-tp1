package app.humanize.repository;

import app.humanize.model.*;
import app.humanize.util.EstadosBrasileiros;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // ... (getTodosUsuarios, getRecrutadores, etc. não mudam) ...
    public List<Usuario> getTodosUsuarios() {
        return new ArrayList<>(this.usuariosEmMemoria);
    }
    public List<Usuario> getRecrutadores() {
        List<Usuario> recrutadores = new ArrayList<>();
        for(Usuario usuario : this.usuariosEmMemoria) {
            if (usuario instanceof Recrutador) {
                recrutadores.add(usuario);
            }
        }
        return recrutadores;
    }
    public List<Usuario> getFuncionarios() {
        return this.usuariosEmMemoria.stream()
                .filter(usuario -> usuario.getPerfil() == Perfil.FUNCIONARIO)
                .collect(Collectors.toList());
    }
    public List<Funcionario> getUsuariosInstanceofFuncionario() {
        return usuariosEmMemoria.stream()
                .filter(u -> u instanceof Funcionario)
                .map(u -> (Funcionario) u)
                .filter(f -> f.getPerfil() != Perfil.ADMINISTRADOR)
                .toList();
    }
    public Optional<Usuario> buscaUsuarioPorLogin(String login) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getLogin().equalsIgnoreCase(login))
                .findFirst();
    }
    public Optional<Usuario> buscaUsuarioPorId(int id) {
        return this.usuariosEmMemoria.stream()
                .filter(u -> u.getId() == id)
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

        if (usuario.getIdiomaPreferencial() == null || usuario.getIdiomaPreferencial().isEmpty()) {
            usuario.setIdiomaPreferencial("pt_BR");
        }

        // Define a data de admissão se for um Funcionario e estiver nula
        if (usuario instanceof Funcionario f && f.getDataAdmissao() == null) {
            f.setDataAdmissao(LocalDate.now());
        }

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
            leitor.readLine();
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
            // CABEÇALHO ATUALIZADO (19 COLUNAS)
            escritor.write("ID;Nome;CPF;Email;Endereco;Login;Senha;Perfil;IdiomaPreferencial;Matricula;DataAdmissao;Periodo;Receita;Despesas;Salario;Cargo;Regime;Departamento;CaminhoFoto;\n");
            for (Usuario usuario : this.usuariosEmMemoria) {
                escritor.write(formatarUsuarioParaCSV(usuario));
            }
        }
    }

    private Usuario parseUsuarioDaLinhaCsv(String linha) {
        String[] campos = linha.split(";", -1);
        // ATUALIZADO: ESPERA 19 CAMPOS
        if (campos.length < 19) {
            System.err.println("Linha CSV inválida (campos: " + campos.length + ", esperado 19): " + linha);
            return null;
        }

        try {
            int id = Integer.parseInt(campos[0]);
            String nome = campos[1];
            String cpf = campos[2];
            String email = campos[3];
            // campo 4 é Endereço
            String login = campos[5];
            String senha = campos[6];
            Perfil perfil = Perfil.valueOf(campos[7].trim().toUpperCase());
            String idiomaPreferencial = campos[8].trim();
            if (idiomaPreferencial.isEmpty()) {
                idiomaPreferencial = "pt_BR";
            }

            Endereco endereco = null;
            if (!campos[4].trim().isEmpty()) {
                String[] partesEndereco = campos[4].split(",");
                if (partesEndereco.length == 6) {
                    endereco = new Endereco.EnderecoBuilder()
                            .logradouro(partesEndereco[0].trim())
                            .numero(Integer.parseInt(partesEndereco[1].trim()))
                            .bairro(partesEndereco[2].trim())
                            .cidade(partesEndereco[3].trim())
                            .estado(EstadosBrasileiros.valueOf(partesEndereco[4].trim().toUpperCase()))
                            .cep(partesEndereco[5].trim())
                            .build();
                } else {
                    System.err.println("Formato de endereço inválido na linha: " + linha);
                }
            }

            // ÍNDICES ATUALIZADOS (de 9 a 18)
            int matricula = Integer.parseInt(campos[9].trim().isEmpty() ? "0" : campos[9].trim());

            // NOVO CAMPO (ÍNDICE 10)
            LocalDate dataAdmissao = null;
            String dataAdmissaoStr = campos[10].trim();
            if (!dataAdmissaoStr.isEmpty() && !dataAdmissaoStr.equalsIgnoreCase("null")) {
                dataAdmissao = LocalDate.parse(dataAdmissaoStr); // Formato YYYY-MM-DD
            }

            int periodo = Integer.parseInt(campos[11].trim().isEmpty() ? "0" : campos[11].trim());
            double receita = Double.parseDouble(campos[12].trim().isEmpty() ? "0.0" : campos[12].trim());
            double despesas = Double.parseDouble(campos[13].trim().isEmpty() ? "0.0" : campos[13].trim());
            double salario = Double.parseDouble(campos[14].trim().isEmpty() ? "0.0" : campos[14].trim());
            String cargo = campos[15].trim();

            Regime regime = null;
            String regimeStr = campos[16].trim();
            if (!regimeStr.isEmpty() && !regimeStr.equalsIgnoreCase("null")) {
                regime = Regime.valueOf(regimeStr.toUpperCase());
            }

            String departamento = campos[17].trim();
            String caminhoFoto = campos[18].trim(); // NOVO ÍNDICE 18

            Usuario usuario = switch (perfil) {
                case ADMINISTRADOR -> new Administrador.AdministradorBuilder()
                        .nome(nome).cpf(cpf).email(email).endereco(endereco)
                        .login(login).senha(senha).perfil(perfil)
                        .idiomaPreferencial(idiomaPreferencial)
                        .matricula(matricula).dataAdmissao(dataAdmissao).periodo(periodo).departamento(departamento)
                        .receita(receita).despesas(despesas).salario(salario)
                        .cargo(cargo).regime(regime).caminhoFoto(caminhoFoto)
                        .build();
                case GESTOR -> new Gestor.GestorBuilder()
                        .nome(nome).cpf(cpf).email(email).endereco(endereco)
                        .login(login).senha(senha).perfil(perfil)
                        .idiomaPreferencial(idiomaPreferencial)
                        .matricula(matricula).dataAdmissao(dataAdmissao).periodo(periodo).departamento(departamento)
                        .receita(receita).despesas(despesas).salario(salario)
                        .cargo(cargo).regime(regime).caminhoFoto(caminhoFoto)
                        .build();
                case RECRUTADOR -> new Recrutador.RecrutadorBuilder()
                        .nome(nome).cpf(cpf).email(email).endereco(endereco)
                        .login(login).senha(senha).perfil(perfil)
                        .idiomaPreferencial(idiomaPreferencial)
                        .matricula(matricula).dataAdmissao(dataAdmissao).periodo(periodo).departamento(departamento)
                        .receita(receita).despesas(despesas).salario(salario)
                        .cargo(cargo).regime(regime).caminhoFoto(caminhoFoto)
                        .build();
                default ->
                        new Funcionario.FuncionarioBuilder()
                                .nome(nome).cpf(cpf).email(email).endereco(endereco)
                                .login(login).senha(senha).perfil(perfil)
                                .idiomaPreferencial(idiomaPreferencial)
                                .matricula(matricula).dataAdmissao(dataAdmissao).periodo(periodo).departamento(departamento)
                                .receita(receita).despesas(despesas).salario(salario)
                                .cargo(cargo).regime(regime).caminhoFoto(caminhoFoto)
                                .build();
            };

            usuario.setId(id);
            return usuario;

        } catch (Exception e) {
            System.err.println("Falha ao parsear linha do CSV: '" + linha + "'. Erro: " + e.getMessage());
            e.printStackTrace();
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
            sb.append(String.join( ",",
                    endereco.getLogradouro(),
                    String.valueOf(endereco.getNumero()),
                    endereco.getBairro(),
                    endereco.getCidade(),
                    String.valueOf(endereco.getEstado()) ,
                    endereco.getCep()
            ));
        }
        sb.append(";");

        sb.append(usuario.getLogin()).append(";");
        sb.append(usuario.getSenha()).append(";");
        sb.append(usuario.getPerfil()).append(";");
        sb.append(usuario.getIdiomaPreferencial() == null ? "pt_BR" : usuario.getIdiomaPreferencial()).append(";");

        if (usuario instanceof Funcionario f) {
            sb.append(f.getMatricula()).append(";");
            sb.append(f.getDataAdmissao() == null ? "" : f.getDataAdmissao().toString()).append(";"); // NOVO CAMPO
            sb.append(f.getPeriodo()).append(";");
            sb.append(f.getReceita()).append(";");
            sb.append(f.getDespesas()).append(";");
            sb.append(f.getSalario()).append(";");
            sb.append(f.getCargo() == null ? "" : f.getCargo()).append(";");
            sb.append(f.getRegime() == null ? "" : f.getRegime().name()).append(";");
            sb.append(f.getDepartamento() == null ? "" : f.getDepartamento()).append(";");
            sb.append(f.getCaminhoFoto() == null ? "" : f.getCaminhoFoto()).append(";");
        } else {
            sb.append(";;;;;;;;;;"); // ATUALIZADO: 10 colunas vazias
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

    public void atualizarUsuario(Usuario usuarioAtualizado) throws IOException {
        Optional<Usuario> usuarioAntigoOpt = buscaUsuarioPorId(usuarioAtualizado.getId());

        if (usuarioAntigoOpt.isPresent()) {
            int index = this.usuariosEmMemoria.indexOf(usuarioAntigoOpt.get());
            if (index != -1) {
                this.usuariosEmMemoria.set(index, usuarioAtualizado);
                persistirAlteracoesNoCSV();
            } else {
                throw new IOException("Erro interno: Usuário encontrado mas índice não localizado.");
            }
        } else {
            throw new IOException("Usuário com ID " + usuarioAtualizado.getId() + " não encontrado para atualizar.");
        }
    }
}