package visao;

import sistema.SistemaLoja;

import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                SistemaLoja sistema = new SistemaLoja();
                new TelaLogin(sistema).setVisible(true);
            } catch (IllegalStateException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
