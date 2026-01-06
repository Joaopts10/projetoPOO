package visao;

import sistema.SistemaLoja;

public class Principal {
    public static void main(String[] args) {
        SistemaLoja sistema = new SistemaLoja();

        System.out.println("Sistema iniciado. Abrindo janela...");

        java.awt.EventQueue.invokeLater(() -> {
            new TelaLogin(sistema).setVisible(true);
        });
    }
}