package pt.up.fe.comp2023;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import java.util.ArrayList;
import java.util.List;

public class MethodTable {
    String name;
    List<Symbol> parameters = new ArrayList<>();
    List<Symbol> localVariables = new ArrayList<>();
    Type returnType;

    public String getName() {
        return name;
    }

    public List<Symbol> getParameters() {
        return parameters;
    }

    public List<Symbol> getLocalVariables() {
        return localVariables;
    }

    public Type getReturnType() {
        return returnType;
    }
}
