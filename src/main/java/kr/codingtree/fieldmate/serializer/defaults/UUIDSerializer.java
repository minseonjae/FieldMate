package kr.codingtree.fieldmate.serializer.defaults;

import kr.codingtree.fieldmate.serializer.ValueSerializer;

import java.util.UUID;

public class UUIDSerializer extends ValueSerializer<UUID> {

    @Override
    public String serializer(UUID value) {
        return value.toString();
    }

    @Override
    public UUID deserializer(String value) {
        return UUID.fromString(value);
    }

}
