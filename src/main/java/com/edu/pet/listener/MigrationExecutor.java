package com.edu.pet.listener;

import com.edu.pet.util.ConnectionPool;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.flywaydb.core.Flyway;

import java.lang.reflect.Field;

@WebListener
public class MigrationExecutor implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Field dataSourceField = ConnectionPool.class.getDeclaredField("DATA_SOURCE");
            dataSourceField.setAccessible(true);
            HikariDataSource dataSource = (HikariDataSource) dataSourceField.get(null);
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .baselineOnMigrate(true)
                    .load();
            flyway.migrate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
