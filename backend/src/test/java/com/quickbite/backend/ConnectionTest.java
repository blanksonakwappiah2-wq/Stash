package com.quickbite.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionTest {
    @Test
    public void testTiDBConnection() {
        String url = "jdbc:mysql://gateway01.eu-central-1.prod.aws.tidbcloud.com:4000/quickbite?sslMode=REQUIRED";
        String user = "\"rR2wkzxgmL37rcR.root\"";
        String password = "MT4AncuP96Kp0QAf";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("SUCCESS: Connected to 'quickbite' using quoted username!");
        } catch (SQLException e) {
            System.err.println("FAILURE: Could not connect with quoted username.");
            e.printStackTrace();
        }
    }
}
