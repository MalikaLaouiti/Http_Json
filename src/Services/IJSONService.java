package Services;

import javax.json.JsonArray;
import javax.json.JsonObject;

public interface IJSONService {

        boolean    create(String fileName, String owner, JsonObject content, String visibility) throws Exception;
        JsonObject get   (String fileName, String client) throws Exception;
        JsonArray getAll(String client) throws Exception;
        boolean    set   (String fileName, String owner, String path, Object value) throws Exception;
        boolean    delete(String fileName, String owner) throws Exception;
    
}
