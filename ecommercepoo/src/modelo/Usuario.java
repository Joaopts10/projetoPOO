package modelo;

import java.io.Serializable;

public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String login;
    protected String senha;
    protected String nome;

    public Usuario(String login, String senha, String nome) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
    }

    public boolean autenticar(String senha) {
        return this.senha.equals(senha);
    }

    public String getLogin() { 
    	return login; 
    }
    
    public String getNome() { 
    	return nome; 
    }

    public abstract boolean isAdmin();
}