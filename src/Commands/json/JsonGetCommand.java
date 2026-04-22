package Commands.json;
import Commands.Command;

import javax.json.Json;
import javax.json.JsonObject;
import DataBase.JsonFileImplementation;

import java.sql.SQLException;
public class JsonGetCommand implements Command {

    private final JsonFileImplementation repo;

    public JsonGetCommand(JsonFileImplementation repo) {
        this.repo = repo;
    }

    @Override
    public JsonObject execute(JsonObject request) {
        try {
            String fileName = request.getString("file");
            String client   = request.getString("client");

            JsonObject result = repo.get(fileName, client);

            if (result != null) {
                return Json.createObjectBuilder().add("status", "OK").add("data", result).build();
            } else {
                return Json.createObjectBuilder()
                        .add("status", "ERROR")
                        .add("message", "Fichier introuvable ou accès refusé.").build();
            }

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