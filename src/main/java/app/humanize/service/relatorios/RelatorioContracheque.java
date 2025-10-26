package app.humanize.service.relatorios;

import app.humanize.model.Perfil;
import app.humanize.model.Usuario;

public class RelatorioContracheque implements IGeradorRelatorio{

    @Override
    public String getNome() {
        return "ContraCheque";
    }

    @Override
    public ReportData coletarDados() {
        return null;
    }

    @Override
    public boolean podeGerar(Usuario usuarioLogado) {
        return usuarioLogado.getPerfil() == Perfil.FUNCIONARIO;
    }
}
