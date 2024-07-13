package cn.touchair.audiobox.common;

public final class Prerequisites {
    private Prerequisites() {}
    public static void check(boolean condition, String errorMsg) {
        if (condition) return;
        throw new RuntimeException(errorMsg);
    }
}
