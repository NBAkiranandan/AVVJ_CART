package user;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import javax.swing.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Date;

public class BillGenerator {

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static void generateBill(int orderId) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Bill PDF");
        fileChooser.setSelectedFile(new java.io.File("Invoice_Order_" + orderId + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String path = fileChooser.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".pdf")) {
            path += ".pdf";
        }

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // Font Setup
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.BLUE);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);

            // Invoice Header
            Paragraph title = new Paragraph("AVVJ CART - TAX INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("Online Shopping Cart Simulation", smallFont));
            document.add(new Chunk(new LineSeparator()));
            document.add(new Paragraph("\n"));

            // Order Info from DB
            try (Connection con = DBConnection.getConnection()) {
                // Fetch Order & User Info
                String orderSql = "SELECT o.order_date, o.total_amount, u.username, u.email, u.mobile, u.address " +
                                 "FROM orders o JOIN users u ON o.user_id = u.id WHERE o.id = ?";
                PreparedStatement ps = con.prepareStatement(orderSql);
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    PdfPTable infoTable = new PdfPTable(2);
                    infoTable.setWidthPercentage(100);
                    infoTable.setSpacingBefore(10f);
                    infoTable.setSpacingAfter(10f);

                    // Left side: Billing Details
                    PdfPCell cell1 = new PdfPCell();
                    cell1.setBorder(Rectangle.NO_BORDER);
                    cell1.addElement(new Paragraph("Billed To:", headerFont));
                    cell1.addElement(new Paragraph(rs.getString("username"), normalFont));
                    cell1.addElement(new Paragraph("Email: " + rs.getString("email"), normalFont));
                    cell1.addElement(new Paragraph("Mobile: " + rs.getString("mobile"), normalFont));
                    cell1.addElement(new Paragraph("Address: " + rs.getString("address"), normalFont));
                    infoTable.addCell(cell1);

                    // Right side: Order Details
                    PdfPCell cell2 = new PdfPCell();
                    cell2.setBorder(Rectangle.NO_BORDER);
                    cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    Paragraph p2 = new Paragraph("Order Summary", headerFont);
                    p2.setAlignment(Element.ALIGN_RIGHT);
                    cell2.addElement(p2);
                    
                    Paragraph pOrder = new Paragraph("Order ID: #" + orderId, normalFont);
                    pOrder.setAlignment(Element.ALIGN_RIGHT);
                    cell2.addElement(pOrder);
                    
                    Paragraph pDate = new Paragraph("Date: " + rs.getTimestamp("order_date").toString(), normalFont);
                    pDate.setAlignment(Element.ALIGN_RIGHT);
                    cell2.addElement(pDate);
                    
                    infoTable.addCell(cell2);
                    document.add(infoTable);

                    document.add(new Paragraph("\n"));

                    // Product Table
                    PdfPTable table = new PdfPTable(5);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{3.5f, 1.5f, 1.5f, 1.5f, 2f});

                    String[] headers = {"Product Name", "Size", "Price (Each)", "Qty", "Total"};
                    for (String head : headers) {
                        PdfPCell headerCell = new PdfPCell(new Phrase(head, headerFont));
                        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        headerCell.setPadding(5);
                        table.addCell(headerCell);
                    }

                    // Fetch Order Items
                    String itemSql = "SELECT p.name, oi.price, oi.quantity, oi.size " +
                                    "FROM order_items oi JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?";
                    PreparedStatement ps2 = con.prepareStatement(itemSql);
                    ps2.setInt(1, orderId);
                    ResultSet rs2 = ps2.executeQuery();

                    double totalAmount = 0;
                    while (rs2.next()) {
                        String name = rs2.getString("name");
                        String size = rs2.getString("size");
                        double price = rs2.getDouble("price");
                        int qty = rs2.getInt("quantity");
                        double rowTotal = price * qty;
                        totalAmount += rowTotal;

                        table.addCell(new Phrase(name, normalFont));
                        table.addCell(new Phrase(size == null || size.isEmpty() ? "-" : size, normalFont));
                        table.addCell(new Phrase("INR " + df.format(price), normalFont));
                        table.addCell(new Phrase(String.valueOf(qty), normalFont));
                        table.addCell(new Phrase("INR " + df.format(rowTotal), normalFont));
                    }
                    document.add(table);

                    // Final Total
                    Paragraph footer = new Paragraph("\nGrand Total: INR " + df.format(totalAmount), titleFont);
                    footer.setAlignment(Element.ALIGN_RIGHT);
                    document.add(footer);

                    document.add(new Paragraph("\n\n"));
                    Paragraph thanks = new Paragraph("Thank you for shopping with AVVJ Cart!", normalFont);
                    thanks.setAlignment(Element.ALIGN_CENTER);
                    document.add(thanks);
                    
                    document.add(new Paragraph("This is a computer-generated invoice.", smallFont));
                }
            }

            document.close();
            JOptionPane.showMessageDialog(null, "Bill Downloaded Successfully!\nPath: " + path);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error Generating PDF: " + e.getMessage());
        }
    }
}
