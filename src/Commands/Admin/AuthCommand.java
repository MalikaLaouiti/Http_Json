package Commands.Admin;

import Commands.Command;
import javax.json.Json;
import javax.json.JsonObject;

import DataBase.AuthAdmin;
import java.sql.SQLException;

public class AuthCommand implements Command {

    private final AuthAdmin auth;

    public AuthCommand(AuthAdmin auth) {
        this.auth = auth;
    }

    @Override
    public JsonObject execute(JsonObject request) {
        try {
            String username = request.getString("username");
            String password = request.getString("password");

            boolean ok = auth.login(username, password);

            if (ok) {
                return Json.createObjectBuilder()
                        .add("status", "OK")
                        .add("message", "Authentification réussie")
                        .build();
            } else {
                return Json.createObjectBuilder()
                        .add("status", "ERROR")
                        .add("message", "Identifiants incorrects")
                        .build();
            }

        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    private JsonObject error(String msg) {
        return Json.createObjectBuilder()
                .add("status", "ERROR")
                .add("message", msg)
                .build();
    }
}