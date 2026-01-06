package visao;

import modelo.Admin;
import modelo.Cliente;
import modelo.Usuario;
import sistema.SistemaLoja;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaLogin extends JFrame {
    private SistemaLoja sistema;
    private JTextField txtLogin;
    private JPasswordField txtSenha;

    public TelaLogin(SistemaLoja sistema) {
        this.sistema = sistema;
        setTitle("Login E-Commerce");
        setSize(300, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Login:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtLogin = new JTextField();
        add(txtLogin, gbc);

        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        add(new JLabel("Senha:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtSenha = new JPasswordField();
        add(txtSenha, gbc);

        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel painelBotoes = new JPanel(new FlowLayout());
        JButton btnEntrar = new JButton("Entrar");
        JButton btnCadastrar = new JButton("Cadastrar-se");
        painelBotoes.add(btnEntrar);
        painelBotoes.add(btnCadastrar);
        add(painelBotoes, gbc);

        
        btnEntrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autenticar();
            }
        });
        
        btnCadastrar.addActionListener(e -> {
            new TelaCadastro(sistema).setVisible(true);
        });
    }

    private void autenticar() {
        String login = txtLogin.getText();
        String senha = new String(txtSenha.getPassword());

        Usuario usuario = sistema.fazerLogin(login, senha);

        if (usuario != null) {
            JOptionPane.showMessageDialog(this, "Bem-vindo, " + usuario.getNome() + "!");
            this.dispose(); 

            
            if (usuario instanceof Admin) {
                new TelaAdmin(sistema).setVisible(true);
            } else if (usuario instanceof Cliente) {
                new TelaCliente(sistema, (Cliente) usuario).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Login ou senha inválidos!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}