package Ex28.Ex01;

import java.sql.*;

public class Ex01 {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/testdb";
        String username = "root";
        String password = "new_password";

        Connection conn = null;

        try {
            // 1. Kết nối đến cơ sở dữ liệu
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("Kết nối thành công!");

            // 2. Kiểm tra trạng thái auto-commit ban đầu
            boolean autoCommit = conn.getAutoCommit();
            System.out.println("Trạng thái auto-commit ban đầu: " + autoCommit);

            // 3. Tắt chế độ auto-commit
            conn.setAutoCommit(false);
            System.out.println("Auto-commit đã bị tắt.");

            // 4. Thực hiện câu lệnh INSERT
            String insertSQL = "INSERT INTO users (name, email) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
            pstmt.setString(1, "Nguyen Van A");
            pstmt.setString(2, "nguyenvana@example.com");

            int rowsInserted = pstmt.executeUpdate();
            System.out.println("Số dòng được chèn: " + rowsInserted);

            // 5. Gọi commit() để lưu thay đổi
            conn.commit();
            System.out.println("Đã commit thay đổi.");

            // 6. Xác minh dữ liệu đã được thêm
            String selectSQL = "SELECT * FROM users WHERE email = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
            selectStmt.setString(1, "nguyenvana@example.com");

            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                System.out.println("Dữ liệu đã được thêm:");
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Email: " + rs.getString("email"));
            } else {
                System.out.println("Không tìm thấy dữ liệu vừa thêm.");
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Có lỗi xảy ra, rollback lại.");
                }
            } catch (SQLException ex) {
                System.out.println("Lỗi khi rollback: " + ex.getMessage());
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
                System.out.println("Đã đóng kết nối.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
