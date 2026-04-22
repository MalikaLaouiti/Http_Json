package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC — une seule connexion partagée.
 * Remplace les 3 constantes selon ton environnement PostgreSQL.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:postgresql://localhost:5432/json_server_db";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "your_password";

    private static Connection instance = null;

    private DatabaseConnection() {}

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver PostgreSQL introuvable. Ajoute postgresql-xx.jar au classpath.", e);
            }
            instance = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connexion PostgreSQL établie.");
        }
        return instance;
    }
}