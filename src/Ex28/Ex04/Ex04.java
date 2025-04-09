package Ex28.Ex04;

import java.sql.*;

public class Ex04 {
    public static void main(String[] args) {
        String urlBankA = "jdbc:mysql://localhost:3306/bankA_db";
        String urlBankB = "jdbc:mysql://localhost:3306/bankB_db";
        String user = "root";
        String pass = "new_password";

        Connection connA = null;
        Connection connB = null;

        try {
            // Kết nối tới cả hai ngân hàng
            connA = DriverManager.getConnection(urlBankA, user, pass);
            connB = DriverManager.getConnection(urlBankB, user, pass);
            System.out.println("Đã kết nối đến cả hai ngân hàng!");

            // Tắt auto-commit cho cả hai
            connA.setAutoCommit(false);
            connB.setAutoCommit(false);
            System.out.println("Đã tắt auto-commit cho cả hai kết nối.");

            // Thông tin chuyển khoản
            int fromAccountId = 1; // từ ngân hàng A
            int toAccountId = 2;   // đến ngân hàng B
            double amount = 300.0;

            // 1. Kiểm tra số dư tài khoản ở ngân hàng A
            PreparedStatement checkStmt = connA.prepareStatement("SELECT balance FROM bank_accounts WHERE account_id = ?");
            checkStmt.setInt(1, fromAccountId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) throw new SQLException("Tài khoản gửi không tồn tại!");

            double balance = rs.getDouble("balance");
            if (balance < amount) throw new SQLException("Không đủ số dư để chuyển khoản!");

            // 2. Trừ tiền từ tài khoản ngân hàng A
            PreparedStatement deductStmt = connA.prepareStatement("UPDATE bank_accounts SET balance = balance - ? WHERE account_id = ?");
            deductStmt.setDouble(1, amount);
            deductStmt.setInt(2, fromAccountId);
            deductStmt.executeUpdate();
            System.out.println("Đã trừ " + amount + " từ tài khoản ngân hàng A.");

            // 3. Cộng tiền vào tài khoản ngân hàng B
            PreparedStatement addStmt = connB.prepareStatement("UPDATE bank_accounts SET balance = balance + ? WHERE account_id = ?");
            addStmt.setDouble(1, amount);
            addStmt.setInt(2, toAccountId);
            int rowsAffected = addStmt.executeUpdate();
            if (rowsAffected == 0) throw new SQLException("Tài khoản nhận không tồn tại!");
            System.out.println("Đã cộng " + amount + " vào tài khoản ngân hàng B.");

            // 4. Nếu mọi thứ ok → commit cả hai
            connA.commit();
            connB.commit();
            System.out.println("Giao dịch chuyển khoản hoàn tất và đã commit cả hai kết nối.");

        } catch (SQLException e) {
            System.out.println("Lỗi khi chuyển khoản: " + e.getMessage());
            try {
                if (connA != null) {
                    connA.rollback();
                    System.out.println("Đã rollback kết nối ngân hàng A.");
                }
                if (connB != null) {
                    connB.rollback();
                    System.out.println("Đã rollback kết nối ngân hàng B.");
                }
            } catch (SQLException ex) {
                System.out.println("Lỗi rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (connA != null) connA.close();
                if (connB != null) connB.close();
                System.out.println("Đã đóng kết nối đến hai ngân hàng.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
