package DataBase;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface JsonFileDAO {
    public boolean create(String fileName, String owner, JsonObject content, String visibility) throws SQLException;
    public JsonObject get(String fileName, String requestingClient) throws SQLException;
    public JsonArray getAll(String requestingClient) throws SQLException;
    public boolean set(String fileName, String owner, String path, Object value) throws SQLException;
    public boolean delete(String fileName, String owner) throws SQLException;
}
