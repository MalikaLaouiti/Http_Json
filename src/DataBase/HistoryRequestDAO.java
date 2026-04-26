package DataBase;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface HistoryRequestDAO {
    public void record(String clientId, String service, String operation, String target, String status, String message);
    public JsonArray getAll(int limit) throws SQLException;
    public JsonArray getForClient(String clientId, int limit) throws SQLException;
}
