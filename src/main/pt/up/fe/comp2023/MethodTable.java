package pt.up.fe.comp2023;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import java.util.ArrayList;
import java.util.List;

public class MethodTable {
    Symbol name;
    List<Symbol> parameters = new ArrayList<>();
    List<Symbol> localVariables = new ArrayList<>();
    Symbol returnType;

    public Symbol getName() {
        return name;
    }

    public List<Symbol> getParameters() {
        return parameters;
    }

    public List<Symbol> getLocalVariables() {
        return localVariables;
    }

    public Symbol getReturnType() {
        return returnType;
    }
}
