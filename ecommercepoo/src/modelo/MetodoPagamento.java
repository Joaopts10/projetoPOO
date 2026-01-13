package modelo;

public enum MetodoPagamento {
    PIX("Pix"),
    CARTAO("Cartão"),
    BOLETO("Boleto");

    private final String label;

    MetodoPagamento(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}


