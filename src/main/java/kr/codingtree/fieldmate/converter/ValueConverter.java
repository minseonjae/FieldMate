package kr.codingtree.fieldmate.converter;

import java.lang.reflect.ParameterizedType;

public abstract class ValueConverter<V> {

    public abstract String serialize(V value);
    public abstract V deserialize(String value);

    public final Class<?> getGenericClass() {
        return (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public final boolean canSerialize(Object object) {
        Class<?> clazz = getGenericClass();
        return (object instanceof Class && object.equals(clazz)) || object.getClass().equals(clazz);
    }

}
