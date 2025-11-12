package app.humanize.model;

import java.time.LocalDate;

public class Administrador extends Funcionario {

    private Administrador(AdministradorBuilder builder) {
        super(
                builder.idiomaPreferencial, builder.nome, builder.cpf, builder.endereco, builder.email,
                builder.login, builder.senha, builder.perfil,
                builder.matricula, builder.dataAdmissao, builder.periodo,
                builder.receita, builder.despesas, builder.salario,
                builder.cargo, builder.regime, builder.departamento,
                builder.caminhoFoto
        );
    }

    public Administrador() {}

    public static class AdministradorBuilder {
        private int matricula;
        private LocalDate dataAdmissao;
        private int periodo;
        private double receita;
        private double despesas;
        private double salario;
        private String cargo;
        private Regime regime;
        private String login;
        private String senha;
        private Perfil perfil;
        private String nome;
        private String cpf;
        private String email;
        private Endereco endereco;
        private String departamento;
        private String caminhoFoto;
        private String idiomaPreferencial;

        public AdministradorBuilder caminhoFoto(String caminhoFoto) { this.caminhoFoto = caminhoFoto; return this; }
        public AdministradorBuilder regime(Regime regime) { this.regime = regime; return this; }
        public AdministradorBuilder cargo(String cargo) { this.cargo = cargo; return this; }
        public AdministradorBuilder matricula(int matricula) { this.matricula = matricula; return this; }
        public AdministradorBuilder dataAdmissao(LocalDate data) { this.dataAdmissao = data; return this; }
        public AdministradorBuilder periodo(int periodo) { this.periodo = periodo; return this; }
        public AdministradorBuilder receita(double receita) { this.receita = receita; return this; }
        public AdministradorBuilder despesas(double despesas) { this.despesas = despesas; return this; }
        public AdministradorBuilder salario(double salario) { this.salario = salario; return this; }
        public AdministradorBuilder nome(String nome) { this.nome = nome; return this; }
        public AdministradorBuilder cpf(String cpf) { this.cpf = cpf; return this; }
        public AdministradorBuilder email(String email) { this.email = email; return this; }
        public AdministradorBuilder endereco(Endereco endereco) { this.endereco = endereco; return this; }
        public AdministradorBuilder login(String login) { this.login = login; return this; }
        public AdministradorBuilder senha(String senha) { this.senha = senha; return this; }
        public AdministradorBuilder perfil(Perfil perfil) { this.perfil = perfil; return this; }
        public AdministradorBuilder departamento(String departamento){this.departamento = departamento; return this;}
        public AdministradorBuilder idiomaPreferencial(String idioma) { this.idiomaPreferencial = idioma; return this; }

        public Administrador build() {
            return new Administrador(this);
        }
    }
}