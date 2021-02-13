package com.giraone.thymeleaf.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE
        = new TypeReference<>() {
    };
    private static final TypeReference<List<Map<String, Object>>> LIST_TYPE_REFERENCE
        = new TypeReference<>() {
    };

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final JavaTimeModule module = new JavaTimeModule();
        MAPPER.registerModule(module);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // StdDateFormat is ISO8601 since jackson 2.9
        MAPPER.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }

    // Hide
    private JsonUtil() {}

    /**
     * Convert an object to JSON byte array.
     *
     * @param object the object to convert
     * @return the JSON byte array
     * @throws IOException on any IO error
     */
    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        return MAPPER.writeValueAsBytes(object);
    }

    /**
     * Convert an object to JSON string.
     *
     * @param object the object to convert
     * @return the JSON String
     * @throws IOException on any IO error
     */
    public static String convertObjectToJsonString(Object object) throws IOException {
        return MAPPER.writeValueAsString(object);
    }

    /**
     * Convert a JSON string to HashMap<String,Object>.
     *
     * @param jsonString the string to convert
     * @return the untyped HashMap
     * @throws IOException on any IO error
     */
    public static Map<String, Object> convertToJsonMap(String jsonString) throws IOException {
        return MAPPER.readValue(jsonString, MAP_TYPE_REFERENCE);
    }

    /**
     * Convert a JSON string to List<HashMap<String,Object>>.
     *
     * @param jsonString the string to convert
     * @return the untyped list of items
     * @throws IOException on any IO error
     */
    public static List<Map<String, Object>> convertToJsonList(String jsonString)
        throws IOException {

        return MAPPER.readValue(jsonString, LIST_TYPE_REFERENCE);
    }

    public static <T> T convert(String jsonString,Class<T> valueTypeRef)
        throws IOException {

        return MAPPER.readValue(jsonString, valueTypeRef);
    }

    @SuppressWarnings("unused")
    public static <T> T convert(String jsonString, TypeReference<T> valueTypeRef)
        throws IOException {

        return MAPPER.readValue(jsonString, valueTypeRef);
    }
}