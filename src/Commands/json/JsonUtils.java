package Commands.json;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class JsonUtils {


    public static String optString(JsonObject obj, String key, String defaultValue) {
        if (obj == null || !obj.containsKey(key)) {
            return defaultValue;
        }
        try {
            return obj.getString(key);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public static String optString(JsonObject obj, String key) {
        if (obj == null || !obj.containsKey(key)) {
            return "";
        }
        try {
            return obj.getString(key);
        } catch (ClassCastException e) {
            return "";
        }
    }

    public static JsonArray optJsonArray(JsonObject obj, String key) {
        if (obj == null || !obj.containsKey(key)) {
            return null;
        }
        try {
            return obj.getJsonArray(key);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public static JsonObject optJsonObject(JsonObject obj, String key) {
        if (obj == null || !obj.containsKey(key)) {
            return null;
        }
        try {
            return obj.getJsonObject(key);
        } catch (ClassCastException e) {
            return null;
        }
    }
}
