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
        if (field == null || fieldValue == null) {
            return null;
        }

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
        } else if (isDefaultClass(fieldValue.getClass().getName())) {
            return fieldValue;
        } else {
            ValueConverter valueSerializer = registry.getConverter(fieldValue);

            if (valueSerializer != null) {
                return valueSerializer.serialize(fieldValue);
            }
        }
        return null;
    }

    @SneakyThrows(Exception.class)
    public Object deserialize(Field field, Object fieldValue, Object fileValue) {
        if (field == null || fileValue == null) {
            return null;
        }

        if (fieldValue instanceof Map && field.getGenericType() instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

            if (types.length == 2) {
                ValueConverter keySerializer = registry.getConverter(types[0]),
                        valueSerializer = registry.getConverter(types[1]);

                if ((keySerializer == null && !isDefaultClass(types[0].getTypeName())) || (valueSerializer == null && !isDefaultClass(types[1].getTypeName()))) {
                    return fieldValue;
                }

                Map result = (Map) fieldValue.getClass().newInstance();
                LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) fileValue;

                for (Map.Entry<String, Object> entry : map.entrySet()) {
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
                    return fieldValue;
                }

                Collection result = (Collection) fieldValue;
                ArrayList<String> list = (ArrayList<String>) fileValue;

                for (String string : list) {
                    result.add(valueSerializer.deserialize(string));
                }

                return result;
            }
        } else if (isDefaultClass(fileValue.getClass().getName())) {
            if (field.getType().isPrimitive() && fileValue instanceof Number) {
                Number numValue = (Number) fileValue;

                try {
                    if (field.getType() == int.class || field.getType() == Integer.class) {
                        return numValue.intValue();
                    } else if (field.getType() == long.class || field.getType() == Long.class) {
                        return numValue.longValue();
                    } else if (field.getType() == double.class || field.getType() == Double.class) {
                        return numValue.doubleValue();
                    } else if (field.getType() == float.class || field.getType() == Float.class) {
                        return numValue.floatValue();
                    } else if (field.getType() == short.class || field.getType() == Short.class) {
                        return numValue.shortValue();
                    } else if (field.getType() == byte.class || field.getType() == Byte.class) {
                        return numValue.byteValue();
                    }
                } catch (NumberFormatException e) {
                }
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                 return Boolean.parseBoolean(String.valueOf(fileValue));
             }

            return fileValue;
        } else {
            ValueConverter valueSerializer = registry.getConverter(fieldValue);

            if (valueSerializer != null) {
                return valueSerializer.deserialize(String.valueOf(fileValue));
            }
        }
        return fieldValue;
    }

    public boolean isDefaultClass(String name) {
        return name != null && name.startsWith("java.lang");
    }

}
