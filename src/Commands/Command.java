package Commands;

import javax.json.JsonObject;

public interface Command {
    JsonObject execute(JsonObject request);

}
