package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;
import java.util.Objects;

public class SemanticUtils {
    //Types
    public static final Type ARRAY_TYPE = new Type("int", true);
    public static final Type BOOLEAN_TYPE = new Type("boolean", false);
    public static final Type INT_TYPE = new Type("int", false);
    public static final Type UNKNOWN_TYPE = new Type("#unknown", false);
    public static final Type UNDEFINED_TYPE = new Type("#undefined", false);

    //Type names
    public static final String ARRAY = ARRAY_TYPE.print();
    public static final String BOOLEAN = BOOLEAN_TYPE.print();
    public static final String INT = INT_TYPE.print();
    public static final String UNKNOWN = UNKNOWN_TYPE.print();
    public static final String UNDEFINED = UNDEFINED_TYPE.print();

    //Typename attribute
    public static final String TYPENAME = "typename";

    public static boolean findImport(List<String> imports, String className) {
        for(String imported : imports){
            List<String> splitImport = List.of(imported.split("\\."));
            if (Objects.equals(splitImport.get(splitImport.size() - 1), className))
                return true;
        }
        return false;
    }

    public static Type getIdentifierType(String methodName, String identifier, MySymbolTable symbolTable){
        List<Symbol> localVariables = symbolTable.getLocalVariables(methodName);
        for (Symbol localVariable : localVariables) {
            if(Objects.equals(localVariable.getName(), identifier))
                return localVariable.getType();
        }

        List<Symbol> parameters = symbolTable.getParameters(methodName);
        for (Symbol parameter : parameters) {
            if(Objects.equals(parameter.getName(), identifier))
                return parameter.getType();
        }

        List<Symbol> fields = symbolTable.getFields();
        for (Symbol field : fields) {
            if(Objects.equals(field.getName(), identifier))
                return field.getType();
        }

        if(findImport(symbolTable.getImports(), identifier))
            return new Type(identifier, false);
        return UNKNOWN_TYPE;
    }
}
