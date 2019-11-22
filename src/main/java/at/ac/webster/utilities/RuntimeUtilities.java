package at.ac.webster.utilities;

public final class RuntimeUtilities {
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
