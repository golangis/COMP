package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.Map;

public class MySymbolTable extends AJmmVisitor<Void, Void> implements SymbolTable {

    private List<String> imports;
    private String className;
    private String superClass;
    private List<Symbol> fields;
    private List<String> methods;
    Map<String, MethodTable> methodTables;

    public MySymbolTable(JmmNode jmmNode) {
        this.visit(jmmNode);
    }

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
    public Type getReturnType(String methodSignature) {
       return this.methodTables.get(methodSignature).getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return this.methodTables.get(methodSignature).getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return this.methodTables.get(methodSignature).getLocalVariables();
    }

    @Override
    protected void buildVisitor() {
        System.out.println("This is the visitor");
        setDefaultVisit(this::setDefaultVisit);
    }

    private Void setDefaultVisit(JmmNode jmmNode, Void unused) {
        return null;
    }
}
