package kr.codingtree.fieldmate;

import kr.codingtree.fieldmate.serializer.ValueSerializer;
import kr.codingtree.fieldmate.serializer.defaults.UUIDSerializer;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@UtilityClass
public class FieldMate {

    static {
        serializers = new ArrayList<>();
    }

    @Getter
    private ArrayList<ValueSerializer> serializers = null;

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

                    if (serializer.equals(object)) {
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
            return serializers.stream().filter(serializer -> serializer.equals(object)).findFirst().orElse(null);
        }

        return null;
    }

    public boolean hasSerializer(Object clazz) {
        return serializers.stream().anyMatch(serializer -> serializer.equals(clazz));
    }

    public void clearSerializers() {
        serializers.clear();
    }

    public Object serializer(Field field, Object fieldValue) {
        if (fieldValue instanceof Map && field.getGenericType() instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

            if (types.length == 2) {
                ValueSerializer keySerializer = getSerializer(types[0]),
                        valueSerializer = getSerializer(types[1]);

                if ((keySerializer == null && !isDefaultClass(types[0].getTypeName())) || (valueSerializer == null && !isDefaultClass(types[1].getTypeName()))) {
                    return null;
                }

                Map<?, ?> map = (Map<?, ?>) fieldValue;
                LinkedHashMap<String, String> result = new LinkedHashMap<>();

                for (Map.Entry<?, ?> entry : map.entrySet()) {
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

    public Object deserialize(Field field, Object fieldValue, Object fileValue) {
        return null;
    }

    public boolean isDefaultClass(String name) {
        return name.startsWith("java.lang.");
    }
}
