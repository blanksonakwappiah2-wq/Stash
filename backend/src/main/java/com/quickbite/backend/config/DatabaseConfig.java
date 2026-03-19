package com.quickbite.backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Value("${DB_HOST:#{null}}")
    private String dbHost;

    @Value("${DB_PORT:5432}")
    private String dbPort;

    @Value("${DB_NAME:#{null}}")
    private String dbName;

    @Value("${SPRING_DATASOURCE_USERNAME:#{null}}")
    private String username;

    @Value("${SPRING_DATASOURCE_PASSWORD:#{null}}")
    private String password;

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Use DATABASE_URL if provided (Render's default when DB is attached manually)
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            if (databaseUrl.startsWith("postgres://")) {
                String cleanUrl = databaseUrl.substring(11);
                String[] userPassAndHostPortDb = cleanUrl.split("@");
                String[] userPass = userPassAndHostPortDb[0].split(":");
                String hostPortDb = userPassAndHostPortDb[1];

                config.setJdbcUrl("jdbc:postgresql://" + hostPortDb);
                config.setUsername(userPass[0]);
                if (userPass.length > 1) {
                    config.setPassword(userPass[1]);
                }
            } else {
                config.setJdbcUrl(databaseUrl);
            }
        } 
        // Fallback to separate component variables
        else if (dbHost != null && dbName != null) {
            config.setJdbcUrl("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName);
            if (username != null) config.setUsername(username);
            if (password != null) config.setPassword(password);
        } else {
            throw new RuntimeException("No database configuration provided. Set DATABASE_URL or DB_HOST, DB_PORT, DB_NAME");
        }
        
        config.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(config);
    }
}
