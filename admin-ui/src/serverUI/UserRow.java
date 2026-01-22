package serverUI;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserRow {
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty rights = new SimpleStringProperty();

    public UserRow(String username, String rights) {
        this.username.set(username);
        this.rights.set(rights);
    }
    public String getUsername() { return username.get(); }
    public StringProperty usernameProperty() { return username; }

    public String getRights() { return rights.get(); }
    public StringProperty rightsProperty() { return rights; }
}
