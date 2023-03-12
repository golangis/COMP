package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySymbolTable extends AJmmVisitor<Void, Void> implements SymbolTable {

    private List<String> imports = new ArrayList<>();
    private String className;
    private String superClass;
    private List<Symbol> fields = new ArrayList<>();
    private List<String> methods = new ArrayList<>();
    Map<String, MethodTable> methodTables = new HashMap<>();

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
        addVisit ("MethodDecl", this::dealWithMethod);
        addVisit("VoidMethodDecl", this::dealWithVoidMethod);
        addVisit("MainMethodDecl", this::dealWithMainMethod);
    }

    private Void setDefaultVisit(JmmNode jmmNode, Void unused) {
        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void dealWithMethod(JmmNode jmmNode, Void unused) {
        String name = jmmNode.get("methodname");
        List<Symbol> parameters = dealWithMethodDeclarationParameters(jmmNode.getJmmChild(1));
        List<Symbol> localVariables = dealWithLocalVars(jmmNode.getChildren());
        Type returnType = dealWithType(jmmNode.getJmmChild(0));

        MethodTable method = new MethodTable();
        method.setName(name);
        method.setParameters(parameters);
        method.setLocalVariables(localVariables);
        method.setReturnType(returnType);

        methods.add(name);
        methodTables.put(name, method);
        return null;
    }

    private Void dealWithVoidMethod(JmmNode jmmNode, Void unused) {
        //TODO
        return null;
    }

    private Void dealWithMainMethod(JmmNode jmmNode, Void unused) {
        //TODO
        return null;
    }

    private Type dealWithType(JmmNode jmmNode) {
        String name = jmmNode.get("typename");
        boolean isArray = jmmNode.getKind().equals("TypeArray");

        return new Type(name, isArray);
    }

    private List<Symbol> dealWithMethodDeclarationParameters(JmmNode jmmNode) {
        List<Symbol> parameters = new ArrayList<>();
        String parameterNames = jmmNode.get("parametername").replaceAll("[\\[\\]\\s+]", "");
        String[] names = parameterNames.split(",");

        for ( int child = 0; child < jmmNode.getNumChildren(); child++){
            String name = names[child];
            Type type = dealWithType(jmmNode.getJmmChild(child));
            Symbol parameter = new Symbol(type, name);
            parameters.add(parameter);
        }
        return parameters;
    }

    private List<Symbol> dealWithLocalVars(List<JmmNode> children) {
        List<Symbol> variables = new ArrayList<>();

        for (JmmNode child: children) {
            if(child.getKind().equals("VarDecl"))
                variables.add(dealWithVarDeclaration(child));
        }
        return variables;
    }

    private Symbol dealWithVarDeclaration(JmmNode jmmNode) {
        String name = jmmNode.get("varname");
        Type type = dealWithType(jmmNode.getJmmChild(0));

        return new Symbol(type, name);
    }
}
