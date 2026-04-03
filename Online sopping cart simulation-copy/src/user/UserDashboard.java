package user;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class UserDashboard extends JFrame {

    int userId;
    String username;

    CardLayout layout;
    JPanel content;

    JPanel productPanel;
    JPanel cartPanel;
    JPanel orderPanel;

    JTextField searchField;
    JComboBox<String> categoryBox;

    JLabel lblWallet, lblCartTotal, lblOrders, lblAddress;

    // Store selected cart item IDs
    List<Integer> selectedCartIds = new ArrayList<>();

    public UserDashboard(int userId) {

        this.userId = userId;
        fetchUsername();

        setTitle("AVVJ Cart - User Dashboard");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());

        root.add(sidebar(), BorderLayout.WEST);
        root.add(header(), BorderLayout.NORTH);
        root.add(mainPanel(), BorderLayout.CENTER);

        add(root);

        ensureWalletExists();
        loadStats();
    }

    void fetchUsername() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT username FROM users WHERE id=?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.username = rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.username = "User";
        }
    }

    JPanel sidebar() {
        JPanel s = new JPanel();
        s.setPreferredSize(new Dimension(260, 0));
        s.setBackground(Color.WHITE);
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JLabel logo = new JLabel("  AVVJ Cart");
        logo.setFont(Theme.TITLE);
        logo.setForeground(Theme.BLUE);
        logo.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        s.add(logo);

        s.add(menuItem("Dashboard", "/icons/dashboard.png"));
        s.add(menuItem("Shop Products", "/icons/products.png"));
        s.add(menuItem("My Cart", "/icons/cart.png"));
        s.add(menuItem("My Orders", "/icons/orders.png"));
        s.add(menuItem("Address", "/icons/profile.png"));
        s.add(menuItem("Wallet", "/icons/wallet.png"));

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
                        layout.show(content, "dash");
                        loadStats();
                        break;
                    case "Shop Products":
                        loadProducts();
                        break;
                    case "My Cart":
                        loadCart();
                        break;
                    case "My Orders":
                        loadOrders();
                        break;
                    case "Address":
                        editAddress();
                        break;
                    case "Wallet":
                        rechargeWallet();
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

    ImageIcon scale(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null)
            return new ImageIcon();
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    JPanel header() {
        JPanel h = new JPanel(new BorderLayout());
        h.setPreferredSize(new Dimension(0, 70));
        h.setBackground(Theme.BLUE);
        h.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel title = new JLabel("Welcome back, " + username);
        title.setFont(Theme.HEADER);
        title.setForeground(Color.WHITE);

        h.add(title, BorderLayout.WEST);

        return h;
    }

    JPanel mainPanel() {
        layout = new CardLayout();
        content = new JPanel(layout);
        content.setBackground(Theme.BG);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        content.add(dashboard(), "dash");
        content.add(productsPanel(), "products");
        content.add(cartPanel(), "cart");
        content.add(orderPanel(), "orders");

        return content;
    }

    JPanel dashboard() {
        JPanel p = new JPanel(new GridLayout(2, 2, 30, 30));
        p.setBackground(Theme.BG);

        lblWallet = new JLabel("₹ 0.00");
        lblCartTotal = new JLabel("₹ 0.00");
        lblOrders = new JLabel("0");
        lblAddress = new JLabel("-");

        p.add(statCard("Wallet Balance", lblWallet, Theme.BLUE));
        p.add(statCard("Cart Total", lblCartTotal, Theme.ORANGE));
        p.add(statCard("Total Orders", lblOrders, Theme.YELLOW));
        p.add(statCard("Shipping Address", lblAddress, new Color(100, 100, 100)));

        return p;
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

    JPanel productsPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Theme.BG);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        searchField = new JTextField(20);
        searchField.setFont(Theme.NORMAL);
        JButton searchBtn = createBtn("Search", Theme.BLUE);

        categoryBox = new JComboBox<>(new String[] { "All", "Electronics", "Fashion", "Footwear" });
        categoryBox.setFont(Theme.NORMAL);

        searchBtn.addActionListener(e -> loadProducts());

        top.add(new JLabel("Find Products: "));
        top.add(searchField);
        top.add(categoryBox);
        top.add(searchBtn);

        productPanel = new JPanel(new GridLayout(0, 3, 25, 25));
        productPanel.setBackground(Theme.BG);
        productPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JScrollPane scroll = new JScrollPane(productPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG);

        container.add(top, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);

        return container;
    }

    JButton createBtn(String t, Color bg) {
        JButton b = new JButton(t);
        b.setFont(Theme.NORMAL);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    void loadProducts() {
        productPanel.removeAll();
        String keyword = searchField.getText();
        String category = categoryBox.getSelectedItem().toString();

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM products WHERE name LIKE ?";
            if (!category.equals("All"))
                sql += " AND category = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            if (!category.equals("All")) {
                ps.setString(2, category);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                productPanel.add(productCard(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("image_path"),
                        rs.getInt("stock")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        layout.show(content, "products");
        productPanel.revalidate();
        productPanel.repaint();
    }

    JPanel productCard(int id, String name, double price, String img, int stock) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel image = new JLabel("", SwingConstants.CENTER);
        image.setPreferredSize(new Dimension(0, 180));
        try {
            image.setIcon(new ImageIcon(new ImageIcon(getClass().getResource(img))
                    .getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            image.setText("No Image");
        }
        card.add(image, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblName = new JLabel(name);
        lblName.setFont(Theme.NORMAL);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPrice = new JLabel("₹ " + price);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPrice.setForeground(Theme.BLUE);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblStock = new JLabel(stock > 0 ? "In Stock: " + stock : "Out of Stock");
        lblStock.setFont(Theme.SMALL);
        lblStock.setForeground(stock > 0 ? new Color(0, 150, 0) : Color.RED);
        lblStock.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton add = createBtn("Add To Cart", Theme.ORANGE);
        add.setAlignmentX(Component.CENTER_ALIGNMENT);
        add.setEnabled(stock > 0);
        add.addActionListener(e -> addToCart(id, price));

        info.add(lblName);
        info.add(Box.createVerticalStrut(5));
        info.add(lblPrice);
        info.add(Box.createVerticalStrut(5));
        info.add(lblStock);
        info.add(Box.createVerticalStrut(15));
        info.add(add);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    void addToCart(int productId, double price) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ck = con.prepareStatement("SELECT stock FROM products WHERE id=?");
            ck.setInt(1, productId);
            ResultSet rs = ck.executeQuery();
            if (rs.next() && rs.getInt(1) <= 0) {
                JOptionPane.showMessageDialog(this, "Product Out of Stock!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO cart(user_id,product_id,quantity,price) VALUES(?,?,1,?)");
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setDouble(3, price);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product Added To Cart");
            loadCart();
            loadStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    JPanel cartPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Theme.BG);

        cartPanel = new JPanel(new GridLayout(0, 1, 15, 15));
        cartPanel.setBackground(Theme.BG);

        JPanel bottom = new JPanel(new BorderLayout(20, 0));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblTotal = new JLabel("Selected Items Total: ");
        lblTotal.setFont(Theme.HEADER);
        lblCartTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblCartTotal.setForeground(Theme.BLUE);

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.add(lblTotal);
        totalPanel.add(lblCartTotal);

        JButton checkout = createBtn("Place Order", Theme.ORANGE);
        checkout.setPreferredSize(new Dimension(200, 50));
        checkout.addActionListener(e -> placeOrder());

        bottom.add(totalPanel, BorderLayout.WEST);
        bottom.add(checkout, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(cartPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG);

        container.add(scroll, BorderLayout.CENTER);
        container.add(bottom, BorderLayout.SOUTH);

        return container;
    }

    void loadCart() {
        cartPanel.removeAll();
        selectedCartIds.clear(); // Clear selections on load
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT c.id,p.name,c.quantity,c.price,p.image_path FROM cart c JOIN products p ON c.product_id=p.id WHERE c.user_id=?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cartPanel.add(cartCard(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("image_path")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateCartTotal();
        layout.show(content, "cart");
        cartPanel.revalidate();
        cartPanel.repaint();
    }

    JPanel cartCard(int id, String name, int qty, double price, String img) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Selection Checkbox
        JCheckBox select = new JCheckBox();
        select.setBackground(Color.WHITE);
        select.addActionListener(e -> {
            if (select.isSelected())
                selectedCartIds.add(id);
            else
                selectedCartIds.remove((Integer) id);
            updateCartTotal();
        });
        card.add(select, BorderLayout.WEST);

        JPanel mainInfo = new JPanel(new BorderLayout(20, 0));
        mainInfo.setBackground(Color.WHITE);

        JLabel image = new JLabel("", SwingConstants.CENTER);
        try {
            image.setIcon(new ImageIcon(new ImageIcon(getClass().getResource(img))
                    .getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            image.setText("No Image");
        }
        mainInfo.add(image, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(1, 3, 20, 0));
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JLabel lblName = new JLabel(name);
        lblName.setFont(Theme.NORMAL);
        info.add(lblName);

        info.add(new JLabel("₹ " + price));

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        qtyPanel.setBackground(Color.WHITE);
        JButton minus = new JButton("-");
        JButton plus = new JButton("+");
        JLabel q = new JLabel("" + qty);
        q.setFont(Theme.HEADER);

        minus.addActionListener(e -> updateQty(id, -1, qty));
        plus.addActionListener(e -> updateQty(id, 1, qty));

        qtyPanel.add(minus);
        qtyPanel.add(Box.createHorizontalStrut(10));
        qtyPanel.add(q);
        qtyPanel.add(Box.createHorizontalStrut(10));
        qtyPanel.add(plus);

        info.add(qtyPanel);

        mainInfo.add(info, BorderLayout.CENTER);

        JButton remove = new JButton("Remove");
        remove.setForeground(Color.RED);
        remove.setBorderPainted(false);
        remove.setContentAreaFilled(false);
        remove.setCursor(new Cursor(Cursor.HAND_CURSOR));
        remove.addActionListener(e -> removeFromCart(id));

        card.add(mainInfo, BorderLayout.CENTER);
        card.add(remove, BorderLayout.EAST);

        return card;
    }

    void updateCartTotal() {
        if (selectedCartIds.isEmpty()) {
            lblCartTotal.setText("₹ 0.00");
            return;
        }

        double total = 0;
        try (Connection con = DBConnection.getConnection()) {
            String placeholders = selectedCartIds.toString().replace("[", "(").replace("]", ")");
            ResultSet rs = con.createStatement().executeQuery(
                    "SELECT SUM(quantity*price) FROM cart WHERE id IN " + placeholders);
            if (rs.next())
                total = rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        lblCartTotal.setText("₹ " + String.format("%.2f", total));
    }

    void updateQty(int id, int change, int currentQty) {
        if (currentQty + change < 1)
            return;
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE cart SET quantity=quantity+? WHERE id=?");
            ps.setInt(1, change);
            ps.setInt(2, id);
            ps.executeUpdate();
            loadCart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void removeFromCart(int id) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM cart WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            loadCart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void placeOrder() {
        if (selectedCartIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select items to buy!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            String placeholders = selectedCartIds.toString().replace("[", "(").replace("]", ")");

            // Get Total for Selected
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT SUM(quantity*price) FROM cart WHERE id IN " + placeholders);
            rs.next();
            double totalAmount = rs.getDouble(1);

            // Check Wallet
            PreparedStatement wallet = con.prepareStatement("SELECT balance FROM wallet WHERE user_id=?");
            wallet.setInt(1, userId);
            ResultSet wr = wallet.executeQuery();
            wr.next();
            double balance = wr.getDouble(1);

            if (balance < totalAmount) {
                JOptionPane.showMessageDialog(this, "Insufficient Wallet Balance");
                return;
            }

            // Deduct Wallet
            PreparedStatement deduct = con.prepareStatement("UPDATE wallet SET balance=balance-? WHERE user_id=?");
            deduct.setDouble(1, totalAmount);
            deduct.setInt(2, userId);
            deduct.executeUpdate();

            // Create Order
            PreparedStatement order = con.prepareStatement(
                    "INSERT INTO orders(user_id,total_amount,status) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
            order.setInt(1, userId);
            order.setDouble(2, totalAmount);
            order.setString(3, "Confirmed");
            order.executeUpdate();

            ResultSet generatedKeys = order.getGeneratedKeys();
            generatedKeys.next();
            int orderId = generatedKeys.getInt(1);

            // Move Selected Cart to Order Items and Deduct Stock
            ResultSet cr = con.createStatement()
                    .executeQuery("SELECT product_id, quantity, price FROM cart WHERE id IN " + placeholders);

            PreparedStatement insItem = con
                    .prepareStatement("INSERT INTO order_items(order_id, product_id, quantity, price) VALUES(?,?,?,?)");
            PreparedStatement updStock = con.prepareStatement("UPDATE products SET stock=stock-? WHERE id=?");

            while (cr.next()) {
                int pid = cr.getInt("product_id");
                int qty = cr.getInt("quantity");
                double pr = cr.getDouble("price");

                insItem.setInt(1, orderId);
                insItem.setInt(2, pid);
                insItem.setInt(3, qty);
                insItem.setDouble(4, pr);
                insItem.executeUpdate();

                updStock.setInt(1, qty);
                updStock.setInt(2, pid);
                updStock.executeUpdate();
            }

            // Clear Selected items from Cart
            con.createStatement().executeUpdate("DELETE FROM cart WHERE id IN " + placeholders);

            con.commit();
            JOptionPane.showMessageDialog(this, "Order Placed Successfully!");
            loadStats();
            loadOrders();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Order Failed: " + e.getMessage());
        }
    }

    JPanel orderPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Theme.BG);

        orderPanel = new JPanel(new GridLayout(0, 1, 20, 20));
        orderPanel.setBackground(Theme.BG);

        JScrollPane scroll = new JScrollPane(orderPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG);

        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    void loadOrders() {
        orderPanel.removeAll();
        try (Connection con = DBConnection.getConnection()) {
            // Fetch items grouped by order or just flat detailed items
            String sql = "SELECT o.id as oid, o.status, o.order_date, p.name, p.image_path, oi.price, oi.quantity, u.address "
                    +
                    "FROM orders o JOIN order_items oi ON o.id = oi.order_id " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE o.user_id = ? ORDER BY o.order_date DESC";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orderPanel.add(orderDetailCard(
                        rs.getInt("oid"),
                        rs.getString("name"),
                        rs.getString("image_path"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("status"),
                        rs.getTimestamp("order_date"),
                        rs.getString("address")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        layout.show(content, "orders");
        orderPanel.revalidate();
        orderPanel.repaint();
    }

    JPanel orderDetailCard(int oid, String name, String img, double price, int qty, String status, Timestamp date,
            String address) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel image = new JLabel("", SwingConstants.CENTER);
        try {
            image.setIcon(new ImageIcon(new ImageIcon(getClass().getResource(img))
                    .getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            image.setText("No Image");
        }
        card.add(image, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(1, 4, 15, 0)); // Changed to 4 columns
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        // Product & Order ID
        JPanel namePanel = new JPanel(new GridLayout(2, 1));
        namePanel.setBackground(Color.WHITE);
        JLabel lblName = new JLabel("<html><p style='width:120px;'>" + name + "</p></html>");
        lblName.setFont(Theme.HEADER);
        JLabel lblOid = new JLabel("Order #" + oid + " | Qty: " + qty);
        lblOid.setFont(Theme.SMALL);
        lblOid.setForeground(Theme.GRAY);
        namePanel.add(lblName);
        namePanel.add(lblOid);

        // Price & Date
        JPanel pricePanel = new JPanel(new GridLayout(2, 1));
        pricePanel.setBackground(Color.WHITE);
        JLabel lblPrice = new JLabel("₹ " + (price * qty));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPrice.setForeground(Theme.BLUE);
        JLabel lblDate = new JLabel(date.toString().substring(0, 16));
        lblDate.setFont(Theme.SMALL);
        lblDate.setForeground(Theme.GRAY);
        pricePanel.add(lblPrice);
        pricePanel.add(lblDate);

        // Address Field
        JPanel addressPanel = new JPanel(new BorderLayout());
        addressPanel.setBackground(Color.WHITE);
        String displayAddress = (address == null || address.trim().isEmpty() || address.equals("-"))
                ? "No Shipping Address Provided"
                : address;
        JLabel lblAddress = new JLabel("<html><div style='width:120px; color:#555555;'><b>Ship To:</b><br>"
                + displayAddress + "</div></html>");
        lblAddress.setFont(Theme.SMALL);
        lblAddress.setVerticalAlignment(SwingConstants.TOP);
        addressPanel.add(lblAddress, BorderLayout.CENTER);

        // Status
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        JLabel lblStatus = new JLabel(status, SwingConstants.RIGHT);
        lblStatus.setFont(Theme.NORMAL);
        lblStatus.setForeground(new Color(0, 150, 0));
        statusPanel.add(lblStatus, BorderLayout.CENTER);

        info.add(namePanel);
        info.add(pricePanel);
        info.add(addressPanel);
        info.add(statusPanel);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    void editAddress() {
        JTextArea area = new JTextArea(lblAddress.getText(), 5, 20);
        int res = JOptionPane.showConfirmDialog(this, new JScrollPane(area), "Edit Address",
                JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE users SET address=? WHERE id=?");
                ps.setString(1, area.getText());
                ps.setInt(2, userId);
                ps.executeUpdate();
                loadStats();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void rechargeWallet() {
        String val = JOptionPane.showInputDialog(this, "Enter Amount to Add:");
        if (val == null || val.isEmpty())
            return;
        try {
            double amount = Double.parseDouble(val);
            if (amount <= 0)
                throw new Exception();
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE wallet SET balance=balance+? WHERE user_id=?");
                ps.setDouble(1, amount);
                ps.setInt(2, userId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Amount Added Successfully!");
                loadStats();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Amount!");
        }
    }

    void loadStats() {
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT balance FROM wallet WHERE user_id=" + userId);
            if (rs.next()) {
                double balance = rs.getDouble(1);
                lblWallet.setText("₹ " + String.format("%.2f", balance));
                if (balance < 0)
                    lblWallet.setForeground(Color.RED);
                else
                    lblWallet.setForeground(new Color(0, 150, 0));
            }

            rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM orders WHERE user_id=" + userId);
            if (rs.next())
                lblOrders.setText(rs.getString(1));

            rs = con.createStatement().executeQuery("SELECT address FROM users WHERE id=" + userId);
            if (rs.next())
                lblAddress.setText(rs.getString(1));

            rs = con.createStatement().executeQuery("SELECT SUM(quantity*price) FROM cart WHERE user_id=" + userId);
            double total = 0;
            if (rs.next())
                total = rs.getDouble(1);
            lblCartTotal.setText("₹ " + String.format("%.2f", total));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void ensureWalletExists() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM wallet WHERE user_id=?");
            pst.setInt(1, userId);
            if (!pst.executeQuery().next()) {
                PreparedStatement ins = con.prepareStatement("INSERT INTO wallet(user_id,balance) VALUES(?,100000)");
                ins.setInt(1, userId);
                ins.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}