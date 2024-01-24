package cn.touchair.audiobox.common;

public abstract class Prerequisites {
    public static void check(boolean condition, String errorMsg) {
        if (condition) return;
        throw new RuntimeException(errorMsg);
    }
}
