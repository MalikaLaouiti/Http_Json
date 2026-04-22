package Commands.http;
import Commands.Command;

import javax.json.Json;
import javax.json.JsonObject;

import Commands.json.JsonUtils;
import Services.IHTTPService;

public class HttpGetCommand implements Command {

    private final IHTTPService httpService;

    public HttpGetCommand(IHTTPService httpService) {
        this.httpService = httpService;
    }

    @Override
    public JsonObject execute(JsonObject request) {
        try {
            String url = request.getString("url");
            JsonObject headers = JsonUtils.optJsonObject(request,"headers");

            String responseBody = httpService.get(url, headers);
            return  Json.createObjectBuilder()
                    .add("status", "OK")
                    .add("data",  Json.createObjectBuilder().add("body", responseBody))
                    .build();

        } catch (Exception e) {
            return  Json.createObjectBuilder().add("status", "ERROR").add("message", e.getMessage()).build();
        }
    }
}