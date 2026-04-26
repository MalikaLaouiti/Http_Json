package Utils;

import Commands.Command;
import Commands.history.HistoryGetCommand;
import Commands.http.*;
import Commands.json.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import DataBase.FileStoreManager;
import DataBase.HistoryRequestImplementation;
import DataBase.JsonFileImplementation;
import Services.HttpServiceImpl;
import Services.IHTTPService;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;


public class CommandDispatcher {

    private final Map<String, Command> commands = new HashMap<>();
    private final HistoryRequestImplementation historyRepo;
    public CommandDispatcher() {
        try {
            FileStoreManager file=new FileStoreManager();

        IHTTPService httpService = new HttpServiceImpl();
        JsonFileImplementation repo  = new JsonFileImplementation(file);
        historyRepo= new HistoryRequestImplementation();

        commands.put("HTTP:GET",    new HttpGetCommand(httpService));
        commands.put("HTTP:POST",   new HttpPostCommand(httpService));
        commands.put("HTTP:add",    new HttpPutCommand(httpService));
        commands.put("HTTP:PATCH",  new HttpPatchCommand(httpService));
        commands.put("HTTP:DELETE", new HttpDeleteCommand(httpService));

        commands.put("JSON:CREATE", new JsonCreateCommand(repo));
        commands.put("JSON:GET",    new JsonGetCommand(repo));
        commands.put("JSON:GETALL", new JsonGetAllCommand(repo));
        commands.put("JSON:SET",    new JsonSetCommand(repo));
        commands.put("JSON:DELETE", new JsonDeleteCommand(repo));

        commands.put("HISTORY:GET", new HistoryGetCommand(historyRepo));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            String clientId = JsonUtils.optString(request,"client", "anonymous"); // ou "url" pour HTTP
            String target   = resolveTarget(service, request);

            Command cmd = commands.get(key);
            if (cmd == null) {
                return Json.createObjectBuilder()
                        .add("status", "ERROR")
                        .add("message", "Commande inconnue : " + key
                                + ". Services disponibles : HTTP, JSON.")
                        .toString();
            }
            JsonObject response = cmd.execute(request);
            String status = response.getString("status"); // "OK" ou "ERROR"
            String message = status.equals("ERROR") ? JsonUtils.optString(response,"message","") : null;
            historyRepo.record(clientId, service, op, target, status, message);
            return response.toString();

        } catch (Exception e) {
            // JSON malformé ou erreur inattendue
            return Json.createObjectBuilder()
                    .add("status", "ERROR")
                    .add("message", "Requête invalide : " + e.getMessage())
                    .toString();
        }
    }
    private String resolveTarget(String service, JsonObject req) {
        if ("HTTP".equals(service))  return JsonUtils.optString(req,"url", "?");
        if ("JSON".equals(service))  return JsonUtils.optString(req,"file", "?");
        return "?";
    }
}