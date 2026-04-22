package Commands.json;
import Commands.Command;
import javax.json.JsonObject;
import repository.JsonFileRepository;

import java.sql.SQLException;

public class JsonCreateCommand implements Command {
    /**
 * Protocole attendu :
 * { "service":"JSON", "op":"CREATE", "file":"file1",
 *   "content":{...}, "visibility":"private", "client":"alice" }
 */


    private final JsonFileRepository repo;

    public JsonCreateCommand(JsonFileRepository repo) {
        this.repo = repo;
    }

    @Override
    public JSONObject execute(JSONObject request) {
        try {
            String fileName   = request.getString("file");
            String client     = request.getString("client");
            JSONObject content = request.optJSONObject("content");
            if (content == null) content = new JSONObject();
            String visibility = request.optString("visibility", "private");

            // Validation visibilité
            if (!visibility.equals("public") && !visibility.equals("private")) {
                return error("visibility doit être 'public' ou 'private'");
            }

            boolean created = repo.create(fileName, client, content, visibility);
            if (created) {
                return ok(new JSONObject().put("message", "Fichier '" + fileName + "' créé."));
            } else {
                return error("Un fichier '" + fileName + "' existe déjà pour ce client.");
            }

        } catch (IllegalArgumentException e) {
            return error("Champ manquant : " + e.getMessage());
        } catch (SQLException e) {
            return error("Erreur base de données : " + e.getMessage());
        }
    }

    private JSONObject ok(JSONObject data) {
        return new JSONObject().put("status", "OK").put("data", data);
    }

    private JSONObject error(String message) {
        return new JSONObject().put("status", "ERROR").put("message", message);
    }
}
