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
    JPanel recentOrdersPanel;

    JTextField searchField;
    JComboBox<String> categoryBox;

    JLabel lblWallet, lblCartTotal, lblOrders, lblAddress, lblDashCartTotal;

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
        s.add(menuItem("My Profile", "/icons/profile.png"));
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
                    case "My Profile":
                        editProfile();
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
        JPanel wrapper = new JPanel(new BorderLayout(30, 30));
        wrapper.setBackground(Theme.BG);

        JPanel p = new JPanel(new GridLayout(1, 4, 20, 0));
        p.setBackground(Theme.BG);

        lblWallet = new JLabel("₹ 0.00");
        lblDashCartTotal = new JLabel("₹ 0.00");
        lblOrders = new JLabel("0");
        lblAddress = new JLabel("-");

        p.add(statCard("Wallet Balance", lblWallet, Theme.BLUE));
        p.add(statCard("Cart Total", lblDashCartTotal, Theme.ORANGE));
        p.add(statCard("Total Orders", lblOrders, Theme.YELLOW));
        p.add(statCard("Shipping Address", lblAddress, new Color(100, 100, 100)));

        wrapper.add(p, BorderLayout.NORTH);

        JPanel recentPanel = new JPanel(new BorderLayout(0, 15));
        recentPanel.setBackground(Theme.BG);
        JLabel lblRecent = new JLabel("Recent Activity");
        lblRecent.setFont(Theme.HEADER);
        recentPanel.add(lblRecent, BorderLayout.NORTH);

        recentOrdersPanel = new JPanel();
        recentOrdersPanel.setLayout(new BoxLayout(recentOrdersPanel, BoxLayout.Y_AXIS));
        recentOrdersPanel.setBackground(Color.WHITE);
        
        JScrollPane scroll = new JScrollPane(recentOrdersPanel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);
        recentPanel.add(scroll, BorderLayout.CENTER);

        wrapper.add(recentPanel, BorderLayout.CENTER);

        return wrapper;
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
                        rs.getInt("stock"),
                        rs.getString("category")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        layout.show(content, "products");
        productPanel.revalidate();
        productPanel.repaint();
    }

    JPanel productCard(int id, String name, double price, String img, int stock, String category) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel image = new JLabel("", SwingConstants.CENTER);
        image.setPreferredSize(new Dimension(0, 180));
        try {
            java.net.URL imgUrl = getClass().getResource(img);
            if(imgUrl != null) {
                image.setIcon(new ImageIcon(new ImageIcon(imgUrl).getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH)));
            } else {
                java.io.File f = new java.io.File("src" + img);
                if(f.exists()){
                    image.setIcon(new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH)));
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

        JComboBox<String> sizeBox = null;
        if (category != null && (category.toLowerCase().contains("fashion") || category.toLowerCase().contains("footwear") || category.toLowerCase().contains("clothing") || category.toLowerCase().contains("shoes"))) {
            String[] sizes = {"S", "M", "L", "XL", "XXL"};
            if (category.toLowerCase().contains("footwear") || category.toLowerCase().contains("shoes")) {
                sizes = new String[]{"6", "7", "8", "9", "10", "11"};
            }
            sizeBox = new JComboBox<>(sizes);
            sizeBox.setMaximumSize(new Dimension(80, 25));
            sizeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        final JComboBox<String> finalSizeBox = sizeBox;
        JButton add = createBtn("Add To Cart", Theme.ORANGE);
        add.setAlignmentX(Component.CENTER_ALIGNMENT);
        add.setEnabled(stock > 0);
        add.addActionListener(e -> {
            String selectedSize = finalSizeBox != null ? (String) finalSizeBox.getSelectedItem() : "";
            addToCart(id, price, selectedSize);
        });

        info.add(lblName);
        info.add(Box.createVerticalStrut(5));
        info.add(lblPrice);
        info.add(Box.createVerticalStrut(5));
        info.add(lblStock);
        info.add(Box.createVerticalStrut(15));
        if (sizeBox != null) {
            JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            sizePanel.setBackground(Color.WHITE);
            JLabel sizeLbl = new JLabel("Select Size:");
            sizeLbl.setFont(Theme.SMALL);
            sizePanel.add(sizeLbl);
            sizePanel.add(sizeBox);
            info.add(sizePanel);
            info.add(Box.createVerticalStrut(10));
        }
        info.add(add);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    void addToCart(int productId, double price, String size) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ck = con.prepareStatement("SELECT stock FROM products WHERE id=?");
            ck.setInt(1, productId);
            ResultSet rs = ck.executeQuery();
            if (rs.next() && rs.getInt(1) <= 0) {
                JOptionPane.showMessageDialog(this, "Product Out of Stock!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO cart(user_id,product_id,quantity,price,size) VALUES(?,?,1,?,?)");
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setDouble(3, price);
            ps.setString(4, size);
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
        lblCartTotal = new JLabel("₹ 0.00");
        lblCartTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblCartTotal.setForeground(Theme.BLUE);

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.add(lblTotal);
        totalPanel.add(lblCartTotal);

        JButton checkout = createBtn("Place Order", Theme.ORANGE);
        checkout.setPreferredSize(new Dimension(200, 50));
        checkout.addActionListener(e -> showCheckoutDialog());

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
        int itemCount = 0;
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT c.id,p.name,c.quantity,c.price,p.image_path,c.size FROM cart c JOIN products p ON c.product_id=p.id WHERE c.user_id=?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                itemCount++;
                cartPanel.add(cartCard(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("image_path"),
                        rs.getString("size")));
            }
            if (itemCount == 0) {
                JLabel emptyLabel = new JLabel("No items in the cart", SwingConstants.CENTER);
                emptyLabel.setFont(Theme.HEADER);
                emptyLabel.setForeground(Theme.GRAY);
                emptyLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
                cartPanel.add(emptyLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateCartTotal();
        layout.show(content, "cart");
        cartPanel.revalidate();
        cartPanel.repaint();
    }

    JPanel cartCard(int id, String name, int qty, double price, String img, String size) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Selection Checkbox
        JCheckBox select = new JCheckBox();
        select.setBackground(Color.WHITE);
        select.setSelected(selectedCartIds.contains(id));
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
            java.net.URL imgUrl = getClass().getResource(img);
            if(imgUrl != null) {
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
        mainInfo.add(image, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(1, 3, 20, 0));
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        String displayName = name;
        if (size != null && !size.trim().isEmpty()) {
            displayName += " (Size: " + size + ")";
        }
        JLabel lblName = new JLabel(displayName);
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
            selectedCartIds.remove((Integer) id);
            loadCart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showCheckoutDialog() {
        if (selectedCartIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select items to buy!");
            return;
        }
        
        try (Connection con = DBConnection.getConnection()) {
            String placeholders = selectedCartIds.toString().replace("[", "(").replace("]", ")");
            
            // Get Items for Summary
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT c.id, p.name, c.quantity, c.price, c.size FROM cart c JOIN products p ON c.product_id=p.id WHERE c.id IN " + placeholders);
                    
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body style='font-family:sans-serif;'><h2>Order Summary</h2><table width='400' cellpadding='5'>");
            sb.append("<tr bgcolor='#f0f0f0'><th align='left'>Product</th><th>Size</th><th>Qty</th><th align='right'>Price</th></tr>");
            
            double totalAmount = 0;
            while (rs.next()) {
                String name = rs.getString("name");
                if (name.length() > 20) name = name.substring(0, 17) + "...";
                String size = rs.getString("size");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                double itemTotal = qty * price;
                totalAmount += itemTotal;
                
                sb.append("<tr>");
                sb.append("<td>").append(name).append("</td>");
                sb.append("<td align='center'>").append(size != null && !size.isEmpty() ? size : "-").append("</td>");
                sb.append("<td align='center'>").append(qty).append("</td>");
                sb.append("<td align='right'>₹ ").append(String.format("%.2f", itemTotal)).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table><hr>");
            sb.append("<h2 style='color:blue;' align='right'>Final Total: ₹ ").append(String.format("%.2f", totalAmount)).append("</h2>");
            sb.append("</body></html>");
            
            // Check Wallet
            PreparedStatement wallet = con.prepareStatement("SELECT balance FROM wallet WHERE user_id=?");
            wallet.setInt(1, userId);
            ResultSet wr = wallet.executeQuery();
            wr.next();
            double balance = wr.getDouble(1);

            if (balance < totalAmount) {
                JOptionPane.showMessageDialog(this, "Insufficient Wallet Balance!\nRequired: ₹ " + String.format("%.2f", totalAmount) + "\nYour Wallet: ₹ " + String.format("%.2f", balance));
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, sb.toString(), "Confirm Checkout", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                executeOrder(totalAmount, placeholders);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void executeOrder(double totalAmount, String placeholders) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

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
                    .executeQuery("SELECT product_id, quantity, price, size FROM cart WHERE id IN " + placeholders);

            PreparedStatement insItem = con
                    .prepareStatement("INSERT INTO order_items(order_id, product_id, quantity, price, size) VALUES(?,?,?,?,?)");
            PreparedStatement updStock = con.prepareStatement("UPDATE products SET stock=stock-? WHERE id=?");

            StringBuilder billText = new StringBuilder();
            billText.append("<html><body style='font-family:sans-serif;'><h2>Invoice / Bill</h2><hr>");
            billText.append("<b>Order ID:</b> #").append(orderId).append("<br>");
            billText.append("<b>Date:</b> ").append(new java.util.Date().toString()).append("<br><br>");
            billText.append("<table width='350' cellpadding='4'><tr bgcolor='#e0e0e0'><th>Prod ID</th><th>Qty</th><th>Size</th><th align='right'>Price</th></tr>");

            while (cr.next()) {
                int pid = cr.getInt("product_id");
                int qty = cr.getInt("quantity");
                double pr = cr.getDouble("price");
                String sz = cr.getString("size");

                insItem.setInt(1, orderId);
                insItem.setInt(2, pid);
                insItem.setInt(3, qty);
                insItem.setDouble(4, pr);
                insItem.setString(5, sz);
                insItem.executeUpdate();

                updStock.setInt(1, qty);
                updStock.setInt(2, pid);
                updStock.executeUpdate();
                
                billText.append("<tr><td align='center'>").append(pid)
                        .append("</td><td align='center'>").append(qty)
                        .append("</td><td align='center'>").append(sz != null && !sz.isEmpty() ? sz : "-")
                        .append("</td><td align='right'>₹ ").append(String.format("%.2f", pr * qty))
                        .append("</td></tr>");
            }
            
            billText.append("</table><hr>");
            billText.append("<b>Total Deducted:</b> ₹ ").append(String.format("%.2f", totalAmount)).append("<br><br>");
            billText.append("<i>Thank you for shopping with AVVJ Cart!</i>");
            billText.append("</body></html>");

            // Clear Selected items from Cart
            con.createStatement().executeUpdate("DELETE FROM cart WHERE id IN " + placeholders);
            selectedCartIds.clear();

            con.commit();
            
            // Show Bill Dialog
            showBillDialog(orderId, billText.toString());
            
            loadStats();
            loadOrders();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Order Failed: " + e.getMessage());
        }
    }

    void showBillDialog(int orderId, String htmlBill) {
        JDialog dialog = new JDialog(this, "Order Receipt", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JEditorPane billArea = new JEditorPane();
        billArea.setContentType("text/html");
        billArea.setText(htmlBill);
        billArea.setEditable(false);
        dialog.add(new JScrollPane(billArea), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        btnPanel.setBackground(Color.WHITE);

        JButton download = createBtn("Download PDF", Theme.BLUE);
        download.addActionListener(e -> BillGenerator.generateBill(orderId));

        JButton close = createBtn("Close", Theme.GRAY);
        close.addActionListener(e -> dialog.dispose());

        btnPanel.add(download);
        btnPanel.add(close);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
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
            String sql = "SELECT o.id as oid, o.status, o.order_date, p.name, p.image_path, oi.price, oi.quantity, u.address, oi.size "
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
                        rs.getString("address"),
                        rs.getString("size")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        layout.show(content, "orders");
        orderPanel.revalidate();
        orderPanel.repaint();
    }

    JPanel orderDetailCard(int oid, String name, String img, double price, int qty, String status, Timestamp date,
            String address, String size) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel image = new JLabel("", SwingConstants.CENTER);
        try {
            java.net.URL imgUrl = getClass().getResource(img);
            if(imgUrl != null) {
                image.setIcon(new ImageIcon(new ImageIcon(imgUrl).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
            } else {
                java.io.File f = new java.io.File("src" + img);
                if(f.exists()){
                    image.setIcon(new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
                } else {
                    image.setText("No Image");
                }
            }
        } catch (Exception e) {
            image.setText("No Image");
        }
        card.add(image, BorderLayout.WEST);

        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 10, 0, 10);

        // Product & Order ID
        JPanel namePanel = new JPanel(new GridLayout(3, 1));
        namePanel.setBackground(Color.WHITE);
        String dName = name;
        if(dName.length() > 25) dName = dName.substring(0,22) + "...";
        JLabel lblName = new JLabel("<html><div style='width:160px;'>" + dName + "</div></html>");
        lblName.setFont(Theme.HEADER);
        JLabel lblOid = new JLabel("Order #" + oid + " | Qty: " + qty);
        lblOid.setFont(Theme.SMALL);
        lblOid.setForeground(Theme.GRAY);
        JLabel lblSize = new JLabel(size != null && !size.trim().isEmpty() ? "Size: " + size : " ");
        lblSize.setFont(Theme.SMALL);
        lblSize.setForeground(Theme.GRAY);
        namePanel.add(lblName);
        namePanel.add(lblOid);
        namePanel.add(lblSize);

        // Price & Date
        JPanel pricePanel = new JPanel(new GridLayout(2, 1));
        pricePanel.setBackground(Color.WHITE);
        JLabel lblPrice = new JLabel("₹ " + String.format("%.2f", price * qty));
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
        JLabel lblAddress = new JLabel("<html><div style='width:180px; color:#555555;'><b>Ship To:</b><br>"
                + displayAddress + "</div></html>");
        lblAddress.setFont(Theme.SMALL);
        lblAddress.setVerticalAlignment(SwingConstants.TOP);
        addressPanel.add(lblAddress, BorderLayout.CENTER);

        // Status & Download
        JPanel statusPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        statusPanel.setBackground(Color.WHITE);
        JLabel lblStatus = new JLabel(status, SwingConstants.RIGHT);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setForeground(new Color(0, 180, 0));
        
        JButton btnDownload = new JButton("Download Bill");
        btnDownload.setFont(Theme.SMALL);
        btnDownload.setForeground(Theme.BLUE);
        btnDownload.setBorder(BorderFactory.createLineBorder(Theme.BLUE));
        btnDownload.setContentAreaFilled(false);
        btnDownload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDownload.addActionListener(e -> BillGenerator.generateBill(oid));
        
        statusPanel.add(lblStatus);
        statusPanel.add(btnDownload);
        
        gbc.weightx = 1.0; gbc.gridx = 0; info.add(namePanel, gbc);
        gbc.weightx = 0.5; gbc.gridx = 1; info.add(pricePanel, gbc);
        gbc.weightx = 1.0; gbc.gridx = 2; info.add(addressPanel, gbc);
        gbc.weightx = 0.5; gbc.gridx = 3; info.add(statusPanel, gbc);

        card.add(info, BorderLayout.CENTER);
        return card;
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
            ps.setInt(1, userId);
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
                ps.setInt(6, userId);
                ps.executeUpdate();
                
                // Update header username and dash components
                this.username = txtName.getText().trim();
                Component[] comps = ((JPanel)getContentPane().getComponent(0)).getComponents();
                for(Component c: comps) {
                    if (c instanceof JPanel && ((JPanel)c).getPreferredSize().height == 70) {
                        JLabel t = (JLabel) ((JPanel)c).getComponent(0);
                        t.setText("Welcome back, " + this.username);
                    }
                }
                lblAddress.setText("<html><div style='width:120px;'>" + txtAddress.getText().trim() + "</div></html>");
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Profile Updated Successfully!");
                loadStats();
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
            lblDashCartTotal.setText("₹ " + String.format("%.2f", total));

            if (recentOrdersPanel != null) {
                recentOrdersPanel.removeAll();
                ResultSet rsRecent = con.createStatement().executeQuery(
                        "SELECT id, total_amount, status, order_date FROM orders WHERE user_id=" + userId + " ORDER BY order_date DESC LIMIT 5");
                
                boolean hasRecent = false;
                while(rsRecent.next()) {
                    hasRecent = true;
                    JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
                    row.setBackground(Color.WHITE);
                    row.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                    
                    row.add(new JLabel("Order #" + rsRecent.getInt("id")));
                    row.add(new JLabel(rsRecent.getTimestamp("order_date").toString().substring(0, 16)));
                    
                    JLabel amt = new JLabel("₹ " + String.format("%.2f", rsRecent.getDouble("total_amount")));
                    amt.setForeground(Theme.BLUE);
                    amt.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    row.add(amt);
                    
                    JLabel st = new JLabel(rsRecent.getString("status"));
                    st.setForeground(new Color(0, 150, 0));
                    row.add(st);
                    
                    recentOrdersPanel.add(row);
                    recentOrdersPanel.add(new JSeparator());
                }
                if (!hasRecent) {
                    JLabel none = new JLabel("No recent orders found.");
                    none.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                    none.setForeground(Theme.GRAY);
                    recentOrdersPanel.add(none);
                }
                recentOrdersPanel.revalidate();
                recentOrdersPanel.repaint();
            }

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