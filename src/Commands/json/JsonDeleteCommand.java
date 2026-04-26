package Commands.json;


import Commands.Command;

import javax.json.Json;
import javax.json.JsonObject;
import DataBase.JsonFileImplementation;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Protocole attendu :
 * { "service":"JSON", "op":"DELETE", "file":"file1", "client":"alice" }
 *
 * Seul le propriétaire peut supprimer son fichier.
 */
public class JsonDeleteCommand implements Command {

    private final JsonFileImplementation repo;

    public JsonDeleteCommand(JsonFileImplementation repo) {
        this.repo = repo;
    }

    @Override
    public JsonObject execute(JsonObject request) {
        try {
            String fileName = request.getString("file");
            String client   = request.getString("client");

            boolean deleted = repo.delete(fileName, client);
            if (deleted) {
                return Json.createObjectBuilder()
                        .add("status", "OK")
                        .add("data", Json.createObjectBuilder()
                                .add("message", "Fichier '" + fileName + "' supprimé."))
                        .build();
            } else {
                return error("Fichier introuvable ou vous n'êtes pas le propriétaire.");
            }

        } catch (IllegalArgumentException e) {
            return error("Champ manquant : " + e.getMessage());
        } catch (SQLException e) {
            return error("Erreur base de données : " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonObject error(String msg) {
        return Json.createObjectBuilder().add("status", "ERROR").add("message", msg).build();
    }
}
