package app.humanize.model;

import java.time.LocalDate;

public class Relatorio {

    private int id;
    private TipoRelatorio tipoRelatorio;
    private LocalDate dataGeracao;
    private Usuario responsavel;

    public Relatorio() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public TipoRelatorio getTipoRelatorio() { return tipoRelatorio; }
    public void setTipoRelatorio(TipoRelatorio tipoRelatorio) { this.tipoRelatorio = tipoRelatorio; }
    public LocalDate getDataGeracao() { return dataGeracao; }
    public void setDataGeracao(LocalDate dataGeracao) { this.dataGeracao = dataGeracao; }
    public Usuario getResponsavel() { return responsavel; }
    public void setResponsavel(Usuario responsavel) { this.responsavel = responsavel; }

    @Override
    public String toString() {
        return "Relatorio{" +
                "id=" + id +
                ", tipo=" + tipoRelatorio +
                ", data=" + dataGeracao +
                ", responsavel=" + (responsavel != null ? responsavel.getNome() : "N/A") +
                '}';
    }
}