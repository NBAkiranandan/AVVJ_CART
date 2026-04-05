package user;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignUp extends JFrame {

    JTextField txtUser, txtEmail, txtMobile, txtAge, txtAddress, txtOTP;
    JPasswordField txtPass, txtCPass;
    JComboBox<String> roleBox;

    JToggleButton eyeBtn, ceyeBtn;
    ImageIcon eyeOpen, eyeClosed;
    JButton btnSendOTP;
    String generatedOTP = "";

    public SignUp() {

        setTitle("AVVJ Cart - Create Account");
        setSize(550, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        eyeOpen = scaleIcon("/icons/visible.png", 20, 20);
        eyeClosed = scaleIcon("/icons/hide.png", 20, 20);

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Theme.BG);

        JPanel card = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            }
        };
        card.setPreferredSize(new Dimension(460, 750));
        card.setOpaque(false);

        JLabel title = new JLabel("Create Account", JLabel.CENTER);
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.BLUE);
        title.setBounds(40, 30, 380, 35);
        card.add(title);

        int x = 60;
        int y = 90;
        int w = 340;
        int h = 40;
        int gap = 65;

        // Username
        addLabel(card, "Username", x, y);
        txtUser = field(card, x, y + 20, w, h);

        // Email
        y += gap;
        addLabel(card, "Email Address", x, y);
        txtEmail = field(card, x, y + 20, w, h);

        // Mobile
        y += gap;
        addLabel(card, "Mobile Number", x, y);
        txtMobile = field(card, x, y + 20, w, h);

        // Age & Role (Split)
        y += gap;
        addLabel(card, "Age", x, y);
        txtAge = field(card, x, y + 20, 100, h);

        addLabel(card, "Select Role", x + 120, y);
        roleBox = new JComboBox<>(new String[] { "User", "Supplier" });
        roleBox.setBounds(x + 120, y + 20, 220, h);
        roleBox.setFont(Theme.NORMAL);
        card.add(roleBox);

        // OTP Verification
        y += gap;
        btnSendOTP = new JButton("Send OTP");
        btnSendOTP.setBounds(x, y + 20, 100, h);
        btnSendOTP.setBackground(Theme.BLUE);
        btnSendOTP.setForeground(Color.WHITE);
        btnSendOTP.setFocusPainted(false);
        btnSendOTP.addActionListener(e -> sendOTP());
        card.add(btnSendOTP);

        txtOTP = field(card, x + 110, y + 20, w - 110, h);
        addLabel(card, "Enter Email OTP", x + 110, y);
        
        y += gap; // Maintain the same visual gap logic for the dropped field

        // Password
        y += gap;
        addLabel(card, "Password", x, y);
        txtPass = password(card, x, y + 20, w - 45, h);
        eyeBtn = new JToggleButton(eyeClosed);
        eyeBtn.setBounds(x + w - 40, y + 20, 40, 40);
        eyeBtn.setBorder(null);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.addActionListener(e -> toggle(txtPass, eyeBtn));
        card.add(eyeBtn);

        // Confirm
        y += gap;
        addLabel(card, "Confirm Password", x, y);
        txtCPass = password(card, x, y + 20, w - 45, h);
        ceyeBtn = new JToggleButton(eyeClosed);
        ceyeBtn.setBounds(x + w - 40, y + 20, 40, 40);
        ceyeBtn.setBorder(null);
        ceyeBtn.setContentAreaFilled(false);
        ceyeBtn.addActionListener(e -> toggle(txtCPass, ceyeBtn));
        card.add(ceyeBtn);

        // Button
        JButton btnSignUp = new JButton("Join AVVJ Cart");
        btnSignUp.setBounds(x, y + 80, w, 50);
        btnSignUp.setBackground(Theme.ORANGE);
        btnSignUp.setForeground(Color.WHITE);
        btnSignUp.setFont(Theme.HEADER);
        btnSignUp.setFocusPainted(false);
        btnSignUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSignUp.addActionListener(e -> submit());
        card.add(btnSignUp);

        JLabel login = new JLabel("Already have an account? Login", JLabel.CENTER);
        login.setBounds(x, y + 140, w, 25);
        login.setFont(Theme.NORMAL);
        login.setForeground(Theme.BLUE);
        login.setCursor(new Cursor(Cursor.HAND_CURSOR));
        login.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new Login().setVisible(true);
                dispose();
            }
        });
        card.add(login);

        container.add(card);
        add(container);
    }

    void toggle(JPasswordField p, JToggleButton b) {
        if (b.isSelected()) {
            p.setEchoChar((char) 0);
            b.setIcon(eyeOpen);
        } else {
            p.setEchoChar('•');
            b.setIcon(eyeClosed);
        }
    }

    void sendOTP() {
        String email = txtEmail.getText().trim();
    
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter email first");
            return;
        }
    
        OTPManager.generateOTP();
    
        boolean sent = EmailUtility.sendOTP(email, OTPManager.getOTP());
    
        if (sent) {
            JOptionPane.showMessageDialog(this, "OTP sent!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to send OTP");
        }
    }

    void submit() {
        String username = txtUser.getText().trim();
        String email = txtEmail.getText().trim();
        String mobile = txtMobile.getText().trim();
        String ageText = txtAge.getText().trim();
        String enteredOTP = txtOTP.getText().trim();
        String role = roleBox.getSelectedItem().toString().toLowerCase();
        String pass = new String(txtPass.getPassword());
        String cpass = new String(txtCPass.getPassword());

        if (username.isEmpty() || email.isEmpty() || mobile.isEmpty() || ageText.isEmpty() || enteredOTP.isEmpty()
                || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        if (!pass.equals(cpass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }
        
        if (!OTPManager.verifyOTP(enteredOTP)) {
            JOptionPane.showMessageDialog(this, "Invalid or Expired OTP!");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            try (Connection con = DBConnection.getConnection()) {
                String sql = "INSERT INTO users(username,email,password,mobile,age,address,security_question,security_answer,role,approved) VALUES(?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, pass);
                ps.setString(4, mobile);
                ps.setInt(5, age);
                ps.setString(6, ""); // Address empty by default
                ps.setString(7, "Email OTP Verification"); // Security Box Removed
                ps.setString(8, "Verified"); // Security Answer Removed
                ps.setString(9, role);
                ps.setInt(10, role.equals("supplier") ? 0 : 1);

                ps.executeUpdate();
                if (role.equals("supplier")) {
                    JOptionPane.showMessageDialog(this, "Registration Successful! Please wait for admin approval.");
                } else {
                    JOptionPane.showMessageDialog(this, "Registration Successful!");
                }
                new Login().setVisible(true);
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Registration Failed: " + e.getMessage());
        }
    }

    void addLabel(JPanel panel, String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.SMALL);
        label.setForeground(Theme.GRAY);
        label.setBounds(x, y, 200, 20);
        panel.add(label);
    }

    JTextField field(JPanel panel, int x, int y, int w, int h) {
        JTextField f = new JTextField();
        f.setBounds(x, y, w, h);
        f.setFont(Theme.NORMAL);
        f.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.GRAY));
        panel.add(f);
        return f;
    }

    JPasswordField password(JPanel panel, int x, int y, int w, int h) {
        JPasswordField f = new JPasswordField();
        f.setBounds(x, y, w, h);
        f.setFont(Theme.NORMAL);
        f.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.GRAY));
        panel.add(f);
        return f;
    }

    ImageIcon scaleIcon(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null)
            return new ImageIcon();
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public static void main(String[] args) {
        new SignUp().setVisible(true);
    }
}