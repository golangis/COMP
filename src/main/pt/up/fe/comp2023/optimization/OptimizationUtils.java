package pt.up.fe.comp2023.optimization;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;

import java.util.*;

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

    public static <T> Set<T> differenceSets(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1);
        if(set2 != null)
            result.removeAll(set2);

        return result;
    }

    public static  <T> Set<T> unionSets(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1);
        if(set2 != null)
            result.addAll(set2);

        return result;
    }

    public static boolean isLocalVar(Element element, Method method) {
        HashMap<String, Descriptor> varTable = method.getVarTable();
        String varName = ((Operand)element).getName();

        return isLocalVar(varName, method);
    }

    public static boolean isLocalVar(String identifier, Method method) {
        HashMap<String, Descriptor> varTable = method.getVarTable();
        int firstLocalVarRegister = method.isStaticMethod() ? 0 : 1 + method.getParams().size();

        return varTable.get(identifier).getVirtualReg() >= firstLocalVarRegister;
    }

    public static List<String> getLocalVars(Method method) {
        HashMap<String, Descriptor> varTable = method.getVarTable();
        List<String> localsVars = new ArrayList<>();

        for(Map.Entry<String, Descriptor> entry : varTable.entrySet()){
            String identifier = entry.getKey();
            if(isLocalVar(identifier, method))
                localsVars.add(identifier);
        }
        return localsVars;
    }

    public static String toVarName(Element element){
        return  ((Operand)element).getName();
    }

    public static int methodAccessThis(Method method) {
        return method.isStaticMethod() ? 0 : 1;
    }

    public static int numParams(Method method){
        return method.getParams().size();
    }
}
