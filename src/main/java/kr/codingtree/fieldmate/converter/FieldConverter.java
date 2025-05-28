package kr.codingtree.fieldmate.converter;

import kr.codingtree.fieldmate.ConverterRegistry;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@AllArgsConstructor
public class FieldConverter {

    private final ConverterRegistry registry;

    public Object serialize(Field field, Object fieldValue) {
        if (fieldValue instanceof Map && field.getGenericType() instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

            if (types.length == 2) {
                ValueConverter keySerializer = registry.getConverter(types[0]),
                        valueSerializer = registry.getConverter(types[1]);

                if ((keySerializer == null && !isDefaultClass(types[0].getTypeName())) || (valueSerializer == null && !isDefaultClass(types[1].getTypeName()))) {
                    return null;
                }

                Map<?, ?> map = (Map) fieldValue;
                LinkedHashMap<String, String> result = new LinkedHashMap<>();

                for (Map.Entry entry : map.entrySet()) {
                    String key = keySerializer != null ? keySerializer.serialize(entry.getKey()) : String.valueOf(entry.getKey()),
                            value = valueSerializer != null ? valueSerializer.serialize(entry.getValue()) : String.valueOf(entry.getValue());

                    result.put(key, value);
                }

                return result;
            }
        } else if (fieldValue instanceof Collection && field.getGenericType() instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

            if (types.length == 1) {
                if (isDefaultClass(types[0].getTypeName())) {
                    return fieldValue;
                }

                ValueConverter valueSerializer = registry.getConverter(types[0]);

                if (valueSerializer == null) {
                    return null;
                }

                Collection<?> collection = (Collection<?>) fieldValue;
                ArrayList<String> result = new ArrayList<>();

                for (Object object : collection) {
                    result.add(valueSerializer.serialize(object));
                }

                return result;
            }
        } else {
            ValueConverter valueSerializer = registry.getConverter(fieldValue);

            if (valueSerializer != null) {
                return valueSerializer.serialize(fieldValue);
            } else if (fieldValue != null && isDefaultClass(fieldValue.getClass().getName())) {
                return fieldValue;
            }
        }
        return null;
    }

    @SneakyThrows(Exception.class)
    public Object deserialize(Field field, Object fieldValue, Object fileValue) {
        if (fieldValue instanceof Map && field.getGenericType() instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

            if (types.length == 2) {
                ValueConverter keySerializer = registry.getConverter(types[0]),
                        valueSerializer = registry.getConverter(types[1]);

                if ((keySerializer == null && !isDefaultClass(types[0].getTypeName())) || (valueSerializer == null && !isDefaultClass(types[1].getTypeName()))) {
                    return null;
                }

                Map result = (Map) fieldValue.getClass().newInstance();
                LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) fileValue;

                for (Map.Entry<String , Object> entry : map.entrySet()) {
                    Object key = keySerializer != null ? keySerializer.deserialize(entry.getKey()) : String.valueOf(entry.getKey()),
                            value = valueSerializer != null ? valueSerializer.deserialize(entry.getValue().toString()) : String.valueOf(entry.getValue());

                    result.put(key, value);
                }

                return result;
            }
        } else if (fieldValue instanceof Collection && field.getGenericType() instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

            if (types.length == 1) {
                if (isDefaultClass(types[0].getTypeName())) {
                    return fieldValue;
                }

                ValueConverter valueSerializer = registry.getConverter(types[0]);

                if (valueSerializer == null) {
                    return null;
                }

                Collection result = (Collection) fieldValue;
                ArrayList<String> list = (ArrayList<String>) fileValue;

                for (String string : list) {
                    result.add(valueSerializer.deserialize(string));
                }

                return result;
            }
        } else if (isDefaultClass(fieldValue.getClass().getName())) {
            if (fieldValue instanceof String) {
                return String.valueOf(fileValue);
            } else if (fieldValue instanceof Integer) {
                try {
                    return Integer.parseInt(String.valueOf(fileValue));
                } catch (NumberFormatException e) {}
            } else if (fieldValue instanceof Double) {
                try {
                    return Double.parseDouble(String.valueOf(fileValue));
                } catch (NumberFormatException e) {}
            } else if (fieldValue instanceof Float) {
                try {
                    return Float.parseFloat(String.valueOf(fileValue));
                } catch (NumberFormatException e) {}
            } else if (fieldValue instanceof Boolean) {
                return Boolean.parseBoolean(String.valueOf(fileValue));
            }
        } else {
            ValueConverter valueSerializer = registry.getConverter(fieldValue);

            if (valueSerializer != null) {
                return valueSerializer.deserialize(String.valueOf(fileValue));
            }
        }
        return null;
    }

    public boolean isDefaultClass(String name) {
        return name.startsWith("java.lang.");
    }

}
