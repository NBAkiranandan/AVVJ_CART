package user;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SupplierDashboard extends JFrame {

    int supplierId;
    CardLayout contentLayout;
    JPanel dynamicContent;

    JTable table;
    DefaultTableModel model;

    JLabel lblTotalProducts, lblLowStock, lblPendingOrders, lblRevenue;

    public SupplierDashboard(int supplierId) {
        this.supplierId = supplierId;

        setTitle("AVVJ cart - Supplier Portal");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        root.add(sidebar(), BorderLayout.WEST);
        root.add(header(), BorderLayout.NORTH);
        root.add(mainContent(), BorderLayout.CENTER);

        add(root);
        loadStats();
    }

    ImageIcon scale(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null)
            return new ImageIcon();
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

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
        s.add(menuItem("Manage Inventory", "/icons/products.png"));
        s.add(menuItem("My Orders", "/icons/orders.png"));
        s.add(menuItem("Revenue Stats", "/icons/revenue.png"));
        s.add(menuItem("My Profile", "/icons/profile.png"));

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
                        loadStats();
                        break;
                    case "Manage Inventory":
                        loadProducts();
                        break;
                    case "My Orders":
                        loadSupplierOrders();
                        break;
                    case "Revenue Stats":
                        showRevenueStats();
                        break;
                    case "My Profile":
                        editProfile();
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

    JPanel header() {
        JPanel h = new JPanel(new BorderLayout());
        h.setPreferredSize(new Dimension(0, 70));
        h.setBackground(Theme.BLUE);
        h.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel title = new JLabel("Supplier Business Console");
        title.setFont(Theme.HEADER);
        title.setForeground(Color.WHITE);
        h.add(title, BorderLayout.WEST);

        return h;
    }

    JPanel mainContent() {
        contentLayout = new CardLayout();
        dynamicContent = new JPanel(contentLayout);
        dynamicContent.setBackground(Theme.BG);
        dynamicContent.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel dash = new JPanel(new GridLayout(2, 2, 30, 30));
        dash.setBackground(Theme.BG);

        lblTotalProducts = new JLabel("0");
        lblLowStock = new JLabel("0");
        lblPendingOrders = new JLabel("0");
        lblRevenue = new JLabel("₹ 0.00");

        dash.add(statCard("Total SKUs", lblTotalProducts, Theme.BLUE));
        dash.add(statCard("Inventory Alerts", lblLowStock, Color.RED));
        dash.add(statCard("Active Shipments", lblPendingOrders, Theme.ORANGE));
        dash.add(statCard("Estimated Earnings", lblRevenue, new Color(0, 150, 136)));

        JPanel tablePanel = new JPanel(new BorderLayout(0, 20));
        tablePanel.setBackground(Theme.BG);
        model = new DefaultTableModel();
        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(Theme.NORMAL);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btns.setBackground(Theme.BG);
        JButton btnAdd = createBtn("Add Product", Theme.ORANGE);
        JButton btnDelete = createBtn("Delete Selection", Color.RED);
        JButton btnBack = createBtn("Back", Theme.BLUE);

        btnAdd.addActionListener(e -> addProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnBack.addActionListener(e -> contentLayout.show(dynamicContent, "dashboard"));

        btns.add(btnAdd);
        btns.add(btnDelete);
        btns.add(btnBack);

        tablePanel.add(scroll, BorderLayout.CENTER);
        tablePanel.add(btns, BorderLayout.SOUTH);

        dynamicContent.add(dash, "dashboard");
        dynamicContent.add(tablePanel, "table");

        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBackground(Theme.BG);
        JPanel grid = new JPanel(new GridLayout(0, 3, 20, 20));
        grid.setBackground(Theme.BG);
        JScrollPane scrollInv = new JScrollPane(grid);
        scrollInv.setBorder(null);
        inventoryPanel.add(scrollInv, BorderLayout.CENTER);

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pBtn.add(btnAdd);
        pBtn.add(btnBack);
        inventoryPanel.add(pBtn, BorderLayout.SOUTH);

        dynamicContent.add(inventoryPanel, "inventoryGrid");

        return dynamicContent;
    }

    JButton createBtn(String t, Color bg) {
        JButton b = new JButton(t);
        b.setFont(Theme.NORMAL);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

    void loadStats() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM products WHERE supplier_id=?");
            ps.setInt(1, supplierId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                lblTotalProducts.setText(rs.getString(1));

            PreparedStatement psStock = con
                    .prepareStatement("SELECT COUNT(*) FROM products WHERE supplier_id=? AND stock < 10");
            psStock.setInt(1, supplierId);
            ResultSet rsStock = psStock.executeQuery();
            if (rsStock.next())
                lblLowStock.setText(rsStock.getString(1));

            // Actual Pending Orders and Revenue
            PreparedStatement psOrders = con.prepareStatement(
                    "SELECT COUNT(DISTINCT o.id) FROM orders o JOIN order_items oi ON o.id = oi.order_id JOIN products p ON oi.product_id = p.id WHERE p.supplier_id=? AND o.status='Confirmed'");
            psOrders.setInt(1, supplierId);
            ResultSet rsOrders = psOrders.executeQuery();
            if (rsOrders.next())
                lblPendingOrders.setText(rsOrders.getString(1));

            PreparedStatement psRev = con.prepareStatement(
                    "SELECT SUM(oi.price * oi.quantity) FROM order_items oi JOIN products p ON oi.product_id = p.id WHERE p.supplier_id=?");
            psRev.setInt(1, supplierId);
            ResultSet rsRev = psRev.executeQuery();
            if (rsRev.next())
                lblRevenue.setText("₹ " + String.format("%.2f", rsRev.getDouble(1)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void loadProducts() {
        JPanel scrollPanel = (JPanel) dynamicContent.getComponent(2);
        JScrollPane scroll = (JScrollPane) scrollPanel.getComponent(0);
        JPanel grid = (JPanel) scroll.getViewport().getView();
        grid.removeAll();

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con
                    .prepareStatement("SELECT * FROM products WHERE supplier_id=?");
            pst.setInt(1, supplierId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                grid.add(supplierProductCard(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("image_path"),
                        rs.getString("description"),
                        rs.getString("category")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        contentLayout.show(dynamicContent, "inventoryGrid");
        grid.revalidate();
        grid.repaint();
    }

    JPanel supplierProductCard(int id, String name, double price, int stock, String img, String desc, String cat) {
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
                if (f.exists()) {
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
        JLabel lblCat = new JLabel("[" + cat + "]");
        lblCat.setFont(Theme.SMALL);
        lblCat.setForeground(Theme.GRAY);
        JLabel lblPrice = new JLabel("₹ " + price);
        lblPrice.setFont(Theme.HEADER);
        lblPrice.setForeground(Theme.BLUE);
        JLabel lblStock = new JLabel("Stock: " + stock);
        lblStock.setFont(Theme.SMALL);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btns.setBackground(Color.WHITE);

        JButton edit = createBtn("Edit", Theme.BLUE);
        edit.addActionListener(e -> editProduct(id, name, price, stock, img, desc, cat));

        JButton delete = createBtn("Delete", Color.RED);
        delete.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection()) {
                con.createStatement().executeUpdate("DELETE FROM products WHERE id=" + id);
                loadProducts();
                loadStats();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btns.add(edit);
        btns.add(Box.createHorizontalStrut(10));
        btns.add(delete);

        info.add(lblName);
        info.add(lblCat);
        info.add(lblPrice);
        info.add(lblStock);
        info.add(Box.createVerticalStrut(10));
        info.add(btns);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    void loadSupplierOrders() {
        model.setRowCount(0);
        model.setColumnCount(0);
        model.addColumn("Order ID");
        model.addColumn("Product");
        model.addColumn("Qty");
        model.addColumn("Total");
        model.addColumn("Date");
        model.addColumn("Status");

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT o.id, p.name, oi.quantity, (oi.price*oi.quantity) as total, o.order_date, o.status " +
                            "FROM orders o JOIN order_items oi ON o.id = oi.order_id " +
                            "JOIN products p ON oi.product_id = p.id " +
                            "WHERE p.supplier_id=? ORDER BY o.order_date DESC");
            ps.setInt(1, supplierId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getDouble(4), rs.getTimestamp(5),
                        rs.getString(6)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        contentLayout.show(dynamicContent, "table");
    }

    void showRevenueStats() {
        loadStats();
        contentLayout.show(dynamicContent, "dashboard");
    }

    void addProduct() {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setSize(450, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = new JTextField(20);
        JTextField txtPrice = new JTextField(20);
        JTextField txtStock = new JTextField(20);
        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JComboBox<String> catBox = new JComboBox<>(new String[] { "Electronics", "Fashion", "Footwear" });

        JLabel lblImgPath = new JLabel("No image selected");
        final String[] selectedPath = { "/icons/products.png" };

        JButton btnPick = new JButton("Pick Image");
        btnPick.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser("src/icons");
            int res = chooser.showOpenDialog(dialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = chooser.getSelectedFile();
                String path = selectedFile.getName();
                selectedPath[0] = "/icons/" + path;
                lblImgPath.setText(path);
                try {
                    java.io.File dest = new java.io.File("src/icons", path);
                    if (!selectedFile.getAbsolutePath().equals(dest.getAbsolutePath())) {
                        java.nio.file.Files.copy(selectedFile.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    java.io.File binDest = new java.io.File("build/classes/icons", path);
                    if (!selectedFile.getAbsolutePath().equals(binDest.getAbsolutePath())) {
                        java.nio.file.Files.createDirectories(binDest.getParentFile().toPath());
                        java.nio.file.Files.copy(selectedFile.toPath(), binDest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton btnSave = createBtn("Save Product", Theme.ORANGE);
        btnSave.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO products(name, price, stock, supplier_id, image_path, description, category) VALUES(?,?,?,?,?,?,?)");
                pst.setString(1, txtName.getText());
                pst.setDouble(2, Double.parseDouble(txtPrice.getText()));
                pst.setInt(3, Integer.parseInt(txtStock.getText()));
                pst.setInt(4, supplierId);
                pst.setString(5, selectedPath[0]);
                pst.setString(6, txtDesc.getText());
                pst.setString(7, catBox.getSelectedItem().toString());
                pst.executeUpdate();
                loadProducts();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Product Added!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtName, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtPrice, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtStock, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        dialog.add(catBox, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        dialog.add(new JScrollPane(txtDesc), gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Image:"), gbc);
        gbc.gridx = 1;
        dialog.add(btnPick, gbc);

        row++;
        gbc.gridx = 1;
        gbc.gridy = row;
        dialog.add(lblImgPath, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(btnSave, gbc);

        dialog.setVisible(true);
    }

    void editProduct(int id, String name, double price, int stock, String img, String desc, String cat) {
        JDialog dialog = new JDialog(this, "Edit Product", true);
        dialog.setSize(450, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = new JTextField(name, 20);
        JTextField txtPrice = new JTextField(String.valueOf(price), 20);
        JTextField txtStock = new JTextField(String.valueOf(stock), 20);
        JTextArea txtDesc = new JTextArea(desc, 3, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JComboBox<String> catBox = new JComboBox<>(new String[] { "Electronics", "Fashion", "Footwear" });
        catBox.setSelectedItem(cat);

        JLabel lblImgPath = new JLabel(img.replace("/icons/", ""));
        final String[] selectedPath = { img };

        JButton btnPick = new JButton("Pick Image");
        btnPick.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser("src/icons");
            int res = chooser.showOpenDialog(dialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = chooser.getSelectedFile();
                String path = selectedFile.getName();
                selectedPath[0] = "/icons/" + path;
                lblImgPath.setText(path);
                try {
                    java.io.File dest = new java.io.File("src/icons", path);
                    if (!selectedFile.getAbsolutePath().equals(dest.getAbsolutePath())) {
                        java.nio.file.Files.copy(selectedFile.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    java.io.File binDest = new java.io.File("build/classes/icons", path);
                    if (!selectedFile.getAbsolutePath().equals(binDest.getAbsolutePath())) {
                        java.nio.file.Files.createDirectories(binDest.getParentFile().toPath());
                        java.nio.file.Files.copy(selectedFile.toPath(), binDest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton btnSave = createBtn("Update Product", Theme.ORANGE);
        btnSave.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE products SET name=?, price=?, stock=?, image_path=?, description=?, category=? WHERE id=?");
                pst.setString(1, txtName.getText());
                pst.setDouble(2, Double.parseDouble(txtPrice.getText()));
                pst.setInt(3, Integer.parseInt(txtStock.getText()));
                pst.setString(4, selectedPath[0]);
                pst.setString(5, txtDesc.getText());
                pst.setString(6, catBox.getSelectedItem().toString());
                pst.setInt(7, id);
                pst.executeUpdate();
                loadProducts();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Product Updated!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtName, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtPrice, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtStock, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        dialog.add(catBox, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        dialog.add(new JScrollPane(txtDesc), gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Image:"), gbc);
        gbc.gridx = 1;
        dialog.add(btnPick, gbc);

        row++;
        gbc.gridx = 1;
        gbc.gridy = row;
        dialog.add(lblImgPath, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(btnSave, gbc);

        dialog.setVisible(true);
    }

    void deleteProduct() {
        int row = table.getSelectedRow();
        if (row == -1)
            return;
        int id = (int) table.getValueAt(row, 0);
        try (Connection con = DBConnection.getConnection()) {
            con.createStatement().executeUpdate("DELETE FROM products WHERE id=" + id);
            model.removeRow(row);
            loadStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void editProfile() {
        JDialog dialog = new JDialog(this, "Edit My Profile", true);
        dialog.setSize(450, 450);
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

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE id=?");
            ps.setInt(1, supplierId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtName.setText(rs.getString("username"));
                txtEmail.setText(rs.getString("email"));
                txtMobile.setText(rs.getString("mobile"));
                txtAge.setText(String.valueOf(rs.getInt("age")));
                txtAddress.setText(rs.getString("address"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JButton btnSave = createBtn("Save Details", Theme.ORANGE);
        btnSave.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE users SET username=?, email=?, mobile=?, age=?, address=? WHERE id=?");
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtEmail.getText().trim());
                ps.setString(3, txtMobile.getText().trim());
                try {
                    ps.setInt(4, Integer.parseInt(txtAge.getText().trim()));
                } catch(Exception ex) {
                    ps.setNull(4, java.sql.Types.INTEGER);
                }
                ps.setString(5, txtAddress.getText().trim());
                ps.setInt(6, supplierId);
                ps.executeUpdate();
                
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Profile Updated Successfully!");
            } catch (SQLIntegrityConstraintViolationException ex) {
                JOptionPane.showMessageDialog(dialog, "Username or Email already exists!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; dialog.add(txtName, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; dialog.add(txtEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Mobile:"), gbc);
        gbc.gridx = 1; dialog.add(txtMobile, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1; dialog.add(txtAge, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; dialog.add(new JScrollPane(txtAddress), gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; dialog.add(btnSave, gbc);

        dialog.setVisible(true);
    }
}
