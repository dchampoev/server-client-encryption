package admin;

import serverUI.api.LogService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LogServiceImpl implements LogService {

    private final Path logPath;

    public LogServiceImpl(String path) {
        this.logPath = Path.of(path);
    }

    @Override
    public String readAll() {
        try {
            if (!Files.exists(logPath)) return "";
            return Files.readString(logPath);
        } catch (Exception e) {
            return "Failed to read log file: " + e.getMessage();
        }
    }

    @Override
    public void clear() {
        try {
            Files.writeString(logPath, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ignored) {}
    }
}
