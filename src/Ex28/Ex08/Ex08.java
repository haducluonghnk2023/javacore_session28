package Ex28.Ex08;

import java.sql.*;
import java.time.LocalDate;

public class Ex08 {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/testdb";
        String username = "root";
        String password = "new_password";

        int customerId = 1;
        int roomId = 101;

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            conn.setAutoCommit(false); // tắt auto-commit

            try {
                // 1. Kiểm tra khách hàng có tồn tại không
                String checkCustomerSQL = "SELECT * FROM customers WHERE customer_id = ?";
                try (PreparedStatement checkCustomerStmt = conn.prepareStatement(checkCustomerSQL)) {
                    checkCustomerStmt.setInt(1, customerId);
                    ResultSet rs = checkCustomerStmt.executeQuery();

                    if (!rs.next()) {
                        logFailedBooking(conn, customerId, roomId, "Khách hàng không tồn tại");
                        throw new SQLException("Khách hàng không tồn tại");
                    }
                }

                // 2. Kiểm tra phòng còn trống không (FOR UPDATE để tránh đồng thời)
                String checkRoomSQL = "SELECT * FROM rooms WHERE room_id = ? FOR UPDATE";
                boolean isAvailable = false;
                try (PreparedStatement checkRoomStmt = conn.prepareStatement(checkRoomSQL)) {
                    checkRoomStmt.setInt(1, roomId);
                    ResultSet rs = checkRoomStmt.executeQuery();
                    if (rs.next()) {
                        isAvailable = rs.getBoolean("availability");
                    } else {
                        logFailedBooking(conn, customerId, roomId, "Phòng không tồn tại");
                        throw new SQLException("Phòng không tồn tại");
                    }
                }

                if (!isAvailable) {
                    logFailedBooking(conn, customerId, roomId, "Phòng đã được đặt");
                    throw new SQLException("Phòng đã được đặt");
                }

                // 3. Cập nhật trạng thái phòng
                String updateRoomSQL = "UPDATE rooms SET availability = false WHERE room_id = ?";
                try (PreparedStatement updateRoomStmt = conn.prepareStatement(updateRoomSQL)) {
                    updateRoomStmt.setInt(1, roomId);
                    updateRoomStmt.executeUpdate();
                }

                // 4. Thêm bản ghi vào bookings
                String insertBookingSQL = "INSERT INTO bookings (customer_id, room_id, booking_date, status) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertBookingSQL)) {
                    insertStmt.setInt(1, customerId);
                    insertStmt.setInt(2, roomId);
                    insertStmt.setDate(3, Date.valueOf(LocalDate.now()));
                    insertStmt.setString(4, "CONFIRMED");
                    insertStmt.executeUpdate();
                }

                conn.commit();
                System.out.println("\u001B[32mĐặt phòng thành công!\u001B[0m");

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("\u001B[31mGiao dịch thất bại, đã rollback.\u001B[0m");
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void logFailedBooking(Connection conn, int customerId, int roomId, String errorMessage) {
        String logSQL = "INSERT INTO failed_bookings (customer_id, room_id, error_message) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(logSQL)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, roomId);
            stmt.setString(3, errorMessage);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Không thể ghi log thất bại: " + e.getMessage());
        }
    }
}
