import visao.TelaLogin;
import sistema.SistemaLoja;

public class Main {
    public static void main(String[] args) {
        SistemaLoja sistema = new SistemaLoja();
        
        java.awt.EventQueue.invokeLater(() -> {
            new TelaLogin(sistema).setVisible(true);
        });
    }
}
