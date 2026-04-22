package Services;


import javax.json.JsonArray;
import javax.json.JsonObject;
import DataBase.JsonFileImplementation;

public class JsonServiceImpl implements IJSONService {

    private final JsonFileImplementation repo;

    public JsonServiceImpl(JsonFileImplementation repo) {
        this.repo = repo;
    }

    @Override
    public boolean create(String fileName, String owner, JsonObject content, String visibility)
            throws Exception {
        return repo.create(fileName, owner, content, visibility);
    }

    @Override
    public JsonObject get(String fileName, String client) throws Exception {
        return repo.get(fileName, client);
    }

    @Override
    public JsonArray getAll(String client) throws Exception {
        return repo.getAll(client);
    }

    @Override
    public boolean set(String fileName, String owner, String path, Object value) throws Exception {
        return repo.set(fileName, owner, path, value);
    }

    @Override
    public boolean delete(String fileName, String owner) throws Exception {
        return repo.delete(fileName, owner);
    }
}