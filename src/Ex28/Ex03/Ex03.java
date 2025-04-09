package Ex28.Ex03;

import java.sql.*;

public class Ex03 {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/testdb";
        String username = "root";
        String password = "new_password";

        Connection conn = null;

        try {
            // 1. Kết nối đến cơ sở dữ liệu
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("Đã kết nối đến cơ sở dữ liệu!");

            // 2. Tắt auto-commit
            conn.setAutoCommit(false);
            System.out.println("Auto-commit đã bị tắt.");

            int fromId = 1; // tài khoản chuyển
            int toId = 2;   // tài khoản nhận
            double amount = 200.0; // số tiền chuyển

            // 3. Kiểm tra số dư tài khoản gửi
            String checkSQL = "SELECT balance FROM accounts WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
            checkStmt.setInt(1, fromId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Tài khoản gửi không tồn tại!");
            }

            double currentBalance = rs.getDouble("balance");
            if (currentBalance < amount) {
                throw new SQLException("Không đủ số dư để chuyển tiền!");
            }

            // 4. Trừ tiền từ tài khoản A
            String deductSQL = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            PreparedStatement deductStmt = conn.prepareStatement(deductSQL);
            deductStmt.setDouble(1, amount);
            deductStmt.setInt(2, fromId);
            deductStmt.executeUpdate();
            System.out.println("Đã trừ " + amount + " từ tài khoản ID " + fromId);

            // 5. Cộng tiền vào tài khoản B
            String addSQL = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            PreparedStatement addStmt = conn.prepareStatement(addSQL);
            addStmt.setDouble(1, amount);
            addStmt.setInt(2, toId);
            addStmt.executeUpdate();
            System.out.println("Đã cộng " + amount + " vào tài khoản ID " + toId);

            // 6. Commit nếu mọi thứ thành công
            conn.commit();
            System.out.println("Giao dịch chuyển tiền hoàn tất!");

        } catch (SQLException e) {
            System.out.println("Lỗi khi chuyển tiền: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Đã rollback giao dịch.");
                }
            } catch (SQLException ex) {
                System.out.println("Lỗi khi rollback: " + ex.getMessage());
            }
        } finally {
            // 7. Kiểm tra lại số dư của cả 2 tài khoản
            try {
                if (conn != null) {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM accounts WHERE id IN (1, 2)");
                    System.out.println("Trạng thái tài khoản sau giao dịch:");
                    while (rs.next()) {
                        System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Balance: " + rs.getDouble("balance"));
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
