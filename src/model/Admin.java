package model;

public class Admin {
    private int idAdmin;
    private String username;
    private String password;

    public Admin(String username, String password) {
        setUsername(username);
        setPassword(password);
    }

    public Admin(int idAdmin, String username, String password) {
        this(username, password);
        this.idAdmin = idAdmin;
    }

    public void setUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username admin tidak boleh kosong!");
        }
        this.username = username.trim();
    }

    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password tidak boleh kosong!");
        }
        this.password = password;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

