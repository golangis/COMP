package pt.up.fe.comp2023.optimization;

import java.util.HashMap;
import java.util.Map;

public class OptimizationUtils {
    public static Map<String, String> intersectMaps (Map<String, String> map1, Map<String, String> map2){
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : map1.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (map2.containsKey(key) && map2.get(key).equals(value))
                result.put(key, value);
        }
        return result;
    }
}
