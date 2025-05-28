package kr.codingtree.fieldmate;

import kr.codingtree.fieldmate.converter.ValueConverter;
import kr.codingtree.fieldmate.converter.defaults.UUIDConverter;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Iterator;

public class ConverterRegistry {

    public ConverterRegistry() {
        valueConverters.add(new UUIDConverter());
    }

    @Getter
    private final ArrayList<ValueConverter> valueConverters = new ArrayList<>();

    @SneakyThrows
    public boolean registerConverter(Object object) {
        if (object instanceof Class) {
            object = ((Class) object).newInstance();
        }

        if (object instanceof ValueConverter && !hasConverter(object)) {
            valueConverters.add((ValueConverter) object);
            return true;
        }

        return false;
    }

    public boolean unregisterConverter(Object object) {
        if (hasConverter(object)) {
            if (!valueConverters.remove(object)) {
                Iterator<ValueConverter> iterator = valueConverters.iterator();

                while (iterator.hasNext()) {
                    ValueConverter serializer = iterator.next();

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

    public ValueConverter getConverter(Object object) {
        if (object != null) {
            return valueConverters.stream().filter(serializer -> serializer.canSerialize(object)).findFirst().orElse(null);
        }

        return null;
    }

    public boolean hasConverter(Object object) {
        return valueConverters.stream().anyMatch(serializer -> serializer.canSerialize(object));
    }

    public void clearConverters() {
        valueConverters.clear();
    }

}
