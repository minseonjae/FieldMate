package kr.codingtree.fieldmate.converter.defaults;

import kr.codingtree.fieldmate.converter.ValueConverter;

import java.util.UUID;

public class UUIDConverter extends ValueConverter<UUID> {

    @Override
    public String serialize(UUID value) {
        return value.toString();
    }

    @Override
    public UUID deserialize(String value) {
        return UUID.fromString(value);
    }

}
