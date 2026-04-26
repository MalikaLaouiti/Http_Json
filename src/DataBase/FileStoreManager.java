package DataBase;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
public class FileStoreManager {

    private static final String BASE_DIR = "LocalDataBase";

    private final Path baseDir;

    public FileStoreManager() throws IOException {
        this.baseDir = Paths.get(BASE_DIR).toAbsolutePath().normalize();
        Files.createDirectories(baseDir);
        System.out.println("[FileStore] Racine : " + baseDir);
    }

    public String write(String owner, String fileName, JsonObject content) throws IOException {
        Path file = resolve(owner, fileName);
        Files.createDirectories(file.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            // Indentation manuelle simple pour lisibilité
            writer.write(prettyPrint(content));
        }

        // Retourne le chemin relatif pour stockage en BD
        return baseDir.relativize(file).toString().replace("\\", "/");
    }

    public JsonObject read(String owner, String fileName) throws IOException {
        Path file = resolve(owner, fileName);
        if (!Files.exists(file)) return null;

        try (InputStream is = Files.newInputStream(file);
             JsonReader reader = Json.createReader(is)) {
            return reader.readObject();
        }
    }


    public boolean patch(String owner, String fileName, String path, String value) throws IOException {
        JsonObject current = read(owner, fileName);
        if (current == null) return false;

        var builder = Json.createObjectBuilder(current);

        if (path.contains(".")) {
            System.out.println("[FileStore] Patch imbriqué : sera synchronisé depuis PostgreSQL.");
            return true;
        }

        builder.add(path, value);
        JsonObject updated = builder.build();
        write(owner, fileName, updated);
        return true;
    }


    public void syncFromDb(String owner, String fileName, JsonObject contentFromDb) throws IOException {
        write(owner, fileName, contentFromDb);
    }

    public boolean delete(String owner, String fileName) throws IOException {
        Path file = resolve(owner, fileName);
        return Files.deleteIfExists(file);
    }

    public boolean exists(String owner, String fileName) {
        try {
            return Files.exists(resolve(owner, fileName));
        } catch (IOException e) {
            return false;
        }
    }

    private Path resolve(String owner, String fileName) throws IOException {
        // Nettoyage des entrées
        String safeOwner = owner.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String safeName  = fileName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        if (!safeName.endsWith(".json")) safeName += ".json";

        Path resolved = baseDir.resolve(safeOwner).resolve(safeName).normalize();

        // Vérification anti path traversal
        if (!resolved.startsWith(baseDir)) {
            throw new IOException("Accès refusé : chemin invalide.");
        }
        return resolved;
    }

    private String prettyPrint(JsonObject obj) {

        StringWriter sw = new StringWriter();
        try (javax.json.JsonWriter jw = Json.createWriterFactory(
                        java.util.Map.of(javax.json.stream.JsonGenerator.PRETTY_PRINTING, true))
                .createWriter(sw)) {
            jw.writeObject(obj);
        }
        return sw.toString();
    }
}