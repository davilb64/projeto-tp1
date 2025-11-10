package app.humanize.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RelatorioFinanceiro {
    // serve pra atualizar a tabela diretamente
    private final StringProperty data;
    private final StringProperty descricao;
    private final StringProperty receita;
    private final StringProperty despesas;
    private final StringProperty saldo;
    private final StringProperty categoria;

    public RelatorioFinanceiro() {
        this.data = new SimpleStringProperty();
        this.descricao = new SimpleStringProperty();
        this.receita = new SimpleStringProperty();
        this.despesas = new SimpleStringProperty();
        this.saldo = new SimpleStringProperty();
        this.categoria = new SimpleStringProperty();
    }

    public RelatorioFinanceiro(String data, String descricao, String receita, String despesas, String saldo, String categoria) {
        this.data = new SimpleStringProperty(data);
        this.descricao = new SimpleStringProperty(descricao);
        this.receita = new SimpleStringProperty(receita);
        this.despesas = new SimpleStringProperty(despesas);
        this.saldo = new SimpleStringProperty(saldo);
        this.categoria = new SimpleStringProperty(categoria);
    }

    public String getData() { return data.get(); }
    public void setData(String data) { this.data.set(data); }
    public StringProperty dataProperty() { return data; }

    public String getDescricao() { return descricao.get(); }
    public void setDescricao(String descricao) { this.descricao.set(descricao); }
    public StringProperty descricaoProperty() { return descricao; }

    public String getReceita() { return receita.get(); }
    public void setReceita(String receita) { this.receita.set(receita); }
    public StringProperty receitaProperty() { return receita; }

    public String getDespesas() { return despesas.get(); }
    public void setDespesas(String despesas) { this.despesas.set(despesas); }
    public StringProperty despesasProperty() { return despesas; }

    public String getSaldo() { return saldo.get(); }
    public void setSaldo(String saldo) { this.saldo.set(saldo); }
    public StringProperty saldoProperty() { return saldo; }

    public String getCategoria() { return categoria.get(); }
    public void setCategoria(String categoria) { this.categoria.set(categoria); }
    public StringProperty categoriaProperty() { return categoria; }
}