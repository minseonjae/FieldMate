package kr.codingtree.fieldmate;

import kr.codingtree.fieldmate.annotation.ExcludeField;
import kr.codingtree.fieldmate.annotation.FieldName;
import kr.codingtree.fieldmate.file.FileType;
import kr.codingtree.fieldmate.serializer.ValueSerializer;
import kr.codingtree.fieldmate.serializer.defaults.UUIDSerializer;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@UtilityClass
public class FieldMate {

    @Getter
    private final ArrayList<ValueSerializer> serializers = new ArrayList<>();;

    public void init() {
        serializers.add(new UUIDSerializer());
    }

    @SneakyThrows
    public boolean registerSerializer(Object object) {
        if (object instanceof Class) {
            object = ((Class) object).newInstance();
        }

        if (object instanceof ValueSerializer && !hasSerializer(object)) {
            serializers.add((ValueSerializer) object);
            return true;
        }

        return false;
    }

    public boolean unregisterSerializer(Object object) {
        if (hasSerializer(object)) {
            if (!serializers.remove(object)) {
                Iterator<ValueSerializer> iterator = serializers.iterator();

                while (iterator.hasNext()) {
                    ValueSerializer serializer = iterator.next();

                    if (serializer.canSerialize(object)) {
                        iterator.remove();
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public ValueSerializer getSerializer(Object object) {
        if (object != null) {
            return serializers.stream().filter(serializer -> serializer.canSerialize(object)).findFirst().orElse(null);
        }

        return null;
    }

    public boolean hasSerializer(Object clazz) {
        return serializers.stream().anyMatch(serializer -> serializer.canSerialize(clazz));
    }

    public void clearSerializers() {
        serializers.clear();
    }

    public Object serializeField(Field field, Object fieldValue) {
        if (fieldValue instanceof Map && field.getGenericType() instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

            if (types.length == 2) {
                ValueSerializer keySerializer = getSerializer(types[0]),
                        valueSerializer = getSerializer(types[1]);

                if ((keySerializer == null && !isDefaultClass(types[0].getTypeName())) || (valueSerializer == null && !isDefaultClass(types[1].getTypeName()))) {
                    return null;
                }

                Map<?, ?> map = (Map) fieldValue;
                LinkedHashMap<String, String> result = new LinkedHashMap<>();

                for (Map.Entry entry : map.entrySet()) {
                    String key = keySerializer != null ? keySerializer.serializer(entry.getKey()) : String.valueOf(entry.getKey()),
                            value = valueSerializer != null ? valueSerializer.serializer(entry.getValue()) : String.valueOf(entry.getValue());

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

                ValueSerializer valueSerializer = getSerializer(types[0]);

                if (valueSerializer == null) {
                    return null;
                }

                Collection<?> collection = (Collection<?>) fieldValue;
                ArrayList<String> result = new ArrayList<>();

                for (Object object : collection) {
                    result.add(valueSerializer.serializer(object));
                }

                return result;
            }
        } else {
            ValueSerializer valueSerializer = getSerializer(fieldValue);

            if (valueSerializer != null) {
                return valueSerializer.serializer(fieldValue);
            } else if (fieldValue != null && isDefaultClass(fieldValue.getClass().getName())) {
                return fieldValue;
            }
        }
        return null;
    }

    @SneakyThrows(Exception.class)
    public Object deserializeField(Field field, Object fieldValue, Object fileValue) {
        if (fieldValue instanceof Map && field.getGenericType() instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

            if (types.length == 2) {
                ValueSerializer keySerializer = getSerializer(types[0]),
                        valueSerializer = getSerializer(types[1]);

                if ((keySerializer == null && !isDefaultClass(types[0].getTypeName())) || (valueSerializer == null && !isDefaultClass(types[1].getTypeName()))) {
                    return null;
                }

                Map result = (Map) fieldValue.getClass().newInstance();
                LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) fileValue;

                for (Map.Entry<String , Object> entry : map.entrySet()) {
                    Object key = keySerializer != null ? keySerializer.deserializer(entry.getKey()) : String.valueOf(entry.getKey()),
                            value = valueSerializer != null ? valueSerializer.deserializer(entry.getValue().toString()) : String.valueOf(entry.getValue());

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

                ValueSerializer valueSerializer = getSerializer(types[0]);

                if (valueSerializer == null) {
                    return null;
                }

                Collection result = (Collection) fieldValue;
                ArrayList<String> list = (ArrayList<String>) fileValue;

                for (String string : list) {
                    result.add(valueSerializer.deserializer(string));
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
            ValueSerializer valueSerializer = getSerializer(fieldValue);

            if (valueSerializer != null) {
                return valueSerializer.deserializer(String.valueOf(fileValue));
            }
        }
        return null;
    }

    @SneakyThrows(Exception.class)
    public void load(File file, FileType fileType, Object loadClass) {
        LinkedHashMap<String, Object> map = fileType.load(file);

        for (Field field : loadClass.getClass().getDeclaredFields()) {
            if (field.getAnnotation(ExcludeField.class) == null) {
                field.setAccessible(true);

                FieldName fieldName = field.getAnnotation(FieldName.class);
                String name;

                if (fieldName != null) {
                    name = fieldName.value();
                } else {
                    name = field.getName();
                }

                Object value = deserializeField(field, field.get(loadClass), map.get(name));

                if (value != null) {
                    field.set(loadClass, value);
                }
            }
        }
    }

    @SneakyThrows(Exception.class)
    public void save(File file, FileType fileType, Object saveClass) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        for (Field field : saveClass.getClass().getDeclaredFields()) {
            if (field.getAnnotation(ExcludeField.class) == null) {
                field.setAccessible(true);

                Object value = serializeField(field, field.get(saveClass));

                if (value != null) {
                    FieldName fieldName = field.getAnnotation(FieldName.class);
                    String name;

                    if (fieldName != null) {
                        name = fieldName.value();
                    } else {
                        name = field.getName();
                    }

                    map.put(name, value);
                }
            }
        }

        fileType.save(file, map);
    }

    public boolean isDefaultClass(String name) {
        return name.startsWith("java.lang.");
    }
}
