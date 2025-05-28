package kr.codingtree.fieldmate;

import kr.codingtree.fieldmate.converter.ClassConverter;
import kr.codingtree.fieldmate.converter.FieldConverter;
import kr.codingtree.fieldmate.converter.ValueConverter;
import kr.codingtree.fieldmate.file.FileStorage;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class FieldMate {

    private final ConverterRegistry registry = new ConverterRegistry();
    private final FieldConverter fieldConverter = new FieldConverter(registry);
    private final ClassConverter classConverter = new ClassConverter(fieldConverter);

    /**
     * Registers a custom converter.
     * The converter must be an implementation of {@link ValueConverter}.
     *
     * @param object The converter object or converter class to register
     * @return Whether the registration was successful
     */
    public boolean registerConverter(Object object) {
        return registry.registerConverter(object);
    }

    /**
     * Unregisters a previously registered converter.
     *
     * @param object The converter object or converter class to unregister
     * @return Whether the unregistration was successful
     */
    public boolean unregisterConverter(Object object) {
        return registry.unregisterConverter(object);
    }

    /**
     * Returns a converter for the specified object type.
     *
     * @param object The object or class to find a converter for
     * @return The found converter, or null if none exists
     */
    public ValueConverter getConverter(Object object) {
        return registry.getConverter(object);
    }

    /**
     * Checks if a converter is registered for the specified object type.
     *
     * @param object The object or class to check
     * @return Whether a converter exists
     */
    public boolean hasConverter(Object object) {
        return registry.hasConverter(object);
    }

    /**
     * Removes all registered converters.
     */
    public void clearConverters() {
        registry.clearConverters();
    }

    /**
     * Loads data from a file and sets field values on the specified object.
     * Field mapping is based on field names and can be customized using the {@link kr.codingtree.fieldmate.annotation.FieldName} annotation.
     * Fields with the {@link kr.codingtree.fieldmate.annotation.ExcludeField} annotation are ignored.
     *
     * @param file The file to read data from
     * @param fileType The file format handler
     * @param loadClass The target object to load data into
     * @throws Exception If an error occurs during file reading or field setting
     */
    @SneakyThrows(Exception.class)
    public void load(File file, FileStorage fileType, Object loadClass) {
        classConverter.load(file, fileType, loadClass);
    }

    /**
     * Saves field values from the specified object to a file.
     * Field mapping is based on field names and can be customized using the {@link kr.codingtree.fieldmate.annotation.FieldName} annotation.
     * Fields with the {@link kr.codingtree.fieldmate.annotation.ExcludeField} annotation are ignored.
     *
     * @param file The file to save data to
     * @param fileType The file format handler
     * @param saveClass The source object to save data from
     * @throws Exception If an error occurs during field access or file writing
     */
    @SneakyThrows(Exception.class)
    public void save(File file, FileStorage fileType, Object saveClass) {
        classConverter.save(file, fileType, saveClass);
    }

}
