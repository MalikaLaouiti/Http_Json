package DataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthAdmin {
    public boolean login(String admin, String pass) throws SQLException {

        String sql = """
                SELECT username,password
                FROM   admin
                WHERE  username = ?
                  AND  password = ?
                """;

        try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, admin);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
}
