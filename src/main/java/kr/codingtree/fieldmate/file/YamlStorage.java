package kr.codingtree.fieldmate.file;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

public class YamlStorage extends FileStorage {

    private DumperOptions dumperOptions = null;
    private Constructor constructor = null;
    private Representer representer = null;


    private Yaml getYaml() {
        if (constructor == null) {
            constructor = new Constructor(new LoaderOptions());
        }

        if (dumperOptions == null) {
            dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        }

        if (representer == null) {
            representer = new Representer(dumperOptions);
        }

        return new Yaml(constructor, representer, dumperOptions);
    }

    @Override
    public String mapToString(Map<String, Object> data) {
        return getYaml().dump(data);
    }

    @Override
    public LinkedHashMap<String, Object> stringToMap(String data) {
        return getYaml().load(data);
    }
}
