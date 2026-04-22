package DataBase;

import javax.json.*;

import java.io.StringReader;
import java.sql.*;
public class JsonFileImplementation  implements JsonFileDAO{
    /**
     * Toutes les opérations CRUD sur la table json_files.
     *
     * DDL à exécuter une seule fois dans PostgreSQL :
     *
     *   CREATE TABLE IF NOT EXISTS json_files (
     *       id          SERIAL PRIMARY KEY,
     *       file_name   VARCHAR(255) NOT NULL,
     *       owner       VARCHAR(255) NOT NULL,
     *       content     JSONB        NOT NULL DEFAULT '{}',
     *       visibility  VARCHAR(10)  NOT NULL DEFAULT 'private',
     *       created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
     *       updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
     *   );
     *   CREATE UNIQUE INDEX IF NOT EXISTS idx_file_owner ON json_files(file_name, owner);
     */



        public boolean create(String fileName, String owner, JsonObject content, String visibility)
                throws SQLException {

            String sql = """
                    INSERT INTO json_files (file_name, owner, content, visibility)
                    VALUES (?, ?, ?::jsonb, ?)
                    ON CONFLICT (file_name, owner) DO NOTHING
                    """;

            try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
                ps.setString(1, fileName);
                ps.setString(2, owner);
                ps.setString(3, content.toString());
                ps.setString(4, visibility);
                return ps.executeUpdate() > 0;
            }
        }

        public JsonObject get(String fileName, String requestingClient) throws SQLException {

            String sql = """
                    SELECT id, file_name, owner, content, visibility, created_at, updated_at
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
                return null; // not found / not authorized
            }
        }

        // ------------------------------------------------------------------ GET ALL

        /**
         * Retourne tous les fichiers accessibles par requestingClient :
         * ses propres fichiers + tous les fichiers publics.
         */
        public JsonArray getAll(String requestingClient) throws SQLException {

            String sql = """
                    SELECT id, file_name, owner, content, visibility, created_at, updated_at
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
                    SET    content    = jsonb_set(content, ?::text[], ?::jsonb, true),
                           updated_at = NOW()
                    WHERE  file_name  = ?
                      AND  owner      = ?
                    """;

            try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
                ps.setString(1, pgPath);
                ps.setString(2, jsonValue);
                ps.setString(3, fileName);
                ps.setString(4, owner);
                return ps.executeUpdate() > 0;
            }
        }

        public boolean delete(String fileName, String owner) throws SQLException {

            String sql = "DELETE FROM json_files WHERE file_name = ? AND owner = ?";

            try (PreparedStatement ps = DataBaseConnection.getInstance().prepareStatement(sql)) {
                ps.setString(1, fileName);
                ps.setString(2, owner);
                return ps.executeUpdate() > 0;
            }
        }

        private JsonObject rowToJson(ResultSet rs) throws SQLException {
            return Json.createObjectBuilder()
                    .add("id", rs.getInt("id"))
                    .add("file_name", rs.getString("file_name"))
                    .add("owner", rs.getString("owner"))
                    .add("content", parseJsonContent(rs.getString("content")))
                    .add("visibility", rs.getString("visibility"))
                    .add("created_at", rs.getTimestamp("created_at").toString())
                    .add("updated_at", rs.getTimestamp("updated_at").toString())
                    .build();
        }

        private JsonValue parseJsonContent(String content) {
            if (content == null || content.isEmpty()) {
                return JsonValue.NULL;
            }
            try {
                try (JsonReader reader = Json.createReader(new StringReader(content))) {
                    return reader.readValue();
                }
            } catch (Exception e) {
                // Si le contenu n'est pas un JSON valide, le traiter comme string
                return Json.createObjectBuilder().build();
            }
        }

        private String escapeJson(String s) {
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }

    }