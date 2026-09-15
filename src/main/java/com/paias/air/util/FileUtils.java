package com.paias.air.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static String readString(String fileName) {
        try (InputStream is = FileUtils.class.getClassLoader().getResourceAsStream(fileName)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
