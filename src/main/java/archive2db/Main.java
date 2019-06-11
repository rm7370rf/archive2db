package archive2db;

public class Main {
    private static final String DB_URL = "jdbc:mysql://IP:3306/db_name";
    private static final String DB_USER = "db_user";
    private static final String DB_PASSWORD = "db_password";

    private static final String FILE_URL = "file_url";

    private static final String ARCHIVED_FILE = "archived_file";
    private static final String UNARCHIVED_FILE = "unarchived_file";

    public static void main(String[] args) {
        try {
            DbAuthData authData = new DbAuthData()
                    .setUrl(DB_URL)
                    .setUser(DB_USER)
                    .setPassword(DB_PASSWORD);

            Handler handler = new Handler(FILE_URL, ARCHIVED_FILE, UNARCHIVED_FILE)
                    .setAuthData(authData);
            handler.doIt();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
