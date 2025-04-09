package Ex28.Ex05;

import java.sql.*;
import java.time.LocalDate;

public class Ex05 {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/testdb";
        String username = "root";
        String password = "new_password";

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("Đã kết nối đến CSDL!");

            // Tắt auto-commit để bắt đầu transaction
            conn.setAutoCommit(false);

            // Thêm đơn hàng mới
            String insertOrderSQL = "INSERT INTO Orders (order_id, customer_name, order_date) VALUES (?, ?, ?)";
            PreparedStatement orderStmt = conn.prepareStatement(insertOrderSQL);
            int newOrderId = 101; // bạn có thể sinh tự động
            orderStmt.setInt(1, newOrderId);
            orderStmt.setString(2, "Nguyen Van A");
            orderStmt.setDate(3, Date.valueOf(LocalDate.now()));
            orderStmt.executeUpdate();
            System.out.println("Đã thêm đơn hàng thành công!");

            // Thêm các chi tiết đơn hàng (nhiều dòng)
            String insertDetailSQL = "INSERT INTO OrderDetails (detail_id, order_id, product_name, quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement detailStmt = conn.prepareStatement(insertDetailSQL);

            // Dữ liệu giả lập
            int[][] details = {
                    {201, newOrderId, 1},
                    {202, newOrderId, 2},
                    {203, newOrderId, 5}
            };
            String[] productNames = {"Sản phẩm A", "Sản phẩm B", "Sản phẩm C"};

            for (int i = 0; i < details.length; i++) {
                detailStmt.setInt(1, details[i][0]); // detail_id
                detailStmt.setInt(2, details[i][1]); // order_id
                detailStmt.setString(3, productNames[i]);
                detailStmt.setInt(4, details[i][2]); // quantity
                detailStmt.executeUpdate();
                System.out.println("Đã thêm chi tiết đơn hàng: " + productNames[i]);
            }

            // Nếu không lỗi, commit
            conn.commit();
            System.out.println("Giao dịch hoàn tất. Đã commit tất cả.");

        } catch (SQLException e) {
            System.out.println("Lỗi trong giao dịch: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Đã rollback toàn bộ giao dịch.");
                }
            } catch (SQLException ex) {
                System.out.println("Lỗi khi rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    System.out.println("Đã đóng kết nối.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
