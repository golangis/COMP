package pt.up.fe.comp2023.semantic;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import java.util.List;

public class MethodTable {

    private final String name;
    private final List<Symbol> parameters;
    private final List<Symbol> localVariables;
    private final Type returnType;

    public MethodTable(String name, List<Symbol> parameters, List<Symbol> localVariables, Type returnType){
        this.name = name;
        this.parameters = parameters;
        this.localVariables = localVariables;
        this.returnType = returnType;
    }

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
