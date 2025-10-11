package app.humanize.model;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Administrador extends Usuario {
    //Construtores
    public Administrador(String nome, String cpf, Endereco endereco, String email, String login, String senha) {
        super(nome, cpf, endereco, email, login, senha);
    }
    public Administrador() {}



}
