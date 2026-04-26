package DataBase;

import javax.json.*;
import java.io.IOException;
import java.sql.*;

public class JsonFileImplementation  implements JsonFileDAO{
    private final FileStoreManager fileStore;

    public JsonFileImplementation(FileStoreManager fileStore) {
        this.fileStore = fileStore;
    }

    public boolean create(String fileName, String owner, JsonObject content, String visibility) throws SQLException, IOException {
        String filePath = fileStore.write(owner, fileName, content);

        String sql = """
                INSERT INTO json_files (file_name, owner, content, visibility,file_path)
                VALUES (?, ?, ?::jsonb, ?, ?)
                ON CONFLICT (file_name, owner) DO NOTHING
                """;

        try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, fileName);
            ps.setString(2, owner);
            ps.setString(3, content.toString());
            ps.setString(4, visibility);
            ps.setString(5, filePath);
            boolean inserted = ps.executeUpdate() > 0;
            if (!inserted) {
                fileStore.delete(owner, fileName);
            }
            return inserted;
        }
    }

    public JsonObject get(String fileName, String requestingClient) throws SQLException {

        String sql = """
                SELECT id, file_name, owner, content, visibility, file_path, created_at, updated_at
                FROM   json_files
                WHERE  file_name = ?
                  AND  (owner = ? OR visibility = 'public')
                """;

        try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, fileName);
            ps.setString(2, requestingClient);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rowToJson(rs);
            }
            return null;
        }
    }


    public JsonArray getAll(String requestingClient) throws SQLException {

        String sql = """
                SELECT id, file_name, owner, content, visibility, file_path, created_at, updated_at
                FROM   json_files
                WHERE  owner = ? OR visibility = 'public'
                ORDER  BY owner, file_name
                """;
        JsonArrayBuilder resultBuilder = Json.createArrayBuilder();

        try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, requestingClient);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                resultBuilder.add(rowToJson(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Ou logger l'erreur
        }

        return resultBuilder.build();  // Retourne JsonArray (vide si pas de données)
    }


    public boolean set(String fileName, String owner, String path, Object value) throws SQLException {

        // Convertir "car-specs.brand" → '{car-specs,brand}'
        String pgPath = "{" + path.replace(".", ",") + "}";

        // Sérialiser la valeur en JSON valide
        String jsonValue;
        if (value instanceof String) {
            // Méthode plus fiable sans substring hasardeux
            jsonValue = "\"" + escapeJson((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            jsonValue = value.toString();
        } else if (value instanceof JsonObject || value instanceof JsonArray) {
            jsonValue = value.toString();
        } else {
            jsonValue = "\"" + escapeJson(value.toString()) + "\"";
        }

        String sql = """
                UPDATE json_files
                SET    content    = jsonb_set(content, ?::text[], to_jsonb(?::text), true),
                       updated_at = NOW()
                WHERE  file_name  = ?
                  AND  owner      = ?
                RETURNING content
                """;

        try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, pgPath);
            ps.setString(2, jsonValue);
            ps.setString(3, fileName);
            ps.setString(4, owner);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // 2. Synchronise le fichier sur disque avec le contenu PostgreSQL
                String updatedContent = rs.getString("content");
                try (JsonReader reader = Json.createReader(
                        new java.io.StringReader(updatedContent))) {
                    fileStore.syncFromDb(owner, fileName, reader.readObject());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            return false;
        }
    }

    public boolean delete(String fileName, String owner) throws SQLException, IOException {

        String sql = "DELETE FROM json_files WHERE file_name = ? AND owner = ?";

        try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, fileName);
            ps.setString(2, owner);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) fileStore.delete(owner, fileName);
            return deleted;
        }
    }

    private JsonObject rowToJson(ResultSet rs) throws SQLException {
        String fp = rs.getString("file_path");
        return Json.createObjectBuilder()
                .add("id", rs.getInt("id"))
                .add("file_name", rs.getString("file_name"))
                .add("owner", rs.getString("owner"))
                .add("content",  Json.createReader(new java.io.StringReader(rs.getString("content"))).readObject())
                .add("visibility", rs.getString("visibility"))
                .add("file_path",   fp != null ? fp : "")
                .add("created_at", rs.getTimestamp("created_at").toString())
                .add("updated_at", rs.getTimestamp("updated_at").toString())
                .build();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}