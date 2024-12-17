package com.almagest_dev.tacobank_core_server.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseConnectionChecker implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null) {
                log.info("Database connected successfully!");
            } else {
                log.error("Failed to connect to the database.");
                System.out.println();
            }
        } catch (SQLException e) {
            log.error("SQL Exception: " + e.getMessage());
        }
    }
}
