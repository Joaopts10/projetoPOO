package modelo;

public class Admin extends Usuario {
    private static final long serialVersionUID = 1L;

    public Admin(String login, String senha, String nome) {
        super(login, senha, nome);
    }

    @Override
    public boolean isAdmin() {
        return true;
    }
}