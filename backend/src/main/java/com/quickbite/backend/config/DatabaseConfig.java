package com.quickbite.backend.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        String databaseUrl = System.getenv("DATABASE_URL");

        // If DATABASE_URL is not set, return null and let Spring use application properties
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        // Render provides postgres:// URLs — convert to jdbc:postgresql://
        URI dbUri = new URI(databaseUrl);
        String userInfo = dbUri.getUserInfo();
        String username = userInfo.split(":")[0];
        String password = userInfo.split(":")[1];
        String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
                + "?sslmode=require";

        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
