package Services;

import javax.json.JsonObject;

public interface IHTTPService {
    String get   (String url, JsonObject headers) throws Exception;
    String post  (String url, String body, JsonObject headers) throws Exception;
    String put   (String url, String body, JsonObject headers) throws Exception;
    String patch (String url, String body, JsonObject headers) throws Exception;
    String delete(String url, JsonObject headers) throws Exception;
}
