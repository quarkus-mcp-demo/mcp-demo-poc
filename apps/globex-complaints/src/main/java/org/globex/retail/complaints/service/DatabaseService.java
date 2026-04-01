package org.globex.retail.complaints.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.ConnectionFunction;
import jakarta.persistence.EntityManager;
import org.globex.retail.complaints.persistence.Complaint;

import java.sql.*;
import java.util.*;

@ApplicationScoped
public class DatabaseService {

    public Map<String, String> databaseInfo() {
        EntityManager em = Complaint.getEntityManager();
        return em.callWithConnection((ConnectionFunction<Connection, Map<String, String>>) connection -> {
            DatabaseMetaData metaData = connection.getMetaData();
            Map<String, String> info = new HashMap<>();
            info.put("database_product_name", metaData.getDatabaseProductName());
            info.put("database_product_version", metaData.getDatabaseProductVersion());
            info.put("driver_name", metaData.getDriverName());
            info.put("driver_version", metaData.getDriverVersion());
            info.put("max_connections", String.valueOf(metaData.getMaxConnections()));
            info.put("read_only", String.valueOf(metaData.isReadOnly()));
            info.put("supports_transactions", String.valueOf(metaData.supportsTransactions()));
            info.put("sql_keywords", metaData.getSQLKeywords());
            return info;
        });
    }

    public List<Map<String, String>> listTables() {
        EntityManager em = Complaint.getEntityManager();
        return em.callWithConnection((ConnectionFunction<Connection, List<Map<String, String>>>) connection -> {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "%", new String[] { "TABLE" });

            List<Map<String, String>> tables = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> table = new HashMap<>();
                table.put("TABLE_CAT", rs.getString("TABLE_CAT"));
                table.put("TABLE_SCHEM", rs.getString("TABLE_SCHEM"));
                table.put("TABLE_NAME", rs.getString("TABLE_NAME"));
                table.put("REMARKS", rs.getString("REMARKS"));
                tables.add(table);
            }
            return tables;
        });
    }

    public List<Map<String, String>> describeTable(String table) {
        EntityManager em = Complaint.getEntityManager();
        return em.callWithConnection((ConnectionFunction<Connection, List<Map<String, String>>>) connection -> {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getColumns(null, null, table, null);
            List<Map<String, String>> columns = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> column = new HashMap<>();
                column.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                column.put("TYPE_NAME", rs.getString("TYPE_NAME"));
                column.put("COLUMN_SIZE", rs.getString("COLUMN_SIZE"));
                column.put("NULLABLE", rs.getString("IS_NULLABLE"));
                column.put("REMARKS", rs.getString("REMARKS"));
                column.put("COLUMN_DEF", rs.getString("COLUMN_DEF"));
                columns.add(column);
            }
            return columns;
        });
    }

    public List<Map<String, String>> queryDatabase(String query) {
        EntityManager em = Complaint.getEntityManager();
        return em.callWithConnection( (ConnectionFunction<Connection, List<Map<String, String>>>)connection -> {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();
            List<Map<String, String>> results = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    if (value != null) {
                        row.put(columnName, value.toString());
                    } else {
                        row.put(columnName, null);
                    }
                }
                results.add(row);
            }
            return results;
        });
    }
}
