package kr.codingtree.fieldmate.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonStorage extends FileStorage {

    private Gson gson = null;
    private GsonBuilder gsonBuilder = null;

    @Override
    public String mapToString(Map<String, Object> data) {
        if (gsonBuilder == null) {
            gsonBuilder = new GsonBuilder();
        }

        return gsonBuilder.setPrettyPrinting().disableHtmlEscaping().create().toJson(data);
    }

    @Override
    public LinkedHashMap<String, Object> stringToMap(String data) {
        if (gson == null) {
            gson = new Gson();
        }
        return gson.fromJson(data, LinkedHashMap.class);
    }

}
