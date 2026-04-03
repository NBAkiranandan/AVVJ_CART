package user;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {

    JLabel lblUsers, lblProducts, lblOrders, lblRevenue, lblSuppliers;

    CardLayout contentLayout;
    JPanel dynamicContent;

    JTable table;
    DefaultTableModel model;

    JPanel btnPanel;
    JButton btnDelete, btnBack, btnAccept, btnEdit;

    JTextField searchField;

    String currentPage = "";

    public AdminDashboard() {

        setTitle("AVVJ cart - Admin Command Center");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        root.add(sidebar(), BorderLayout.WEST);
        root.add(header(), BorderLayout.NORTH);
        root.add(mainContent(), BorderLayout.CENTER);

        add(root);

        loadDashboardData();
    }

    /* DASHBOARD DATA */

    void loadDashboardData() {

        try (Connection con = DBConnection.getConnection()) {

            if (con == null)
                return;

            lblUsers.setText(getCount(con,
                    "SELECT COUNT(*) FROM users WHERE role='user'"));

            lblSuppliers.setText(getCount(con,
                    "SELECT COUNT(*) FROM users WHERE role='supplier'"));

            lblProducts.setText(getCount(con,
                    "SELECT COUNT(*) FROM products"));

            lblOrders.setText(getCount(con,
                    "SELECT COUNT(*) FROM orders"));

            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT IFNULL(SUM(total_amount),0) FROM orders");

            if (rs.next())
                lblRevenue.setText("₹ " + rs.getDouble(1));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String getCount(Connection con, String sql) throws Exception {

        ResultSet rs = con.createStatement().executeQuery(sql);
        rs.next();
        return rs.getString(1);
    }

    /* SIDEBAR */

    JPanel sidebar() {
        JPanel s = new JPanel();
        s.setPreferredSize(new Dimension(260, 0));
        s.setBackground(Color.WHITE);
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JLabel logo = new JLabel("  AVVJ cart");
        logo.setFont(Theme.TITLE);
        logo.setForeground(Theme.BLUE);
        logo.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        s.add(logo);

        s.add(menuItem("Dashboard", "/icons/dashboard.png"));
        s.add(menuItem("Manage Users", "/icons/users.png"));
        s.add(menuItem("Manage Products", "/icons/products.png"));
        s.add(menuItem("Manage Orders", "/icons/orders.png"));
        s.add(menuItem("Manage Suppliers", "/icons/suppliers.png"));

        s.add(Box.createVerticalGlue());
        s.add(menuItem("Logout", "/icons/logout.png"));
        s.add(Box.createVerticalStrut(20));

        return s;
    }

    JPanel menuItem(String text, String iconPath) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        item.setBackground(Color.WHITE);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon icon = scale(iconPath, 20, 20);
        JLabel ic = new JLabel(icon);
        JLabel txt = new JLabel(text);
        txt.setFont(Theme.NORMAL);
        txt.setForeground(Theme.TEXT);

        item.add(ic);
        item.add(txt);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                item.setBackground(new Color(240, 245, 255));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                item.setBackground(Color.WHITE);
            }

            public void mouseClicked(java.awt.event.MouseEvent e) {
                switch (text) {
                    case "Dashboard":
                        contentLayout.show(dynamicContent, "dashboard");
                        loadDashboardData();
                        break;
                    case "Manage Users":
                        loadUsers();
                        break;
                    case "Manage Products":
                        loadProducts();
                        break;
                    case "Manage Orders":
                        loadOrders();
                        break;
                    case "Manage Suppliers":
                        loadSuppliers();
                        break;
                    case "Logout":
                        new Login().setVisible(true);
                        dispose();
                        break;
                }
            }
        });

        return item;
    }

    /* HEADER */

    JPanel header() {

        JPanel h = new JPanel(new BorderLayout());
        h.setPreferredSize(new Dimension(0, 70));
        h.setBackground(Theme.BLUE);
        h.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel title = new JLabel("E-Commerce Administration");
        title.setFont(Theme.HEADER);
        title.setForeground(Color.WHITE);

        h.add(title, BorderLayout.WEST);

        return h;
    }

    /* MAIN CONTENT */

    JPanel mainContent() {

        contentLayout = new CardLayout();
        dynamicContent = new JPanel(contentLayout);
        dynamicContent.setBackground(Theme.BG);
        dynamicContent.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel dash = new JPanel(new GridLayout(2, 3, 30, 30));
        dash.setBackground(Theme.BG);

        lblUsers = new JLabel("0");
        lblProducts = new JLabel("0");
        lblOrders = new JLabel("0");
        lblRevenue = new JLabel("₹0");
        lblSuppliers = new JLabel("0");

        dash.add(statCard("Total Customers", lblUsers, Theme.BLUE));
        dash.add(statCard("Live Products", lblProducts, Theme.ORANGE));
        dash.add(statCard("All Orders", lblOrders, Theme.YELLOW));
        dash.add(statCard("Total Sales", lblRevenue, new Color(0, 150, 136)));
        dash.add(statCard("Registered Suppliers", lblSuppliers, new Color(100, 100, 100)));

        JPanel tablePanel = new JPanel(new BorderLayout(0, 20));
        tablePanel.setBackground(Theme.BG);

        model = new DefaultTableModel();
        table = new JTable(model);
        table.setRowHeight(50);
        table.setFont(Theme.NORMAL);

        JScrollPane scroll = new JScrollPane(table);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(20);
        JButton btnSearch = new JButton("Search");

        btnSearch.addActionListener(e -> search());

        topPanel.add(searchField);
        topPanel.add(btnSearch);

        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));

        btnDelete = createBtn("Delete", Color.RED);
        btnBack = createBtn("Back", Theme.BLUE);
        btnAccept = createBtn("Accept Supplier", new Color(0, 150, 0));
        btnEdit = createBtn("Edit", Theme.ORANGE);

        btnDelete.addActionListener(e -> deleteRow());
        btnBack.addActionListener(e -> contentLayout.show(dynamicContent, "dashboard"));
        btnAccept.addActionListener(e -> approveSupplier());
        btnEdit.addActionListener(e -> editUser());

        btnPanel.add(btnDelete);
        btnPanel.add(btnBack);

        tablePanel.add(topPanel, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);
        tablePanel.add(btnPanel, BorderLayout.SOUTH);

        dynamicContent.add(dash, "dashboard");
        dynamicContent.add(tablePanel, "table");

        JPanel productScrollPanel = new JPanel(new BorderLayout());
        productScrollPanel.setBackground(Theme.BG);
        JPanel grid = new JPanel(new GridLayout(0, 3, 20, 20));
        grid.setBackground(Theme.BG);
        JScrollPane pScroll = new JScrollPane(grid);
        pScroll.setBorder(null);
        productScrollPanel.add(pScroll, BorderLayout.CENTER);

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pBtn.add(btnBack);
        productScrollPanel.add(pBtn, BorderLayout.SOUTH);

        dynamicContent.add(productScrollPanel, "productGrid");

        return dynamicContent;
    }

    JButton createBtn(String t, Color bg) {

        JButton b = new JButton(t);
        b.setFont(Theme.NORMAL);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    JPanel statCard(String title, JLabel value, Color topColor) {
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(0, 4));
        strip.setBackground(topColor);
        c.add(strip, BorderLayout.NORTH);

        JLabel t = new JLabel(title);
        t.setFont(Theme.SMALL);
        t.setForeground(Theme.GRAY);
        c.add(t, BorderLayout.CENTER);

        value.setFont(new Font("Segoe UI", Font.BOLD, 28));
        value.setForeground(Theme.TEXT);
        c.add(value, BorderLayout.SOUTH);

        return c;
    }

    /* USERS */

    void loadUsers() {

        currentPage = "users";

        btnPanel.removeAll();
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnBack);

        loadTable("SELECT id,username,email,mobile,address FROM users WHERE role='user'");
    }

    /* PRODUCTS WITH ICONS */

    void loadProducts() {
        currentPage = "products";
        JPanel scrollPanel = (JPanel) dynamicContent.getComponent(2);
        JScrollPane scroll = (JScrollPane) scrollPanel.getComponent(0);
        JPanel grid = (JPanel) scroll.getViewport().getView();
        grid.removeAll();

        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM products");
            while (rs.next()) {
                grid.add(adminProductCard(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("image_path")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        contentLayout.show(dynamicContent, "productGrid");
        grid.revalidate();
        grid.repaint();
    }

    JPanel adminProductCard(int id, String name, double price, int stock, String img) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel image = new JLabel("", SwingConstants.CENTER);
        image.setPreferredSize(new Dimension(0, 120));
        try {
            java.net.URL imgUrl = getClass().getResource(img);
            if (imgUrl != null) {
                image.setIcon(new ImageIcon(new ImageIcon(imgUrl).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
            } else {
                java.io.File f = new java.io.File("src" + img);
                if(f.exists()){
                    image.setIcon(new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
                } else {
                    image.setText("No Image");
                }
            }
        } catch (Exception e) {
            image.setText("No Image");
        }
        card.add(image, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblName = new JLabel(name);
        lblName.setFont(Theme.NORMAL);
        JLabel lblPrice = new JLabel("₹ " + price);
        lblPrice.setFont(Theme.HEADER);
        lblPrice.setForeground(Theme.BLUE);
        JLabel lblStock = new JLabel("Stock: " + stock);
        lblStock.setFont(Theme.SMALL);

        JButton delete = createBtn("Delete", Color.RED);
        delete.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection()) {
                con.createStatement().executeUpdate("DELETE FROM products WHERE id=" + id);
                loadProducts();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        info.add(lblName);
        info.add(lblPrice);
        info.add(lblStock);
        info.add(Box.createVerticalStrut(10));
        info.add(delete);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    /* ORDERS */

    void loadOrders() {

        currentPage = "orders";

        btnPanel.removeAll();
        btnPanel.add(btnBack);

        loadTable(
                "SELECT o.id, u.username AS customer, o.total_amount, o.status, o.order_date " +
                        "FROM orders o JOIN users u ON o.user_id=u.id ORDER BY o.order_date DESC");
    }

    /* SUPPLIERS */

    void loadSuppliers() {

        currentPage = "users"; // We target the users table for deletions/updates

        btnPanel.removeAll();
        btnPanel.add(btnEdit);
        btnPanel.add(btnAccept);
        btnPanel.add(btnDelete);
        btnPanel.add(btnBack);

        loadTable("SELECT id,username,email,approved FROM users WHERE role='supplier'");
    }

    /* GENERIC TABLE */

    void loadTable(String sql) {

        contentLayout.show(dynamicContent, "table");

        try (Connection con = DBConnection.getConnection()) {

            model.setRowCount(0);
            model.setColumnCount(0);

            ResultSet rs = con.createStatement().executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();

            int cols = meta.getColumnCount();

            for (int i = 1; i <= cols; i++)
                model.addColumn(meta.getColumnLabel(i));

            while (rs.next()) {

                Object[] row = new Object[cols];

                for (int i = 0; i < cols; i++)
                    row[i] = rs.getObject(i + 1);

                model.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* SEARCH */

    void search() {

        String key = searchField.getText();

        if (currentPage.equals("users"))
            loadTable("SELECT id,username,email FROM users WHERE username LIKE '%" + key + "%'");

        if (currentPage.equals("products"))
            loadProducts();
    }

    /* DELETE */

    void deleteRow() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row first");
            return;
        }

        int id = (int) table.getValueAt(row, 0);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement("DELETE FROM " + currentPage + " WHERE id=?");

            ps.setInt(1, id);
            ps.executeUpdate();

            model.removeRow(row);

            JOptionPane.showMessageDialog(this, "Deleted successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* APPROVE SUPPLIER */

    void approveSupplier() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select supplier first");
            return;
        }

        int id = (int) table.getValueAt(row, 0);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement("UPDATE users SET approved=1 WHERE id=?");

            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Supplier approved");

            loadSuppliers();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* EDIT USER / SUPPLIER */

    void editUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user/supplier first");
            return;
        }

        int id = (int) table.getValueAt(row, 0);
        String currentRole = "user"; // To know how to reload the table

        JDialog dialog = new JDialog(this, "Edit User Details", true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtMobile = new JTextField(20);
        JTextField txtAge = new JTextField(20);
        JTextArea txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"user", "supplier", "admin"});
        JCheckBox chkApproved = new JCheckBox("Approved");
        chkApproved.setBackground(Theme.BG);

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtName.setText(rs.getString("username"));
                txtEmail.setText(rs.getString("email"));
                txtMobile.setText(rs.getString("mobile"));
                txtAge.setText(String.valueOf(rs.getInt("age")));
                txtAddress.setText(rs.getString("address"));
                currentRole = rs.getString("role");
                roleBox.setSelectedItem(currentRole);
                chkApproved.setSelected(rs.getInt("approved") == 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String finalCurrentRole = currentRole;

        JButton btnSave = createBtn("Save Details", Theme.ORANGE);
        btnSave.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE users SET username=?, email=?, mobile=?, age=?, address=?, role=?, approved=? WHERE id=?");
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtEmail.getText().trim());
                ps.setString(3, txtMobile.getText().trim());
                try {
                    ps.setInt(4, Integer.parseInt(txtAge.getText().trim()));
                } catch(Exception ex) {
                    ps.setNull(4, java.sql.Types.INTEGER);
                }
                ps.setString(5, txtAddress.getText().trim());
                ps.setString(6, roleBox.getSelectedItem().toString());
                ps.setInt(7, chkApproved.isSelected() ? 1 : 0);
                ps.setInt(8, id);
                ps.executeUpdate();
                
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Details Updated Successfully!");
                
                // Reload appropriate table
                if (finalCurrentRole.equals("supplier") || roleBox.getSelectedItem().toString().equals("supplier")) {
                    loadSuppliers();
                } else {
                    loadUsers();
                }
            } catch (SQLIntegrityConstraintViolationException ex) {
                JOptionPane.showMessageDialog(dialog, "Username or Email already exists!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        int gridy = 0;
        gbc.gridx = 0; gbc.gridy = gridy; dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; dialog.add(txtName, gbc);

        gridy++;
        gbc.gridx = 0; gbc.gridy = gridy; dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; dialog.add(txtEmail, gbc);

        gridy++;
        gbc.gridx = 0; gbc.gridy = gridy; dialog.add(new JLabel("Mobile:"), gbc);
        gbc.gridx = 1; dialog.add(txtMobile, gbc);

        gridy++;
        gbc.gridx = 0; gbc.gridy = gridy; dialog.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1; dialog.add(txtAge, gbc);

        gridy++;
        gbc.gridx = 0; gbc.gridy = gridy; dialog.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; dialog.add(new JScrollPane(txtAddress), gbc);
        
        gridy++;
        gbc.gridx = 0; gbc.gridy = gridy; dialog.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; dialog.add(roleBox, gbc);
        
        gridy++;
        gbc.gridx = 0; gbc.gridy = gridy; dialog.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; dialog.add(chkApproved, gbc);

        gridy++;
        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2; dialog.add(btnSave, gbc);

        dialog.setVisible(true);
    }

    /* ICONS */

    ImageIcon scale(String path, int w, int h) {

        java.net.URL url = getClass().getResource(path);

        if (url == null)
            return new ImageIcon();

        Image img = new ImageIcon(url).getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);

        return new ImageIcon(img);
    }

    public static void main(String[] args) {

        new AdminDashboard().setVisible(true);
    }
}