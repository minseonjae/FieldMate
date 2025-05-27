package kr.codingtree.fieldmate.file;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class FileType {


    @SneakyThrows(IOException.class)
    private void createFile(File file) {
        if (file.getPath().contains("\\")) {
            new File(file.getPath().substring(0, file.getPath().lastIndexOf("\\"))).mkdirs();;
        } else if (file.getPath().contains("/")) {
            new File(file.getPath().substring(0, file.getPath().lastIndexOf("/"))).mkdirs();
        }

        file.createNewFile();
    }

    @SneakyThrows(IOException.class)
    public LinkedHashMap<String, Object> load(File file) {
        if (!file.exists()) {
            createFile(file);
        }

        @Cleanup FileInputStream fis = new FileInputStream(file);
        @Cleanup InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        @Cleanup BufferedReader br = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }

        return stringToMap(sb.toString());
    }

    @SneakyThrows(IOException.class)
    public boolean save(File file, Map<String, Object> data) {
        if (!file.exists()) {
            createFile(file);
        }

        @Cleanup FileOutputStream fos = new FileOutputStream(file);
        @Cleanup OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        @Cleanup BufferedWriter bw = new BufferedWriter(osw);

        bw.write(mapToString(data));

        return true;
    }

    public abstract String mapToString(Map<String, Object> data);
    public abstract LinkedHashMap<String, Object> stringToMap(String data);
}
