package app.humanize.util;

import app.humanize.model.Usuario;
import app.humanize.repository.UsuarioRepository;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Classe Singleton para gerenciar a sessão do usuário logado.
 * Ela armazena o objeto Usuario e o disponibiliza para toda a aplicação.
 */
public final class UserSession {

    private static final String BUNDLE = "bundles.messages";
    private Locale currentLocale = new Locale("pt", "BR");

    private static UserSession instance;
    private Usuario usuarioLogado;

    private UserSession() {}

    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        Locale.setDefault(currentLocale);

        if (usuarioLogado != null) {
            String langTag = locale.toLanguageTag().replace("-", "_");
            usuarioLogado.setIdiomaPreferencial(langTag);

            try {
                UsuarioRepository.getInstance().atualizarUsuario(usuarioLogado);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Falha ao salvar a preferência de idioma no CSV: " + e.getMessage());
            }
        }
    }

    public Locale getLocale() {
        return currentLocale;
    }

    public ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE, getLocale());
    }

    public void setLocaleFromString(String langKey) {
        Locale newLocale;
        String langTag;

        switch (langKey) {
            case "profile.language.english":
                newLocale = new Locale("en", "US");
                langTag = "en_US";
                break;
            case "profile.language.spanish":
                newLocale = new Locale("es", "ES");
                langTag = "es_ES";
                break;
            default: // Português
                newLocale = new Locale("pt", "BR");
                langTag = "pt_BR";
                break;
        }

        if (usuarioLogado != null) {
            usuarioLogado.setIdiomaPreferencial(langTag);
            try {
                UsuarioRepository.getInstance().atualizarUsuario(usuarioLogado);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Falha ao salvar a preferência de idioma no CSV: " + e.getMessage());
            }
        }
        this.currentLocale = newLocale;
        Locale.setDefault(currentLocale);
    }

    public String getStringFromLocale() {
        String lang = getLocale().getLanguage();
        if (lang.equals("en")) return "profile.language.english";
        if (lang.equals("es")) return "profile.language.spanish";
        return "profile.language.portuguese";
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(Usuario usuario) {
        this.usuarioLogado = usuario;

        String idioma = usuario.getIdiomaPreferencial();
        if (idioma != null && !idioma.isEmpty()) {
            String[] partes = idioma.split("_");
            if (partes.length == 2) {
                this.currentLocale = new Locale(partes[0], partes[1]);
            } else {
                this.currentLocale = new Locale(partes[0]);
            }
        } else {
            this.currentLocale = new Locale("pt", "BR");
        }
        Locale.setDefault(this.currentLocale);
    }

    public void logout() {
        this.usuarioLogado = null;
        this.currentLocale = new Locale("pt", "BR");
        Locale.setDefault(this.currentLocale);
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
}