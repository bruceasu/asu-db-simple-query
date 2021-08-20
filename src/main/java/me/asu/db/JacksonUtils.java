package me.asu.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2020/8/5.
 */
public class JacksonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

    public static String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serializeForPrint(Object data) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode deserialize(String data) {
        try {
            return objectMapper.readTree(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(String data, Class<T> cls) {
        if (cls == String.class) {
            return (T) data;
        } else {
            try {
                return objectMapper.readValue(data, new TypeReference<T>() {
                    @Override
                    public Type getType() {
                        return cls;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * to Map
     *
     * @param data json String
     * @return a Map
     */
    public static Map deserializeToMap(String data) {
        try {
            return objectMapper.readValue(data, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> deserializeToList(String data, Class<T> cls) {
        CollectionType listType = objectMapper.getTypeFactory()
                                              .constructCollectionType(ArrayList.class, cls);
        try {
            return objectMapper.readValue(data, listType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<Map> deserializeToList(String data) {
        return deserializeToList(data, Map.class);
    }

    public static Map convertToMap(Object object) {
        //用jackson将bean转换为map
        return objectMapper.convertValue(object, new TypeReference<Map>() {});
    }


    public static List<Map> convertToListMap(List<Object> list) {
        //用jackson将bean转换为List<Map>
        return objectMapper.convertValue(list, new TypeReference<List<Map<String, String>>>() {});
    }

    public static <T> T convertToObject(Object data, Class<T> cls) {
        if (data == null) return null;
        if (data.getClass() == JsonNode.class) {
            return (T) data;
        } else {
            try {
                return objectMapper.convertValue(data, new TypeReference<T>() {
                    @Override
                    public Type getType() {
                        return cls;
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String asText(JsonNode dtNode, String item) {
        return asText(dtNode, item, null);
    }

    public static String asText(JsonNode dtNode, String item, String defaultValue) {
        if (dtNode == null) {
            return defaultValue;
        }

        JsonNode node = dtNode.get(item);
        if (node == null) {
            return defaultValue;
        }
        return node.asText();
    }

}
