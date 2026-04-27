package Commands.history;
import Commands.Command;
import DataBase.HistoryRequestImplementation;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class HistoryGetCommand implements Command {

    private final HistoryRequestImplementation repo;

    public HistoryGetCommand(HistoryRequestImplementation repo) {
        this.repo = repo;
    }

    @Override
    public JsonObject execute(JsonObject request) {
        try {

            int limit = request.containsKey("limit")
                    ? request.getInt("limit") : 50;

            JsonArray entries = repo.getAll(limit);

            return Json.createObjectBuilder()
                    .add("status", "OK")
                    .add("data", Json.createObjectBuilder()
                            .add("count",   entries.size())
                            .add("entries", entries))
                    .build();

        } catch (Exception e) {
            return Json.createObjectBuilder()
                    .add("status",  "ERROR")
                    .add("message", e.getMessage())
                    .build();
        }
    }
}