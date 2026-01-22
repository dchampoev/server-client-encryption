package cipher;

public final class CardCipher {
    private CardCipher() {}

    public static String encrypt(String card, int attempt, int baseShift) {
        if (card == null || card.length() != 16) {
            throw new IllegalArgumentException("Card number must be 16 digits");
        }
        if (attempt < 1 || attempt > 12) {
            throw new IllegalArgumentException("Attempt must be in range 1..12");
        }
        if (baseShift < 0 || baseShift > 15) {
            throw new IllegalArgumentException("Base shift must be in range 0..15");
        }
        int shift0 = (baseShift + (attempt - 1)) % 16;
        StringBuilder cipherText = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            char ch =  card.charAt(i);
            if(ch<'0'||ch>'9') {
                throw new IllegalArgumentException("Invalid digit in card number");
            }
            int digit = ch-'0';

            int shift16 = (shift0 + i) %16;
            int shift = shift16 % 10;

            int encrypted = (digit + shift)%10;
            cipherText.append(encrypted);
        }
        return  cipherText.toString();
    }

    public static String decrypt(String crypt, int attempt, int baseShift) {
        if (crypt == null || crypt.length() != 16) {
            throw new IllegalArgumentException("Crypt number must be 16 digits");
        }
        if (attempt < 1 || attempt > 12) {
            throw new IllegalArgumentException("Attempt must be in range 1..12");
        }
        if (baseShift < 0 || baseShift > 15) {
            throw new IllegalArgumentException("Base shift must be in range 0..15");
        }
        int  shift0 = (baseShift + (attempt - 1)) % 16;
        StringBuilder cardText = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            char ch =  crypt.charAt(i);
            if(ch<'0'||ch>'9') {
                throw new IllegalArgumentException("Invalid digit in crypt number");
            }
            int digit = ch - '0';
            int shift = ((shift0 + i) % 16)%10;
            int decrypted = (digit - shift + 10)%10;
            cardText.append(decrypted);
        }
        return  cardText.toString();
    }
}
