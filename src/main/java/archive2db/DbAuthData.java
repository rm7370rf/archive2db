package archive2db;

public class DbAuthData {
    private String url;
    private String user;
    private String password;

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public DbAuthData setUrl(String url) {
        this.url = url;
        return this;
    }

    public DbAuthData setUser(String user) {
        this.user = user;
        return this;
    }

    public DbAuthData setPassword(String password) {
        this.password = password;
        return this;
    }
}
