package modelo;

import java.io.Serializable;

public class Endereco implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cep;
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

    public Endereco(String cep, String rua, String numero, String complemento, String bairro, String cidade, String estado) {
        this.cep = cep;
        this.rua = rua;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rua).append(", ").append(numero);
        if (complemento != null && !complemento.trim().isEmpty()) {
            sb.append(" - ").append(complemento.trim());
        }
        sb.append("\n").append(bairro).append(" - ").append(cidade).append("/").append(estado);
        sb.append("\nCEP: ").append(cep);
        return sb.toString();
    }
}




