package Commands.json;

import Commands.Command;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import DataBase.JsonFileImplementation;
import java.sql.SQLException;

public class JsonGetAllCommand implements Command {

    private final JsonFileImplementation repo;

    public JsonGetAllCommand(JsonFileImplementation repo) {
        this.repo = repo;
    }

    @Override
    public JsonObject execute(JsonObject request) {
        try {
            String client = request.getString("client");
            JsonArray files = repo.getAll(client);
            return Json.createObjectBuilder()
                    .add("status", "OK")
                    .add("data", Json.createObjectBuilder()
                            .add("count", files.size())
                            .add("files", files))
                    .build();

        } catch (IllegalArgumentException e) {
            return error("Champ manquant : " + e.getMessage());
        } catch (SQLException e) {
            return error("Erreur base de données : " + e.getMessage());
        }
    }

    private JsonObject error(String msg) {
        return Json.createObjectBuilder().add("status", "ERROR").add("message", msg).build();
    }
}
