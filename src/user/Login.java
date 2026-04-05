package user;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Login extends JFrame {

    JTextField txtUser;
    JPasswordField txtPass;
    JToggleButton eyeBtn;

    Color BG = new Color(236, 232, 225);
    Color CARD_GLASS = new Color(255, 255, 255, 210);
    Color GOLD = new Color(198, 160, 74);
    Color TEXT = new Color(60, 60, 60);
    Color BORDER = new Color(200, 200, 200);

    ImageIcon eyeOpen;
    ImageIcon eyeClosed;

    public Login() {
        setTitle("AVVJ cart - Login");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        eyeOpen = scaleIcon("/icons/visible.png", 20, 20);
        eyeClosed = scaleIcon("/icons/hide.png", 20, 20);

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Theme.BG);

        JPanel card = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            }
        };

        card.setPreferredSize(new Dimension(440, 480));
        card.setOpaque(false);

        JLabel title = new JLabel("Login", JLabel.CENTER);
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.BLUE);
        title.setBounds(40, 40, 360, 35);
        card.add(title);

        JLabel sub = new JLabel("Get access to your Orders, Wishlist and Recommendations", JLabel.CENTER);
        sub.setFont(Theme.SMALL);
        sub.setForeground(Theme.GRAY);
        sub.setBounds(40, 75, 360, 20);
        card.add(sub);

        JLabel userLbl = new JLabel("Enter Username");
        userLbl.setFont(Theme.SMALL);
        userLbl.setForeground(Theme.GRAY);
        userLbl.setBounds(60, 130, 120, 20);
        card.add(userLbl);

        txtUser = new JTextField();
        txtUser.setBounds(60, 155, 320, 40);
        txtUser.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.GRAY));
        txtUser.setFont(Theme.NORMAL);
        card.add(txtUser);

        JLabel passLbl = new JLabel("Enter Password");
        passLbl.setFont(Theme.SMALL);
        passLbl.setForeground(Theme.GRAY);
        passLbl.setBounds(60, 210, 120, 20);
        card.add(passLbl);

        txtPass = new JPasswordField();
        txtPass.setBounds(60, 235, 280, 40);
        txtPass.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.GRAY));
        txtPass.setEchoChar('•');
        txtPass.setFont(Theme.NORMAL);
        card.add(txtPass);

        eyeBtn = new JToggleButton(eyeClosed);
        eyeBtn.setBounds(340, 235, 40, 40);
        eyeBtn.setBorder(null);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setFocusPainted(false);
        eyeBtn.addActionListener(e -> togglePassword());
        card.add(eyeBtn);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBounds(60, 320, 320, 45);
        btnLogin.setBackground(Theme.ORANGE);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(Theme.HEADER);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> login());
        card.add(btnLogin);

        JLabel signup = new JLabel("New to AVVJ cart? Create an account", JLabel.CENTER);
        signup.setBounds(60, 380, 320, 25);
        signup.setFont(Theme.NORMAL);
        signup.setForeground(Theme.BLUE);
        signup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signup.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new SignUp().setVisible(true);
                dispose();
            }
        });
        card.add(signup);

        JLabel forgot = new JLabel("Forgot Password?", JLabel.CENTER);
        forgot.setBounds(60, 410, 320, 25);
        forgot.setFont(Theme.SMALL);
        forgot.setForeground(Theme.GRAY);
        forgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new ForgotPassword().setVisible(true);
            }
        });
        card.add(forgot);

        container.add(card);
        add(container);
    }

    void togglePassword() {
        if (eyeBtn.isSelected()) {
            txtPass.setEchoChar((char) 0);
            eyeBtn.setIcon(eyeOpen);
        } else {
            txtPass.setEchoChar('•');
            eyeBtn.setIcon(eyeClosed);
        }
    }

    void login() {
        String username = txtUser.getText().trim();
        String password = String.valueOf(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all fields");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database Connection Failed!");
                return;
            }

            PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                int approved = rs.getInt("approved");

                if (approved == 0) {
                    JOptionPane.showMessageDialog(this, "Your account is pending admin approval.");
                    return;
                }

                if (role.equalsIgnoreCase("admin")) {
                    new AdminDashboard().setVisible(true);
                } else if (role.equalsIgnoreCase("supplier")) {
                    new SupplierDashboard(id).setVisible(true);
                } else {
                    new UserDashboard(id).setVisible(true);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error!");
        }
    }

    ImageIcon scaleIcon(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null)
            return new ImageIcon();
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public static void main(String[] args) {
        new Login().setVisible(true);
    }
}
