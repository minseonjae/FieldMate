package kr.codingtree.fieldmate.converter;

import kr.codingtree.fieldmate.annotation.ExcludeField;
import kr.codingtree.fieldmate.annotation.FieldName;
import kr.codingtree.fieldmate.file.FileStorage;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;

@AllArgsConstructor
public class ClassConverter {

    private final FieldConverter converter;

    public void load(File file, Class<? extends FileStorage> fileType, Object loadClass) {
        try {
            load(file, fileType.newInstance(), loadClass);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows(Exception.class)
    public void load(File file, FileStorage fileType, Object loadClass) {
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

                Object value = converter.deserialize(field, field.get(loadClass), map.get(name));

                if (value != null) {
                    field.set(loadClass, value);
                }
            }
        }
    }

    public void save(File file, Class<? extends FileStorage> fileType, Object saveClass) {
        try {
            save(file, fileType.newInstance(), saveClass);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows(Exception.class)
    public void save(File file, FileStorage fileType, Object saveClass) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        for (Field field : saveClass.getClass().getDeclaredFields()) {
            if (field.getAnnotation(ExcludeField.class) == null) {
                field.setAccessible(true);

                Object value = converter.serialize(field, field.get(saveClass));

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

}
