package Commands.json;


import Commands.Command;

import javax.json.Json;
import javax.json.JsonObject;
import DataBase.JsonFileImplementation;

import java.sql.SQLException;

public class JsonSetCommand implements Command {

    private final JsonFileImplementation repo;

    public JsonSetCommand(JsonFileImplementation repo) {
        this.repo = repo;
    }

    @Override
    public JsonObject execute(JsonObject request) {
        try {
            String fileName = request.getString("file");
            String client   = request.getString("client");
            String path     = request.getString("path");
            Object value    = request.get("value");  // String, Number, Boolean, JsonObject, JSONArray

            // Validation path : pas de caractères dangereux
            if (!path.matches("[a-zA-Z0-9_\\-\\.]+")) {
                return error("path invalide : caractères autorisés : lettres, chiffres, -, _, .");
            }

            boolean updated = repo.set(fileName, client, path, value);
            if (updated) {
                return Json.createObjectBuilder()
                        .add("status", "OK")
                        .add("data", Json.createObjectBuilder()
                                .add("message", "SET appliqué sur '" + path + "' dans '" + fileName + "'."))
                        .build();
            } else {
                return error("Fichier introuvable ou vous n'êtes pas le propriétaire.");
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
