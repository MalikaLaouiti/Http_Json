package Commands.json;
import Commands.Command;

import javax.json.Json;
import javax.json.JsonObject;

import DataBase.JsonFileImplementation;

import java.sql.SQLException;

public class JsonCreateCommand implements Command {

    private final JsonFileImplementation repo;

    public JsonCreateCommand(JsonFileImplementation repo) {
        this.repo = repo;
    }

    @Override
    public JsonObject execute(JsonObject request) {
        try {
            String fileName   = request.getString("file");
            String client     = request.getString("client");
            JsonObject content = request.getJsonObject("content");
            if (content == null) content = Json.createObjectBuilder().build();
            String visibility = JsonUtils.optString(request,"visibility", "private");

            // Validation visibilité
            if (!visibility.equals("public") && !visibility.equals("private")) {
                return error("visibility doit être 'public' ou 'private'");
            }

            boolean created = repo.create(fileName, client, content, visibility);
            if (created) {
                return ok(Json.createObjectBuilder().add("message", "Fichier '" + fileName + "' créé.").build());
            } else {
                return error("Un fichier '" + fileName + "' existe déjà pour ce client.");
            }

        } catch (IllegalArgumentException e) {
            return error("Champ manquant : " + e.getMessage());
        } catch (SQLException e) {
            return error("Erreur base de données : " + e.getMessage());
        }
    }

    private JsonObject ok(JsonObject data) {
        return Json.createObjectBuilder().add("status", "OK").add("data", data).build();
    }

    private JsonObject error(String message) {
        return Json.createObjectBuilder().add("status", "ERROR").add("message", message).build();
    }
}
