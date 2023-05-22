package pt.up.fe.comp2023.optimization;

import org.specs.comp.ollir.Element;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OptimizationUtils {
    public static void intersectMaps (Map<String, String> map1, Map<String, String> map2, Map<String, String> result){
        result.clear();

        for (Map.Entry<String, String> entry : map1.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (map2.containsKey(key) && map2.get(key).equals(value))
                result.put(key, value);
        }
    }

    public static Set<Element> differenceSets (Set<Element> set1, Set<Element> set2){
        Set<Element> result = new HashSet<>(set1);
        result.removeAll(set2);

        return result;
    }

    public static Set<Element> unionSets (Set<Element> set1, Set<Element> set2){
        Set<Element> result = new HashSet<>(set1);
        result.addAll(set2);

        return result;
    }
}
