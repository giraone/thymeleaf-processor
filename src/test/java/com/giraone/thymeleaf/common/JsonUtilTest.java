package com.giraone.thymeleaf.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("squid:S100")
public class JsonUtilTest {

    @Test
    public void whenConvertObjectToJsonBytes_thenReceiveBytes() throws IOException {
        byte[] bytes = JsonUtil.convertObjectToJsonBytes("Hello World");
        assertThat(bytes).isNotEmpty();
        assertThat(bytes.length).isEqualTo(13);
    }

    @Test
    public void whenConvertObjectToJsonString_thenReceiveString() throws IOException {
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("testKey", "testValue");
        String resultJson = JsonUtil.convertObjectToJsonString(jsonMap);
        assertThat(resultJson).isEqualTo("{\"testKey\":\"testValue\"}");
    }

    @Test
    public void whenConvertToJsonMap_thenReceiveMap() throws IOException {
        String jsonString = "{\"testKey\":\"testValue\"}";
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("testKey", "testValue");

        Map<String, Object> resultMap = JsonUtil.convertToJsonMap(jsonString);
        assertThat(resultMap).isEqualTo(jsonMap);
    }

    @Test
    public void whenConvertToJsonList_thenReceiveListOfMaps() throws IOException {
        String jsonString = "[{\"testKey\":\"testValue\"},{\"testKey\":\"testValue\"}]";
        List<Map<String, String>> expectedListOfMaps = new ArrayList<>();
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("testKey", "testValue");

        expectedListOfMaps.add(jsonMap);
        expectedListOfMaps.add(jsonMap);

        List<Map<String, Object>> resultList = JsonUtil.convertToJsonList(jsonString);
        assertThat(resultList).isEqualTo(expectedListOfMaps);
    }

}