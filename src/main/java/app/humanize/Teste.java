package app.humanize;

import app.humanize.model.Endereco;
import app.humanize.model.Funcionario;
import app.humanize.model.Perfil;
import app.humanize.repository.UsuarioRepository;

import java.io.IOException;
import java.time.LocalDate;

public class Teste {
    public static void main(String[] args) throws IOException {
        Funcionario.FuncionarioBuilder funcionarioBuilder = new Funcionario.FuncionarioBuilder();
        funcionarioBuilder.nome("Davi");
        funcionarioBuilder.cpf("04935825170");
        funcionarioBuilder.email("davilopesbrito64@gmail.com");
        Endereco.EnderecoBuilder enderecoBuilder = new Endereco.EnderecoBuilder();
        enderecoBuilder.logradouro("Capão");
        enderecoBuilder.numero(12);
        enderecoBuilder.bairro("Nilópolis");
        enderecoBuilder.cidade("Rio de Janeiro");
        enderecoBuilder.estado("RJ");
        enderecoBuilder.cep("12345");
        Endereco endereco = enderecoBuilder.build();
        funcionarioBuilder.endereco(endereco);
        funcionarioBuilder.login("davi");
        funcionarioBuilder.senha("davi123");
        funcionarioBuilder.perfil(Perfil.FUNCIONARIO);
        funcionarioBuilder.matricula(242023425);
        funcionarioBuilder.periodo(10);
        funcionarioBuilder.dataEmissao(LocalDate.ofEpochDay(2025-11-10));
        funcionarioBuilder.receita(1000);
        funcionarioBuilder.despesas(100000);
        funcionarioBuilder.salario(1950);
        Funcionario funcionario = funcionarioBuilder.build();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        usuarioRepository.escreveUsuarioNovo(funcionario);
    }
}
