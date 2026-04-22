package Utils;

import Commands.Command;
import Commands.http.*;
import Commands.json.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import DataBase.JsonFileImplementation;
import Services.HttpServiceImpl;
import Services.IHTTPService;
import Services.JsonServiceImpl;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Reçoit un JsonObject, lit "service" + "op", et route vers la bonne Command.
 *
 * Toutes les commandes sont instanciées une seule fois (stateless).
 * Le dispatcher lui-même est partagé entre tous les ClientHandler.
 */
public class CommandDispatcher {

    // clé = "SERVICE:OP"  ex: "HTTP:GET"  "JSON:CREATE"
    private final Map<String, Command> commands = new HashMap<>();

    public CommandDispatcher() {
        // --- Services (partagés, stateless)
        IHTTPService httpService = new HttpServiceImpl();
        JsonFileImplementation repo  = new JsonFileImplementation();

        // --- Commandes HTTP
        commands.put("HTTP:GET",    new HttpGetCommand(httpService));
        commands.put("HTTP:POST",   new HttpPostCommand(httpService));
        commands.put("HTTP:add",    new HttpPutCommand(httpService));
        commands.put("HTTP:PATCH",  new HttpPatchCommand(httpService));
        commands.put("HTTP:DELETE", new HttpDeleteCommand(httpService));

        // --- Commandes JSON CRUD
        commands.put("JSON:CREATE", new JsonCreateCommand(repo));
        commands.put("JSON:GET",    new JsonGetCommand(repo));
        commands.put("JSON:GETALL", new JsonGetAllCommand(repo));
        commands.put("JSON:SET",    new JsonSetCommand(repo));
        commands.put("JSON:DELETE", new JsonDeleteCommand(repo));
    }

    public String dispatch(String raw) {
        try {

            JsonObject request;
            try (JsonReader reader = Json.createReader(new StringReader(raw))) {
                request = reader.readObject();
            } catch (Exception e) {
                request = Json.createObjectBuilder().build();
            }


            String service = JsonUtils.optString(request,"service", "").toUpperCase();
            String op      = JsonUtils.optString(request,"op",      "").toUpperCase();
            String key     = service + ":" + op;

            Command cmd = commands.get(key);
            if (cmd == null) {
                return Json.createObjectBuilder()
                        .add("status", "ERROR")
                        .add("message", "Commande inconnue : " + key
                                + ". Services disponibles : HTTP, JSON.")
                        .toString();
            }

            return cmd.execute(request).toString();

        } catch (Exception e) {
            // JSON malformé ou erreur inattendue
            return Json.createObjectBuilder()
                    .add("status", "ERROR")
                    .add("message", "Requête invalide : " + e.getMessage())
                    .toString();
        }
    }
}