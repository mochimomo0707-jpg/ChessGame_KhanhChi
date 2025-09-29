/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author Admin
 */
public class DAO {

    protected Connection con;

    public DAO() {
        final String DATABASE_NAME = "DATN"; // Tên Database
        final String jdbcURL = "jdbc:sqlserver://localhost:1433;databaseName=" + DATABASE_NAME + ";encrypt=false;";
        final String JDBC_USER = "sa";  // Tên user SQL Server
        final String JDBC_PASSWORD = "1234"; // Mật khẩu user SQL Server

        try {
            // Load JDBC Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Kết nối đến SQL Server
            con = DriverManager.getConnection(jdbcURL, JDBC_USER, JDBC_PASSWORD);
            System.out.println("Kết nối database thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection to database failed");
            System.out.println("Full URL: " + jdbcURL);
            System.out.println("Trying to connect as: " + JDBC_USER);

        }
    }
}
