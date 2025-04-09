package Ex28.Ex02;

import java.sql.*;

public class Ex02 {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/testdb";
        String username = "root";
        String password = "new_password";

        Connection conn = null;

        try {
            // 1. Kết nối đến cơ sở dữ liệu
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("Đã kết nối đến cơ sở dữ liệu!");

            // 2. Tắt chế độ auto-commit
            conn.setAutoCommit(false);
            System.out.println("Auto-commit đã bị tắt.");

            // 3. Câu lệnh INSERT hợp lệ
            String insertSQL1 = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";
            PreparedStatement pstmt1 = conn.prepareStatement(insertSQL1);
            pstmt1.setInt(1, 100); // giả sử 100 chưa tồn tại
            pstmt1.setString(2, "Le Van B");
            pstmt1.setString(3, "levanb@example.com");
            pstmt1.executeUpdate();
            System.out.println("Chèn dữ liệu hợp lệ thành công.");

            // 4. Câu lệnh INSERT gây lỗi (ID = 100 bị trùng)
            String insertSQL2 = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";
            PreparedStatement pstmt2 = conn.prepareStatement(insertSQL2);
            pstmt2.setInt(1, 100); // ID trùng → gây lỗi
            pstmt2.setString(2, "Pham Thi C");
            pstmt2.setString(3, "phamthic@example.com");
            pstmt2.executeUpdate(); // lỗi xảy ra tại đây

            // 5. Nếu không lỗi, commit
            conn.commit();

        } catch (SQLException e) {
            System.out.println("Lỗi xảy ra khi thực hiện INSERT: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Đã rollback giao dịch.");
                }
            } catch (SQLException ex) {
                System.out.println("Lỗi khi rollback: " + ex.getMessage());
            }
        } finally {
            // 6. Kiểm tra lại dữ liệu
            try {
                if (conn != null) {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE id = 100");

                    if (rs.next()) {
                        System.out.println("Dữ liệu vẫn tồn tại sau rollback:");
                        System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name"));
                    } else {
                        System.out.println("Không có dữ liệu nào được thêm sau rollback.");
                    }

                    conn.close();
                    System.out.println("Kết thúc chương trình.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
