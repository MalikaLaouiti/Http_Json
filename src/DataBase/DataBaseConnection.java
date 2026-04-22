package DataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DataBaseConnection {
    private static Connection instance = null;

    private void DatabaseConnection() {}

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver PostgreSQL introuvable. Ajoute postgresql-xx.jar au classpath.", e);
            }
            instance = DriverManager.getConnection(DataBaseConfig.URL_DB, DataBaseConfig.USERNAME, DataBaseConfig.PASSWORD);
            System.out.println("[DB] Connexion PostgreSQL établie.");
        }
        return instance;
    }

}
//j'ai utilisé design pattern :singleton