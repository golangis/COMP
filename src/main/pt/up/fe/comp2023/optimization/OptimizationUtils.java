package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.Map;

public class OptimizationUtils {
    public static void replaceIfElseWithReachedCode(JmmNode jmmNode, JmmNode reachedCode){
        int ifElseIndex = jmmNode.getIndexOfSelf();

        if (reachedCode.getKind().equals("CodeBlock")){
            for(JmmNode child : reachedCode.getChildren())
                jmmNode.getJmmParent().add(child, child.getIndexOfSelf() + ifElseIndex);
        }
        else
            jmmNode.getJmmParent().add(reachedCode, ifElseIndex);
        jmmNode.delete();
    }

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
