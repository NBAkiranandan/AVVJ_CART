package user;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ForgotPassword extends JFrame {

    JTextField txtUser, txtOTP;
    JPasswordField txtPass, txtCPass;
    JLabel lblEmailStatus;
    JButton btnCheck, btnSendOTP, btnReset;
    JPanel root, card;
    String userEmail = "";
    String generatedOTP = "";

    public ForgotPassword() {
        setTitle("AVVJ cart - Reset Password");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        root = new JPanel(new GridBagLayout());
        root.setBackground(Theme.BG);

        card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        card.setPreferredSize(new Dimension(420, 500));
        card.setOpaque(false);

        JLabel title = new JLabel("Identity Verification", JLabel.CENTER);
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.BLUE);
        title.setBounds(30, 40, 360, 35);
        card.add(title);

        int x = 50, w = 320, h = 40;

        addLabel(card, "Enter Username", x, 100);
        txtUser = field(card, x, 125, w, h);

        btnCheck = new JButton("Verify User");
        btnCheck.setBounds(x, 175, w, 40);
        btnCheck.setBackground(Theme.BLUE);
        btnCheck.setForeground(Color.WHITE);
        btnCheck.addActionListener(e -> verifyUser());
        card.add(btnCheck);

        lblEmailStatus = new JLabel("Email will appear here");
        lblEmailStatus.setBounds(x, 230, w, 20);
        lblEmailStatus.setFont(Theme.SMALL);
        lblEmailStatus.setForeground(Theme.GRAY);
        card.add(lblEmailStatus);

        btnSendOTP = new JButton("Send OTP");
        btnSendOTP.setBounds(x, 255, 100, h);
        btnSendOTP.setBackground(Theme.BLUE);
        btnSendOTP.setForeground(Color.WHITE);
        btnSendOTP.setEnabled(false);
        btnSendOTP.addActionListener(e -> sendOTP());
        card.add(btnSendOTP);

        txtOTP = field(card, x + 110, 255, w - 110, h);
        txtOTP.setEnabled(false);

        addLabel(card, "New Password", x, 310);
        txtPass = password(card, x, 335, w, h);
        txtPass.setEnabled(false);

        addLabel(card, "Confirm Password", x, 385);
        txtCPass = password(card, x, 410, w, h);
        txtCPass.setEnabled(false);

        btnReset = new JButton("Reset Password");
        btnReset.setBounds(x, 470, w, 45);
        btnReset.setBackground(Theme.ORANGE);
        btnReset.setForeground(Color.WHITE);
        btnReset.setEnabled(false);
        btnReset.addActionListener(e -> resetPassword());
        card.add(btnReset);

        root.add(card);
        add(root);
    }

    void verifyUser() {
        String user = txtUser.getText().trim();
        if (user.isEmpty())
            return;

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT email FROM users WHERE username=?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                userEmail = rs.getString("email");
                String maskedEmail = userEmail.replaceAll("(^[^@]{3}|(?!^)\\G)[^@]", "$1*");
                lblEmailStatus.setText("Registered Email: " + maskedEmail);
                
                btnSendOTP.setEnabled(true);
                btnCheck.setEnabled(false);
                txtUser.setEditable(false);
            } else {
                JOptionPane.showMessageDialog(this, "Username not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendOTP() {
        OTPManager.generateOTP();
        JOptionPane.showMessageDialog(this, "Sending OTP to your registered email... Please wait.");
        boolean sent = EmailUtility.sendOTP(userEmail, OTPManager.getOTP());
        
        if (!sent) {
            JOptionPane.showMessageDialog(this, "Failed to send OTP!");
            return;
        }
        
        JOptionPane.showMessageDialog(this, "OTP sent!");
        
        txtOTP.setEnabled(true);
        txtPass.setEnabled(true);
        txtCPass.setEnabled(true);
        btnReset.setEnabled(true);
    }

    void resetPassword() {
        String user = txtUser.getText().trim();
        String enteredOTP = txtOTP.getText().trim();
        String pass = String.valueOf(txtPass.getPassword());
        String cpass = String.valueOf(txtCPass.getPassword());

        if (enteredOTP.isEmpty() || pass.isEmpty() || !pass.equals(cpass)) {
            JOptionPane.showMessageDialog(this, "Please check all fields and ensure passwords match.");
            return;
        }
        
        if (!OTPManager.verifyOTP(enteredOTP)) {
            JOptionPane.showMessageDialog(this, "Invalid or Expired OTP!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE users SET password=? WHERE username=?");
            ps.setString(1, pass);
            ps.setString(2, user);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Password Reset Successful!");
                new Login().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Reset Failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addLabel(JPanel p, String t, int x, int y) {
        JLabel l = new JLabel(t);
        l.setBounds(x, y, 200, 20);
        l.setFont(Theme.SMALL);
        l.setForeground(Theme.GRAY);
        p.add(l);
    }

    JTextField field(JPanel p, int x, int y, int w, int h) {
        JTextField f = new JTextField();
        f.setBounds(x, y, w, h);
        f.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.GRAY));
        f.setFont(Theme.NORMAL);
        p.add(f);
        return f;
    }

    JPasswordField password(JPanel p, int x, int y, int w, int h) {
        JPasswordField f = new JPasswordField();
        f.setBounds(x, y, w, h);
        f.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.GRAY));
        f.setFont(Theme.NORMAL);
        p.add(f);
        return f;
    }

    public static void main(String[] args) {
        new ForgotPassword().setVisible(true);
    }
}
