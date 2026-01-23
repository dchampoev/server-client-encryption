package storage;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CardStore {
    public static final int MAX_ATTEMPTS = 12;

    public static final class CardRecord {
        public final int baseShift;
        public final AtomicInteger attempts = new AtomicInteger(0);

        public CardRecord(int baseShift) {
            this.baseShift = baseShift;
        }
    }

    public static final class CryptInfo {
        public final String card;
        public final int attempts;
        public final int baseShift;

        public CryptInfo(String card, int attempts, int baseShift) {
            this.card = card;
            this.attempts = attempts;
            this.baseShift = baseShift;
        }
    }

    private final SecureRandom random = new SecureRandom();

    private final ConcurrentHashMap<String, CardRecord> cardToRecord = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CryptInfo> cryptToInfo = new ConcurrentHashMap<>();

    public CardRecord getOrCreateRecord(String card) {
        return cardToRecord.computeIfAbsent(card, c -> new CardRecord(randomBaseShift()));
    }

    public int nextAttemptOrFail(CardRecord record) {
        int next = record.attempts.incrementAndGet();
        if (next >= MAX_ATTEMPTS) {
            record.attempts.decrementAndGet();
            return -1;
        }
        return next;
    }

    public void saveCryptogram(String crypt, String card, int attempt, int baseShift) {
        cryptToInfo.put(crypt, new CryptInfo(card, attempt, baseShift));
    }

    public CryptInfo findByCryptogram(String crypt) {
        return cryptToInfo.get(crypt);
    }

    private int randomBaseShift() {
        return 1 + random.nextInt(15);
    }

    public Map<String, CryptInfo> snapshotCryptograms() {
        return Map.copyOf(cryptToInfo);
    }
}
