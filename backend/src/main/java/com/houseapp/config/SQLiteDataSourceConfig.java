package com.houseapp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

@Configuration
public class SQLiteDataSourceConfig {
  private static final int BUSY_TIMEOUT_MS = 5000;

  @Bean
  DataSource dataSource(@Value("${spring.datasource.url}") String jdbcUrl) {
    SQLiteConfig sqliteConfig = new SQLiteConfig();
    sqliteConfig.setBusyTimeout(BUSY_TIMEOUT_MS);
    sqliteConfig.setJournalMode(SQLiteConfig.JournalMode.WAL);
    sqliteConfig.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
    sqliteConfig.enforceForeignKeys(true);

    SQLiteDataSource sqliteDataSource = new SQLiteDataSource(sqliteConfig);
    sqliteDataSource.setUrl(jdbcUrl);

    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDataSource(sqliteDataSource);
    hikariConfig.setMaximumPoolSize(1);
    hikariConfig.setMinimumIdle(1);
    hikariConfig.setConnectionTimeout(10_000);
    hikariConfig.setPoolName("HouseAppSQLitePool");
    return new HikariDataSource(hikariConfig);
  }
}
