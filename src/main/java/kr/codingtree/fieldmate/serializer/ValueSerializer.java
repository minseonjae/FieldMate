package kr.codingtree.fieldmate.serializer;

import java.lang.reflect.ParameterizedType;

public abstract class ValueSerializer<V> {

    public abstract String serializer(V value);
    public abstract V deserializer(String value);

    public final Class getGenericClass() {
        return (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public final boolean equals(Object object) {
        Class clazz = getGenericClass();
        return (object instanceof Class && object.equals(clazz)) || object.getClass().equals(clazz);
    }

}
