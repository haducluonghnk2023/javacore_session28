package Ex28.Ex06;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class Ex06 {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/testdb";
        String username = "root";
        String password = "new_password";

        String departmentName = "Phòng Kỹ thuật";
        List<String> employeeNames = Arrays.asList("Nguyễn Văn A", "Trần Thị B", "Lê Văn C");

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            conn.setAutoCommit(false);

            try {
                // 1. Thêm phòng ban
                String insertDeptSQL = "INSERT INTO departments (name) VALUES (?)";
                try (PreparedStatement deptStmt = conn.prepareStatement(insertDeptSQL, Statement.RETURN_GENERATED_KEYS)) {
                    deptStmt.setString(1, departmentName);
                    deptStmt.executeUpdate();

                    // Lấy ID phòng ban vừa thêm
                    ResultSet generatedKeys = deptStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int departmentId = generatedKeys.getInt(1);

                        // 2. Thêm các nhân viên
                        String insertEmpSQL = "INSERT INTO employees (name, department_id) VALUES (?, ?)";
                        try (PreparedStatement empStmt = conn.prepareStatement(insertEmpSQL)) {
                            for (String empName : employeeNames) {
                                empStmt.setString(1, empName);
                                empStmt.setInt(2, departmentId);
                                empStmt.executeUpdate();
                            }
                        }
                    } else {
                        throw new SQLException("Không thể lấy ID phòng ban.");
                    }
                }

                // Nếu không có lỗi thì commit
                conn.commit();
                System.out.println("Giao dịch thành công!");

            } catch (SQLException ex) {
                // Có lỗi → rollback
                conn.rollback();
                System.err.println("Giao dịch thất bại, đã rollback.");
                ex.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
