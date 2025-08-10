package utils.enums;

public enum ScrollAmount {
    SMALL(300),
    MEDIUM(650),
    LARGE(1000);

    private final int value;

    ScrollAmount(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}