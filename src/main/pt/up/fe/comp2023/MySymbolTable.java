package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class MySymbolTable implements SymbolTable {

    private List<String> imports;
    private String className;
    private String superClass;
    private List<Symbol> fields;
    private List<String> methods;
    // TODO: add field methodSignatures

    @Override
    public List<String> getImports() {
        return this.imports;
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public String getSuper() {
        return this.superClass;
    }

    @Override
    public List<Symbol> getFields() {
        return this.fields;
    }

    @Override
    public List<String> getMethods() {
        return this.methods;
    }

    @Override
    // TODO: implement after having methodSignatures
    public Type getReturnType(String methodSignature) {
        return null;
    }

    @Override
    // TODO: implement after having methodSignatures
    public List<Symbol> getParameters(String methodSignature) {
        return null;
    }

    @Override
    // TODO: implement after having methodSignatures
    public List<Symbol> getLocalVariables(String methodSignature) {
        return null;
    }
}
