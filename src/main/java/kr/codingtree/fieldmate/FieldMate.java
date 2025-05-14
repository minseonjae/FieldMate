package kr.codingtree.fieldmate;

import kr.codingtree.fieldmate.serializer.ValueSerializer;
import kr.codingtree.fieldmate.serializer.defaults.UUIDSerializer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Iterator;

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
}
