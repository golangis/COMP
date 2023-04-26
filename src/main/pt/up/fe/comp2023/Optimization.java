package pt.up.fe.comp2023;

import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
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
        var method = jmmNode.getAncestor("MethodDecl");
        Symbol var = null;

        if (method.isPresent()) {
            List<Symbol> localVarClass = table.getLocalVariables(method.get().get("methodname"));
            List<Symbol> paramsOnClass = table.getParameters(method.get().get("methodname"));

            // Check if local var
            for (Symbol lv : localVarClass)
                if (lv.getName().equals(jmmNode.get("value")))
                    var = lv;

            // If not local var, then check if param
            if (var == null)
                for (Symbol p : paramsOnClass)
                    if (p.getName().equals(jmmNode.get("value")))
                        var = p;


        }

        List<Symbol> fields = table.getFields();
        // If not local nor param, check if field
        if (var == null)
            for (Symbol f : fields)
                if (f.getName().equals(jmmNode.get("value")))
                    var = f;
        // If found, send it with its type
        if (var != null)
            code += jmmNode.get("value") + OllirUtils.ollirTypes(var.getType());
        // If it's not any of the above, then consider it's in an import
        else
            code += jmmNode.get("value");
        return null;
    }

    private Void dealWithThis(JmmNode jmmNode, Void unused) {
        code += "this";
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
        JmmNode father = jmmNode.getJmmParent(); // see the father to see the whole expression
        String leftString = "", rightString = "";

        if (father.getKind().equals("Assignment")){ // or ARRAY ASSIGNMENT (LATER IMPLEMENT)
            leftString = father.get("varname");
            rightString = jmmNode.get("classname");
        }

        code += "new(" + rightString + ")." + rightString + ";\n";
        code += "\t\tinvokespecial(" + leftString + "." + rightString + ",\"<init>\").V";


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
        JmmNode left =  jmmNode.getJmmChild(0);
        String methodName = jmmNode.get("methodcall");
        String returnType = ".V";
        boolean isStatic = false;

        if (table.getMethods().contains(methodName))
            for (String m : table.getMethods())
                if (m.equals(methodName))
                    returnType = OllirUtils.ollirTypes(table.getReturnType(m));
                else
                    returnType = ".V";

        if (left.getKind().equals("This")){
            code += "\t\tinvokevirtual(";

            }
        else {
            if (table.getImports().contains(left.get("value"))) {
                code += "\t\tinvokestatic(" + left.get("value") + " , \"" + methodName + "\"";  // The first arg is the object that calls the method and the second is the name of the method called
                isStatic = true;
            }
            else
                code += "\t\tinvokevirtual(" ;
        }

        // Case the invocation is not static
        if (!isStatic){
            code += left.get("value") + OllirUtils.ollirTypes(new Type(left.get("typename"), false))+ " , \"" + methodName + "\"";
        }

        // The following arguments can exist or not they are the arguments of the method called
        JmmNode params = jmmNode.getJmmChild(1);
        for (var child : params.getChildren()) {
            code += " , ";
            visit(child);
        }

        code += ")";

        // Type of method
        code += returnType + ";\n";


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
        Symbol var = null;
        boolean isField = false;

        /*
            Grammar distributes method declarations into 3 categories:
                - main method;
                - void method;
                - method with return type

        Because of that, it's needed to check if the parent of the assignment function corresponds
        to any of these types of methods to check if it is either a local variable or a parameter.
        */

        // Main Method
        if (jmmNode.getJmmParent().getKind().equals("MainMethodDecl")){
            // Local Var
            for (Symbol v : table.getLocalVariables("main"))
                if (v.getName().equals(jmmNode.get("varname")))
                    var = v;

        }
        // Void Method OR Method with Return Type
        else if (jmmNode.getJmmParent().getKind().equals("VoidMethodDecl") || jmmNode.getJmmParent().getKind().equals("MethodDecl")) {
            // Local Var
            for (Symbol v : table.getLocalVariables(jmmNode.getJmmParent().get("methodname")))
                if (v.getName().equals(jmmNode.get("varname")))
                    var = v;
            // Parameter
            if (var == null)
                for (Symbol v : table.getParameters(jmmNode.getJmmParent().get("methodname")))
                    if (v.getName().equals(jmmNode.get("varname")))
                        var = v;
        }
        // Field
        if (var == null)
            for (Symbol s : this.table.getFields())
                if (s.getName().equals(jmmNode.get("varname"))) {
                    var = s;
                    isField = true;
                }
        // Throw error
        if (var == null)
            throw new NullPointerException("Variable 'var' is not a LOCAL VAR or a PARAMETER or a FIELD");


        String left = jmmNode.get("varname");
        JmmNode right = jmmNode.getChildren().get(0);
        code += "\t\t" + left + OllirUtils.ollirTypes(var.getType()) + " :=" + OllirUtils.ollirTypes(var.getType()) + " ";
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
        // Parameters
        List<Symbol> parameters = table.getParameters(jmmNode.get("methodname"));
        for (int i = 0; i< parameters.size(); i++){
            Symbol parameter = parameters.get(i);
            code += parameter.getName() + OllirUtils.ollirTypes(parameter.getType());
            if ( i + 1 < parameters.size())
                code += ", ";
        }
        code += ")";
        // Return Type of Method
        String returnType = OllirUtils.ollirTypes(table.getReturnType(jmmNode.get("methodname")));
        code += returnType + " {\n";

        for (int i = 0; i < jmmNode.getChildren().size() - 1; i++ )
            visit(jmmNode.getChildren().get(i));

        // Return
        int indexReturn = jmmNode.getChildren().size() - 1;

        code += "\t\tret" + returnType + " ";
        visit(jmmNode.getChildren().get(indexReturn));  // Visit the expression after "return" keyword
        code += ";\n\t}\n";
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
