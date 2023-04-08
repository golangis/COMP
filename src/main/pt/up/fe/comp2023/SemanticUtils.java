package pt.up.fe.comp2023;

import java.util.List;
import java.util.Objects;

public class SemanticUtils {
    public static boolean findImport(List<String> imports, String className) {
        for(String imported : imports){
            List<String> splitImport = List.of(imported.split("\\."));
            if (Objects.equals(splitImport.get(splitImport.size() - 1), className))
                return true;
        }
        return false;
    }
}
