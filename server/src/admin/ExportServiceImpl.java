package admin;

import serverUI.api.ExportService;
import storage.CardStore;
import storage.CardStore.CryptInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExportServiceImpl implements ExportService {
    private final CardStore store;

    public ExportServiceImpl(CardStore store) {
        this.store = store;
    }

    @Override
    public void exportByCryptogram(String path) {
        Map<String,CryptInfo> snap = store.snapshotCryptograms();

        String content =
                header("Sorted by cryptogram")+
                snap.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(e -> formatLine(e.getKey(),e.getValue().card))
                        .collect(Collectors.joining(System.lineSeparator()))
                +System.lineSeparator();

        write(path,content);
    }

    @Override
    public void exportByCard(String path) {
        Map<String, CryptInfo> snap = store.snapshotCryptograms();

        String content =
                header("Sorted by card number")+
                snap.entrySet().stream()
                        .collect(Collectors.groupingBy(e -> e.getValue().card,
                                Collectors.mapping(Map.Entry::getKey, Collectors.toList())))
                        .entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .flatMap(e -> {
                            String card = e.getKey();
                            return e.getValue().stream()
                                    .sorted(Comparator.naturalOrder())
                                    .map(crypt -> formatLine(crypt,card));
                        })
                        .collect(Collectors.joining(System.lineSeparator()))
                +System.lineSeparator();
        write(path, content);
    }

    private static void write(String path, String content) {
        try {
            Path p = Path.of(path);
            Files.createDirectories(p.toAbsolutePath().getParent());
            Files.writeString(p, content);
        } catch (Exception e) {
            throw new RuntimeException("Export failed: "+ e.getMessage(), e);
        }
    }
    private static String header(String title) {
        return title + System.lineSeparator()
                + "CRYPT           -> CARD" + System.lineSeparator()
                + "-------------------------" + System.lineSeparator();
    }
    private static String formatLine(String crypt, String card) {
        return crypt + " -> " + card;
    }
}
