package kr.codingtree.fieldmate;

import kr.codingtree.fieldmate.converter.ClassConverter;
import kr.codingtree.fieldmate.converter.FieldConverter;
import kr.codingtree.fieldmate.converter.ValueConverter;
import kr.codingtree.fieldmate.file.FileStorage;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * FieldMate is a utility class that simplifies serialization/deserialization between objects and files.
 * It supports conversion between various file formats (JSON, YAML, etc.) and objects, providing field name mapping and exclusion capabilities.
 * Custom type converters can be registered to handle complex object structures.
 *
 * @see kr.codingtree.fieldmate.annotation.FieldName
 * @see kr.codingtree.fieldmate.annotation.ExcludeField
 * @see kr.codingtree.fieldmate.file.FileStorage
 */
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
     * Loads field values into the specified object using a string path and storage class type.
     * This method automatically creates a new instance of the storage class.
     * Field mapping is based on field names and can be customized using the {@link kr.codingtree.fieldmate.annotation.FieldName} annotation.
     * Fields with the {@link kr.codingtree.fieldmate.annotation.ExcludeField} annotation are ignored.
     *
     * @param path      The path of the file to read data from
     * @param fileType  The class of the file format handler (e.g., JsonStorage.class)
     * @param loadClass The target object to load data into
     * @throws Exception If an error occurs during file reading or field setting
     */
    public void load(String path, Class<? extends FileStorage> fileType, Object loadClass) {
        load(new File(path), fileType, loadClass);
    }

    /**
     * Loads field values into the specified object using a file path and storage class type.
     * This method automatically creates a new instance of the storage class.
     * Field mapping is based on field names and can be customized using the {@link kr.codingtree.fieldmate.annotation.FieldName} annotation.
     * Fields with the {@link kr.codingtree.fieldmate.annotation.ExcludeField} annotation are ignored.
     *
     * @param file      The file to read data from
     * @param fileType  The class of the file format handler (e.g., JsonStorage.class)
     * @param loadClass The target object to load data into
     * @throws Exception If an error occurs during file reading or field setting
     */
    public void load(File file, Class<? extends FileStorage> fileType, Object loadClass) {
        classConverter.load(file, fileType, loadClass);
    }

    /**
     * Loads data from a file and sets field values on the specified object.
     * Field mapping is based on field names and can be customized using the {@link kr.codingtree.fieldmate.annotation.FieldName} annotation.
     * Fields with the {@link kr.codingtree.fieldmate.annotation.ExcludeField} annotation are ignored.
     *
     * @param path      The Path of the file to read data from
     * @param fileType  The file format handler
     * @param loadClass The target object to load data into
     * @throws Exception If an error occurs during file reading or field setting
     */
    public void load(String path, FileStorage fileType, Object loadClass) {
        load(new File(path), fileType, loadClass);
    }

    /**
     * Loads data from a file and sets field values on the specified object.
     * Field mapping is based on field names and can be customized using the {@link kr.codingtree.fieldmate.annotation.FieldName} annotation.
     * Fields with the {@link kr.codingtree.fieldmate.annotation.ExcludeField} annotation are ignored.
     *
     * @param file      The file to read data from
     * @param fileType  The file format handler
     * @param loadClass The target object to load data into
     * @throws Exception If an error occurs during file reading or field setting
     */
    @SneakyThrows(Exception.class)
    public void load(File file, FileStorage fileType, Object loadClass) {
        classConverter.load(file, fileType, loadClass);
    }

    /**
     * Saves field values from the specified object to a file at the given string path using the specified storage class type.
     * This method automatically creates a new instance of the storage class.
     * Field mapping is based on field names and can be customized using the {@link kr.codingtree.fieldmate.annotation.FieldName} annotation.
     * Fields with the {@link kr.codingtree.fieldmate.annotation.ExcludeField} annotation are ignored.
     *
     * @param path      The string path of the file to save data to
     * @param fileType  The class of the file format handler (e.g., JsonStorage.class)
     * @param saveClass The source object to save data from
     * @throws Exception If an error occurs during field access or file writing
     */
    public void save(String path, Class<? extends FileStorage> fileType, Object saveClass) {
        save(new File(path), fileType, saveClass);
    }

    /**
     * Saves field values from the specified object to a file using the specified storage class type.
     * This method automatically creates a new instance of the storage class.
     * Field mapping is based on field names and can be customized using the {@link kr.codingtree.fieldmate.annotation.FieldName} annotation.
     * Fields with the {@link kr.codingtree.fieldmate.annotation.ExcludeField} annotation are ignored.
     *
     * @param file      The file to save data to
     * @param fileType  The class of the file format handler (e.g., JsonStorage.class)
     * @param saveClass The source object to save data from
     * @throws Exception If an error occurs during field access or file writing
     */
    public void save(File file, Class<? extends FileStorage> fileType, Object saveClass) {
        classConverter.save(file, fileType, saveClass);
    }

    /**
     * Saves field values from the specified object to a file at the given string path.
     * This method is a convenience wrapper around the File-based save method.
     * Field mapping is based on field names and can be customized using the {@link kr.codingtree.fieldmate.annotation.FieldName} annotation.
     * Fields with the {@link kr.codingtree.fieldmate.annotation.ExcludeField} annotation are ignored.
     *
     * @param path      The string path of the file to save data to
     * @param fileType  The file format handler instance
     * @param saveClass The source object to save data from
     * @throws Exception If an error occurs during field access or file writing
     */
    public void save(String path, FileStorage fileType, Object saveClass) {
        save(new File(path), fileType, saveClass);
    }

    /**
     * Saves field values from the specified object to a file.
     * Field mapping is based on field names and can be customized using the {@link kr.codingtree.fieldmate.annotation.FieldName} annotation.
     * Fields with the {@link kr.codingtree.fieldmate.annotation.ExcludeField} annotation are ignored.
     *
     * @param file      The file to save data to
     * @param fileType  The file format handler
     * @param saveClass The source object to save data from
     * @throws Exception If an error occurs during field access or file writing
     */
    @SneakyThrows(Exception.class)
    public void save(File file, FileStorage fileType, Object saveClass) {
        classConverter.save(file, fileType, saveClass);
    }

}
