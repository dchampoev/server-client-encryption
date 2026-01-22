package luhn;

public final class LuhnValidator {

    private LuhnValidator() {}

    public static boolean isValid(String card) {
        if (card == null || card.length() != 16) {
            return false;
        }

        // must start with 3, 4, 5 or 6
        char first = card.charAt(0);
        if (first != '3' && first != '4' && first != '5' && first != '6') {
            return false;
        }

        int sum = 0;
        boolean doubleDigit = false;

        for (int i = card.length() - 1; i >= 0; i--) {
            char ch = card.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }

            int d = ch - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }

            sum += d;
            doubleDigit = !doubleDigit;
        }

        return sum % 10 == 0;
    }
}