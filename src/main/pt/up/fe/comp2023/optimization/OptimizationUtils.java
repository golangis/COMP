package pt.up.fe.comp2023.optimization;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;

import java.util.HashMap;
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
        if(set2 != null)
            result.addAll(set2);

        return result;
    }

    public static boolean isLocalVar(Element element, Method method) {
        HashMap<String, Descriptor> varTable = method.getVarTable();
        String varName = ((Operand)element).getName();
        int firstLocalVarRegister = method.isStaticMethod() ? 0 : 1 + method.getParams().size();

        return varTable.get(varName).getVirtualReg() >= firstLocalVarRegister;
    }

    public static boolean isLocalVar(String identifier, Method method) {
        HashMap<String, Descriptor> varTable = method.getVarTable();
        int firstLocalVarRegister = method.isStaticMethod() ? 0 : 1 + method.getParams().size();

        return varTable.get(identifier).getVirtualReg() >= firstLocalVarRegister;
    }

    public static Set<String> getLocalVars(Method method) {
        HashMap<String, Descriptor> varTable = method.getVarTable();
        Set<String> localsVars = new HashSet<>();

        for(Map.Entry<String, Descriptor> entry : varTable.entrySet()){
            String identifier = entry.getKey();
            if(isLocalVar(identifier, method))
                localsVars.add(identifier);
        }
        return localsVars;
    }

}
