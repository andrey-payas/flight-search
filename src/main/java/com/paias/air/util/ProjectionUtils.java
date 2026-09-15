package com.paias.air.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.function.Function;

public class ProjectionUtils {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    private static final ObjectMapper IGNORE_UNKNOWN_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    public static <T> Function<Object, T> as(Class<T> toClass) {
        return p -> OBJECT_MAPPER.convertValue(p, toClass);
    }

    public static <T> T copyFieldsIgnoreUnknown(Class<T> toClass, Object value) {
        return IGNORE_UNKNOWN_MAPPER.convertValue(value, toClass);
    }
}
