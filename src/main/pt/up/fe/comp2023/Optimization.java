package pt.up.fe.comp2023;

import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.ArrayList;
import java.util.List;


public class Optimization extends AJmmVisitor<Void, Void> implements JmmOptimization {
    String code = "";
    List<Report> reports = new ArrayList<>();
    private SymbolTable table;

   // private class PostOrdOptm extends PostorderJmmVisitor<Void, Void>{}

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        table = semanticsResult.getSymbolTable();
        visit(semanticsResult.getRootNode());
        code += "} \n";
        System.out.println(code);
        return new OllirResult(semanticsResult, code, reports);
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::visitAllChildren);
        addVisit("ClassDecl", this::dealWithClass); // Dealing with imports in here
        addVisit("MethodDecl", this::dealWithMethod);
        addVisit("VoidMethodDecl", this::dealWithMethod);
        addVisit("MainMethodDecl", this::dealWithMainMethod);
        addVisit("MethodDeclParameters", this::dealWithParamDecl);
        addVisit("MethodParameters", this::dealWithMethodCallParam);
        addVisit("VarDecl", this::dealWithVarDecl);
        addVisit("CodeBlock", this::dealWithCodeBlock);
       // addVisit("Condition", this::dealWithCondition); // not for checkpoint 2
       // addVisit("Cycle", this::dealWithCycle); // not for checkpoint 2
        addVisit("Expr", this::dealWithExpr);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("ArrayAssignment", this::dealWithArrayAssignment);
        addVisit("ParenthesesExpr", this::dealWithParenthesesExpr);
        addVisit("NegationExpr", this::dealWithNegationExpr);
        addVisit("ArithmeticExpr", this::dealWithArithmetic);
        addVisit("ComparisonExpr", this::dealWithComparison);
        addVisit("LogicalExpr", this::dealWithLogicalExpr);
        addVisit("ArraySubscript", this::dealWithArraySubscript);
        addVisit("LengthFieldAccess", this::dealWithLenFieldAccess);
        addVisit("MethodCall", this::dealWithMethodCall);
        addVisit("ArrayCreation", this::dealWithArrayCreation);
        addVisit("ObjectCreation", this::dealWithObjectCreation);
        addVisit("Integer", this::dealWithInteger);
        addVisit("Boolean", this::dealWithBoolean);
        addVisit("This", this::dealWithThis);
        addVisit("Identifier", this::dealWithIdentifier);
    }

    private Void dealWithIdentifier(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void dealWithThis(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithBoolean(JmmNode jmmNode, Void unused) {
        code += jmmNode.get("value") + ".bool";
        return null;
    }

    private Void dealWithInteger(JmmNode jmmNode, Void unused) {
        code += jmmNode.get("value") + ".i32";
        return null;
    }

    private Void dealWithObjectCreation(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void dealWithArrayCreation(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithMethodCall(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithLenFieldAccess(JmmNode jmmNode, Void unused) {
        List<Symbol> fieldsOnClass = table.getFields();
        for (Symbol currField: fieldsOnClass){
            code += "\t.field private " + currField.getName() + OllirUtils.ollirTypes(currField.getType()) + ";\n";
        }
        return null;
    }

    private Void dealWithArraySubscript(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithLogicalExpr(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithComparison(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithArithmetic(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithNegationExpr(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithArrayAssignment(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithParenthesesExpr(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithAssignment(JmmNode jmmNode, Void unused) {
        String left = jmmNode.get("varname");
        JmmNode right = jmmNode.getChildren().get(0);
        code += "//" + OllirUtils.ollirTypes(jmmNode.)+ left +  " := ";
        visit(right);
        code += ";\n";
        return null;
    }

    private Void dealWithExpr(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithCodeBlock(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithVarDecl(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithMethodCallParam(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }

    private Void dealWithParamDecl(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren())
            visit(child);

        return null;
    }
    private Void dealWithMethod(JmmNode jmmNode, Void unused) {
        code += "\t.method public " + jmmNode.get("methodname") + "(";

        // Params
        List<Symbol> parameters = table.getParameters(jmmNode.get("methodname"));
        for (int i = 0; i< parameters.size(); i++){
            Symbol parameter = parameters.get(i);
            code += parameter.getName() + OllirUtils.ollirTypes(parameter.getType());
            if ( i + 1 < parameters.size())
                code += ", ";
        }
        code += ")";
        // Return Type of Method
        code += OllirUtils.ollirTypes(table.getReturnType(jmmNode.get("methodname"))) + " {\n";

        for (var child : jmmNode.getChildren())
            visit(child);

        code += "\t}\n";

        return null;
    }


    private Void dealWithMainMethod(JmmNode jmmNode, Void unused) {
        code += "\t.method public static main(" + jmmNode.get("parametername") + ".array.String).V{\n";
        for (var child : jmmNode.getChildren())
            visit(child);
        code += "\t}\n";
        return null;
    }
    private Void dealWithClass(JmmNode jmmNode, Void unused) {
        // Imports
        List<String> imports = table.getImports();
        for (String currImport : imports)
            code  += "import " + currImport + ";\n";

        // Verifies the existence of a superclass
        String superClass = table.getSuper();
        if (superClass == null)
            code += table.getClassName() + " {\n";
        else
            code += table.getClassName() +  " extends " + superClass + "{\n";
        dealWithLenFieldAccess(jmmNode, unused);

        // Constructor
        code += "\t.construct " + table.getClassName() + "().V {\n" + "\t\tinvokespecial(this, \"<init>\").V;\n\t}\n";

        for (var child : jmmNode.getChildren()) // Visit methods, etc..
            visit(child);

        return null;
    }
}
