package pt.up.fe.comp2023.ollir;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.optimization.*;

import java.util.ArrayList;
import java.util.List;

public class Optimization extends AJmmVisitor<Void, Void> implements JmmOptimization {
    String code = "";
    String temp;
    List<Report> reports = new ArrayList<>();
    private SymbolTable table;
    int tempVarId = 0;

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        this.table = semanticsResult.getSymbolTable();
        visit(semanticsResult.getRootNode());
        code += "} \n";
        System.out.println(code);
        return new OllirResult(semanticsResult, code, reports);
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        if (Boolean.parseBoolean(semanticsResult.getConfig().get("optimize"))) {
            ConstantPropagation constantPropagation = new ConstantPropagation(semanticsResult);
            ConstantFolding constantFolding = new ConstantFolding(semanticsResult);

            boolean codeModified = constantPropagation.apply() || constantFolding.apply();
            while (codeModified) {
                codeModified = constantPropagation.apply() || constantFolding.apply();
            }
        }
        return semanticsResult;
    }

    public OllirResult optimize(OllirResult ollirResult) {
        int registerAllocationOption = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));

        if (registerAllocationOption >= 0){
            ClassUnit classUnit = ollirResult.getOllirClass();
            for (Method method : classUnit.getMethods())
                new RegisterAllocation(method, registerAllocationOption);
        }
        return ollirResult;
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::visitAllChildren);
        addVisit("ClassDecl", this::dealWithClass); // Dealing with imports in here
        addVisit("MethodDecl", this::dealWithMethod);
        addVisit("VoidMethodDecl", this::dealWithVoidMethod);
        addVisit("MainMethodDecl", this::dealWithMainMethod);
        addVisit("MethodDeclParameters", this::dealWithParamDecl);
        addVisit("MethodParameters", this::dealWithMethodCallParam);
        addVisit("VarDecl", this::dealWithVarDecl);
        addVisit("CodeBlock", this::dealWithCodeBlock);
        addVisit("Condition", this::dealWithCondition);
        addVisit("Cycle", this::dealWithCycle);
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

    private Void dealWithCycle(JmmNode jmmNode, Void unused) {
        code += "\t\t"; visit(jmmNode.getJmmChild(0)); code += "\n";
        var ifId = tempVarId++;

        // Condition statement - negation
        code += "\t\tif (!.bool " +  jmmNode.getJmmChild(0).get("valueOl") + ") goto end_loop" + ifId + ";\n";

        // What occurs if the condition is met
        code += "\t\tloop" + ifId + ":\n\t"; visit(jmmNode.getJmmChild(1));
        code += "\t\t"; visit(jmmNode.getJmmChild(0)); code += "\n";
        code += "\t\t if( " + jmmNode.getJmmChild(0).get("valueOl")  + ") goto loop" + ifId + ";\n";

        // End of If
        code +="\t\tend_loop" + ifId + ":\n\t";


        return null;
    }

    private Void dealWithCondition(JmmNode jmmNode, Void unused) {
        code += "\t\t"; visit(jmmNode.getJmmChild(0));
        var ifId = tempVarId++;

        // Condition statement
        code += "\t\tif (" + jmmNode.getJmmChild(0).get("valueOl") + ") goto if" + ifId + ";\n";

        // What occurs if the condition isn't met
        code += "\t\t\t"; visit(jmmNode.getJmmChild(2));
        code += "\t\t\tgoto endif" + ifId + ";\n";

        // What occurs if the condition is met
        code += "\t\tif" + ifId + ":\n\t"; visit(jmmNode.getJmmChild(1));

        // End of If
        code +="\t\tendif" + ifId + ":\n\t";


        return null;
    }

    private Void dealWithIdentifier(JmmNode jmmNode, Void unused) {
        var method = jmmNode.getAncestor("MethodDecl");
        var voidMethod = jmmNode.getAncestor("VoidMethodDecl");
        var mainMethod = jmmNode.getAncestor("MainMethodDecl");
        Symbol var = null;
        boolean isLocal = false, isParam = false, isField = false;
        int idParam = 0;

        if (method.isPresent() || voidMethod.isPresent() || mainMethod.isPresent()) {
            var methodName = mainMethod.isPresent() ? "main" : method.orElseGet(voidMethod::get).get("methodname");
            List<Symbol> localVarClass = table.getLocalVariables(methodName);
            List<Symbol> paramsOnClass = table.getParameters(methodName);

            // Check if local var
            for (Symbol lv : localVarClass)
                if (lv.getName().equals(jmmNode.get("value"))) {
                    var = lv;
                    isLocal = true;
                }

            // If not local var, then check if param
            if (var == null)
                for (int p = 0; p < paramsOnClass.size(); p++)
                    if (paramsOnClass.get(p).getName().equals(jmmNode.get("value"))) {
                        var = paramsOnClass.get(p);
                        isParam = true;
                        idParam = p + 1;
                    }
        }

        List<Symbol> fields = table.getFields();

        // If not local nor param, check if field
        if (var == null)
            for (Symbol f : fields)
                if (f.getName().equals(jmmNode.get("value"))) {
                    var = f;
                    isField = true;
                }

        // If found, send it with its type
        if (var != null) {
            String ttype = OllirUtils.ollirTypes(var.getType());
            if (isLocal)
                jmmNode.put("valueOl", jmmNode.get("value") + ttype);
            else if (isParam)
                jmmNode.put("valueOl", "$" + idParam + "." + jmmNode.get("value") + ttype);
            else if (isField) {
                temp = "t" + tempVarId++ + ttype;
                jmmNode.put("valueOl", temp);
                code += temp + " :=" + ttype + " getfield(this , " + jmmNode.get("value") + ttype + ")" + ttype + ";";
            }
        }
        // If it's not any of the above, then consider it's in an import
        else
            jmmNode.put("valueOl", jmmNode.get("value"));
        return null;
    }

    private Void dealWithThis(JmmNode jmmNode, Void unused) {
        jmmNode.put("valueOl", "this." + table.getClassName());
        return null;
    }

    private Void dealWithBoolean(JmmNode jmmNode, Void unused) {
        jmmNode.put("valueOl", jmmNode.get("value").equals("true") ? "1.bool" : "0.bool");
        return null;
    }

    private Void dealWithInteger(JmmNode jmmNode, Void unused) {
        jmmNode.put("valueOl", jmmNode.get("value") + ".i32");
        return null;
    }

    private Void dealWithObjectCreation(JmmNode jmmNode, Void unused) {
        String rightString = jmmNode.get("classname");

        temp = "t" + tempVarId++ + "." + rightString;

        code += temp + " :=." + rightString + " new(" + rightString + ")." + rightString + ";\n";
        code += "\t\tinvokespecial(" + temp + ",\"<init>\").V;\n";

        jmmNode.put("valueOl", temp);

        return null;
    }

    private Void dealWithArrayCreation(JmmNode jmmNode, Void unused) {
        visit(jmmNode.getJmmChild(0));
        code += "\t\tt" + tempVarId + ".array.i32 :=.array.i32 new(array, " + jmmNode.getJmmChild(0).get("valueOl") +").array.i32;\n";

        jmmNode.put("valueOl", "t" + tempVarId++ + ".array.i32" );

        return null;
    }

    private Void dealWithMethodCall(JmmNode jmmNode, Void unused) {
        JmmNode left = jmmNode.getJmmChild(0);
        visit(left);
        String methodName = jmmNode.get("methodcall");
        String returnType = ".V";
        boolean isStatic = false;
        boolean makeTemp = !jmmNode.getJmmParent().getKind().equals("Expr");

        for (String m : table.getMethods())
            if (m.equals(methodName))
                returnType = OllirUtils.ollirTypes(table.getReturnType(m));


        JmmNode params = jmmNode.getJmmChild(1);
        for (var child : params.getChildren()) {
            visit(child);
        }

        temp = "t" + tempVarId++ + returnType;
        if (makeTemp)
            code += temp + " :=" + returnType + " ";

        if (left.getKind().equals("This")) {
            code += "invokevirtual(";
        } else {
            if (table.getImports().contains(left.get("valueOl"))) {
                code += "invokestatic(" + left.get("valueOl") + " , \"" + methodName + "\"";  // The first arg is the object that calls the method and the second is the name of the method called
                isStatic = true;
            } else
                code += "invokevirtual(";
        }

        // Case the invocation is not static
        if (!isStatic) {
            code += left.get("valueOl") + " , \"" + methodName + "\"";
        }

        // The following arguments can exist or not they are the arguments of the method called
        for (var child : params.getChildren()) {
            code += " , " + child.get("valueOl");
        }

        code += ")";

        // Type of method
        code += returnType;

        if (makeTemp)
            code += ";\n";

        jmmNode.put("valueOl", temp);

        return null;
    }

    private Void dealWithFieldDeclaration(JmmNode jmmNode, Void unused) {
        List<Symbol> fieldsOnClass = table.getFields();
        for (Symbol currField : fieldsOnClass) {
            code += "\t.field private " + currField.getName() + OllirUtils.ollirTypes(currField.getType()) + ";\n";
        }
        return null;
    }

    private Void dealWithLenFieldAccess(JmmNode jmmNode, Void unused) {
        JmmNode caller = jmmNode.getJmmChild(0);
        visit(caller);
        jmmNode.put("valueOl", "t" + tempVarId + ".i32");
        String caller_name = caller.get("valueOl");
        code += "t" + tempVarId++ + ".i32 :=.i32 arraylength(" + caller_name  + ").i32;\n";
        return null;
    }

    private Void dealWithArraySubscript(JmmNode jmmNode, Void unused) {
        JmmNode left = jmmNode.getJmmChild(0);
        JmmNode right = jmmNode.getJmmChild(1);
        visit(left);
        visit(right);
        String leftS = left.get("valueOl");
        String rightS = right.get("valueOl");
        code += "\t\tt" + tempVarId + ".i32 :=.i32 " + leftS + "[" + rightS + "].i32;\n";
        jmmNode.put("valueOl", "t" + tempVarId++ + ".i32");
        return null;
    }

    private Void dealWithLogicalExpr(JmmNode jmmNode, Void unused) {
        JmmNode leftSon = jmmNode.getJmmChild(0);
        JmmNode rightSon = jmmNode.getJmmChild(1);

        visit(leftSon);
        visit(rightSon);

        String left = leftSon.get("valueOl");
        String right = rightSon.get("valueOl");
        temp = "t" + tempVarId++ + ".bool";

        code += temp + " :=.bool " + left + " " + jmmNode.get("op") + ".bool " + right + ";\n";
        jmmNode.put("valueOl", temp);

        return null;
    }

    private Void dealWithComparison(JmmNode jmmNode, Void unused) {
        JmmNode leftSon = jmmNode.getJmmChild(0);
        JmmNode rightSon = jmmNode.getJmmChild(1);

        visit(leftSon);
        visit(rightSon);

        String left = leftSon.get("valueOl");
        String right = rightSon.get("valueOl");
        temp = "t" + tempVarId++ + ".bool";

        code += temp + " :=.bool " + left + " " + jmmNode.get("op") + ".bool " + right;
        code += ";\n";
        jmmNode.put("valueOl", temp);

        return null;
    }

    private Void dealWithArithmetic(JmmNode jmmNode, Void unused) {
        JmmNode leftSon = jmmNode.getJmmChild(0);
        JmmNode rightSon = jmmNode.getJmmChild(1);

        visit(leftSon);
        visit(rightSon);

        String left = leftSon.get("valueOl");
        String right = rightSon.get("valueOl");
        temp = "t" + tempVarId++ + ".i32";

        code += temp + " :=.i32 " + left + " " + jmmNode.get("op") + ".i32 " + right + ";\n";
        jmmNode.put("valueOl", temp);

        return null;
    }

    private Void dealWithNegationExpr(JmmNode jmmNode, Void unused) {
        JmmNode son = jmmNode.getJmmChild(0);
        visit(son);
        String sonS = son.get("valueOl");
        temp = "t" + tempVarId++ + ".bool";
        code += temp + " :=.bool !.bool " + sonS + ";\n";
        jmmNode.put("valueOl", temp);
        return null;
    }

    private Void dealWithArrayAssignment(JmmNode jmmNode, Void unused) {
        var method = jmmNode.getAncestor("MethodDecl");
        var voidMethod = jmmNode.getAncestor("VoidMethodDecl");
        var mainMethod = jmmNode.getAncestor("MainMethodDecl");
        Symbol var = null;
        boolean isLocal = false, isParam = false, isField = false;
        int idParam = 0;
        String left = jmmNode.get("arrayname");

        if (method.isPresent() || voidMethod.isPresent() || mainMethod.isPresent()) {
            var methodName = mainMethod.isPresent() ? "main" : method.orElseGet(voidMethod::get).get("methodname");
            List<Symbol> localVarClass = table.getLocalVariables(methodName);
            List<Symbol> paramsOnClass = table.getParameters(methodName);

            // Check if local var
            for (Symbol lv : localVarClass)
                if (lv.getName().equals(left)) {
                    var = lv;
                    isLocal = true;
                }

            // If not local var, then check if param
            if (var == null)
                for (int p = 0; p < paramsOnClass.size(); p++)
                    if (paramsOnClass.get(p).getName().equals(left)) {
                        var = paramsOnClass.get(p);
                        isParam = true;
                        idParam = p + 1;
                    }
        }

        List<Symbol> fields = table.getFields();

        // If not local nor param, check if field
        if (var == null)
            for (Symbol f : fields)
                if (f.getName().equals(left)) {
                    var = f;
                    isField = true;
                }

        JmmNode right = jmmNode.getChildren().get(0);
        JmmNode last = jmmNode.getChildren().get(1);
        visit(right);
        visit(last);

        if (isLocal)
            code += "\t\t" + left;
        else if (isParam)
            code += "\t\t$" + idParam + '.' + left;
        else if (isField)
            code += "\t\tt" + tempVarId + ".array.i32 :=.array.i32 getfield(this, " + left + ".array.i32).array.i32;" +
                    "\n\t\tt" + tempVarId++;

        code += "[" + right.get("valueOl") +  "].i32 :=.i32 " + last.get("valueOl") +";\n";

        return null;
    }

    private Void dealWithParenthesesExpr(JmmNode jmmNode, Void unused) {
        visit(jmmNode.getJmmChild(0));
        jmmNode.put("valueOl", jmmNode.getJmmChild(0).get("valueOl"));
        return null;
    }

    private Void dealWithAssignment(JmmNode jmmNode, Void unused) {
        var method = jmmNode.getAncestor("MethodDecl");
        var voidMethod = jmmNode.getAncestor("VoidMethodDecl");
        var mainMethod = jmmNode.getAncestor("MainMethodDecl");
        Symbol var = null;
        boolean isLocal = false, isParam = false, isField = false;
        int idParam = 0;
        String left = jmmNode.get("varname");

        if (method.isPresent() || voidMethod.isPresent() || mainMethod.isPresent()) {
            var methodName = mainMethod.isPresent() ? "main" : method.orElseGet(voidMethod::get).get("methodname");
            List<Symbol> localVarClass = table.getLocalVariables(methodName);
            List<Symbol> paramsOnClass = table.getParameters(methodName);

            // Check if local var
            for (Symbol lv : localVarClass)
                if (lv.getName().equals(left)) {
                    var = lv;
                    isLocal = true;
                }

            // If not local var, then check if param
            if (var == null)
                for (int p = 0; p < paramsOnClass.size(); p++)
                    if (paramsOnClass.get(p).getName().equals(left)) {
                        var = paramsOnClass.get(p);
                        isParam = true;
                        idParam = p + 1;
                    }
        }

        List<Symbol> fields = table.getFields();

        // If not local nor param, check if field
        if (var == null)
            for (Symbol f : fields)
                if (f.getName().equals(left)) {
                    var = f;
                    isField = true;
                }

        JmmNode right = jmmNode.getChildren().get(0);
        visit(right);

        if (isLocal)
            code += "\t\t" + left + OllirUtils.ollirTypes(var.getType()) + " :=" + OllirUtils.ollirTypes(var.getType()) + " ";
        else if (isParam)
            code += "\t\t$" + idParam + "." + left + OllirUtils.ollirTypes(var.getType()) + " :=" + OllirUtils.ollirTypes(var.getType()) + " ";
        else if (isField)
            code += "\t\tputfield(this, " + left + OllirUtils.ollirTypes(var.getType()) + ", ";

        code += right.get("valueOl");
        code += isField ? ").V;\n" : ";\n";
        return null;
    }

    private Void dealWithExpr(JmmNode jmmNode, Void unused) {
        code += "\t\t";
        for (var child : jmmNode.getChildren())
            visit(child);

        code += ";\n";
        System.out.println(jmmNode.getKind());
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
        for (int i = 0; i < parameters.size(); i++) {
            Symbol parameter = parameters.get(i);
            code += parameter.getName() + OllirUtils.ollirTypes(parameter.getType());
            if (i + 1 < parameters.size())
                code += ", ";
        }
        code += ")";

        // Return Type of Method
        String returnType = OllirUtils.ollirTypes(table.getReturnType(jmmNode.get("methodname")));
        code += returnType + " {\n";

        for (int i = 0; i < jmmNode.getChildren().size() - 1; i++)
            visit(jmmNode.getChildren().get(i));

        // Return
        JmmNode returnNode = jmmNode.getJmmChild(jmmNode.getNumChildren() - 1);

        visit(returnNode);  // Visit the expression after "return" keyword
        code += "\t\tret" + returnType + " " + returnNode.get("valueOl") + ";\n\t}\n";
        return null;
    }

    private Void dealWithVoidMethod(JmmNode jmmNode, Void unused) {
        code += "\t.method public " + jmmNode.get("methodname") + "(";
        // Parameters
        List<Symbol> parameters = table.getParameters(jmmNode.get("methodname"));
        for (int i = 0; i < parameters.size(); i++) {
            Symbol parameter = parameters.get(i);
            code += parameter.getName() + OllirUtils.ollirTypes(parameter.getType());
            if (i + 1 < parameters.size())
                code += ", ";
        }
        code += ")";

        code += ".V{\n";

        for (int i = 0; i < jmmNode.getChildren().size(); i++)
            visit(jmmNode.getChildren().get(i));

        code += "\t\tret.V;\n\t}\n";
        return null;
    }


    private Void dealWithMainMethod(JmmNode jmmNode, Void unused) {
        code += "\t.method public static main(" + jmmNode.get("parametername") + ".array.String).V{\n";
        for (var child : jmmNode.getChildren())
            visit(child);
        code += "\t\tret.V;\n\t}\n";
        return null;
    }

    private Void dealWithClass(JmmNode jmmNode, Void unused) {
        // Imports
        List<String> imports = table.getImports();
        for (String currImport : imports)
            code += "import " + currImport + ";\n";

        // Verifies the existence of a superclass
        String superClass = table.getSuper();
        if (superClass == null)
            code += table.getClassName() + " {\n";
        else
            code += table.getClassName() + " extends " + superClass + "{\n";
        dealWithFieldDeclaration(jmmNode, unused);

        // Constructor
        code += "\t.construct " + table.getClassName() + "().V {\n" + "\t\tinvokespecial(this, \"<init>\").V;\n\t}\n";

        for (var child : jmmNode.getChildren()) // Visit methods, etc..
            visit(child);

        return null;
    }
}
