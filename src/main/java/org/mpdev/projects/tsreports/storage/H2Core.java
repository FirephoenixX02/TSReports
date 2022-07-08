package org.mpdev.projects.tsreports.storage;

import com.zaxxer.hikari.HikariDataSource;
import org.mpdev.projects.tsreports.TSReports;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class H2Core implements DBCore {

    private final TSReports plugin = TSReports.getInstance();
    private final HikariDataSource dataSource = new HikariDataSource();

    public H2Core() {
        initialize();
    }

    private void initialize() {
        String pluginName = plugin.getDescription().getName();
        dataSource.setPoolName("[" + pluginName + "]" + " Hikari");
        plugin.getLogger().info("Loading storage provider: H2");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:./plugins/" + pluginName + "/" + pluginName);
    }

    @Override
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public ResultSet select(String query) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error: " + e.getMessage());
            plugin.getLogger().severe("Query: " + query);
        }
        return null;
    }

    @Override
    public void execute(String query) {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute(query);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error: " + e.getMessage());
            plugin.getLogger().severe("Query: " + query);
        }
    }

    @Override
    public void executeUpdateAsync(String query) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try (Connection connection = dataSource.getConnection()) {
                connection.createStatement().executeUpdate(query);
            } catch (SQLException e) {
                plugin.getLogger().severe("Error: " + e.getMessage());
                plugin.getLogger().severe("Query: " + query);
            }
        });
    }

    @Override
    public Boolean existsTable(String table) {
        try (Connection connection = dataSource.getConnection()) {
            ResultSet tables = connection.getMetaData().getTables(null, null, table, null);
            return tables.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to check if table " + table + " exists: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean existsColumn(String table, String column) {
        try (Connection connection = dataSource.getConnection()) {
            ResultSet col = connection.getMetaData().getColumns(null, null, table, column);
            return col.next();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to check if column " + column + " exists in table " + table + ": " + e.getMessage());
            return false;
        }
    }

}
