package auth;

public interface UserService {
    User authenticate(String username, String password);
}
