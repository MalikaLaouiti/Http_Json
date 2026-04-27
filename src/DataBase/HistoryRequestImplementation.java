package DataBase;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.sql.*;

public class HistoryRequestImplementation {

    public void record(String clientId, String service, String operation, String target, String status, String message) {
        String sql = """
                INSERT INTO request_history
                    (client_id, service, operation, target, status, message)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, clientId);
            ps.setString(2, service);
            ps.setString(3, operation);
            ps.setString(4, target);
            ps.setString(5, status);
            ps.setString(6, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            // L'historique ne doit jamais faire crasher une commande
            System.err.println("[HISTORY] Erreur enregistrement : " + e.getMessage());
        }
    }

//    public JsonArray getForClient(String clientId, int limit) throws SQLException {
//        String sql = """
//                SELECT id, client_id, service, operation, target,
//                       status, message, executed_at
//                FROM   request_history
//                WHERE  client_id = ?
//                ORDER  BY executed_at DESC
//                LIMIT  ?
//                """;
//
//        JsonArrayBuilder builder = Json.createArrayBuilder();
//        try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
//            ps.setString(1, clientId);
//            ps.setInt(2, limit);
//            ResultSet rs = ps.executeQuery();
//            while (rs.next()) {
//                builder.add(rowToJson(rs));
//            }
//        }
//        return builder.build();
//    }

    public JsonArray getAll(int limit) throws SQLException {
        String sql = """
                SELECT id, client_id, service, operation, target,
                       status, message, executed_at
                FROM   request_history
                ORDER  BY executed_at DESC
                LIMIT  ?
                """;

        JsonArrayBuilder builder = Json.createArrayBuilder();
        try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                builder.add(rowToJson(rs));
            }
        }
        return builder.build();
    }

    private JsonObject rowToJson(ResultSet rs) throws SQLException {
        String msg = rs.getString("message");
        return Json.createObjectBuilder()
                .add("id",          rs.getInt("id"))
                .add("client_id",   rs.getString("client_id"))
                .add("service",     rs.getString("service"))
                .add("operation",   rs.getString("operation"))
                .add("target",      rs.getString("target"))
                .add("status",      rs.getString("status"))
                .add("message",     msg != null ? msg : "")
                .add("executed_at", rs.getTimestamp("executed_at").toString())
                .build();
    }
}
