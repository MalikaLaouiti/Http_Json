package DataBase;

public class DataBaseConfig {
    public static final String NOM_DRIVER = "org.postgresql.Driver";
    public static final String IPServer="localhost";
    public static final String PORT= "5020";
    public static final String DataBaseName="Files";
    public static final String URL_DB ="jdbc:postgresql://"+IPServer+":"+PORT+"/"+DataBaseName;
    public static final String USERNAME="postgres";
    public static final String PASSWORD="malikaadmin";
}