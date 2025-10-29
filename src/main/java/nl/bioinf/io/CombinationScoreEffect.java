package nl.bioinf.io;


// enum met: enhancing(+), oppsosing(-), unknown(+ en -), synergetisch(+, extra text)
public enum CombinationScoreEffect {
    ENHANCING("+"),
    OPPOSING("-"),
    SYNERGETISCH("+"),
    UNKNOWN("+/-");

    private final String symbol;

    CombinationScoreEffect(String symbol) {
        this.symbol = symbol;
    }

    public String GetSymbol() {
        return symbol;
    }

    public static CombinationScoreEffect fromResult(String result) {
        if (result == null) return UNKNOWN;
        return switch (result.toLowerCase()) {
            case "enhancing"-> ENHANCING;
            case "opposing" -> OPPOSING;
            case "synergetisch" -> SYNERGETISCH;
            default -> UNKNOWN;
        };
    }
}
