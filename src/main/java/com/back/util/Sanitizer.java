package com.back.util;

public class Sanitizer {

    private Sanitizer() {}

    public static String sanitizePath(String input) {
        if (input == null) return "";
        return input
            .replace("..", "")
            .replace("\\", "/")
            .replaceAll("^/+|/+$", "")
            .trim();
    }

    public static boolean isValidFileName(String filename) {
        if (filename == null || filename.isBlank()) return false;
        return !filename.contains("..") && !filename.contains("/") && !filename.contains("\\");
    }
}
