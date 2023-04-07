package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Objects;

public class ExpressionAnalysis extends AJmmVisitor<Type, Type> {
    private String methodName;
    private Analysis analysis;
    public ExpressionAnalysis (String methodName, Analysis analysis){
        this.analysis = analysis;
    }

    @Override
    protected void buildVisitor() {
        addVisit("ParenthesesExpr", this::dealWithParenthesesExpr);
        addVisit("NegationExpr", this::checkBooleanExpression);
        addVisit("BinExpr", this::checkOperandsType);
        addVisit("ArraySubscript", this::dealWithArraySubscript);
        addVisit("LengthFieldAccess", this::dealWithLengthFieldAccess);
        addVisit("MethodCall", this::dealWithMethodCall);
        addVisit("ArrayCreation", this::checkIntegerLength);
        addVisit("ObjectCreation", this::dealWithObjectCreation);
        addVisit("Integer", this::dealWithInteger);
        addVisit("Boolean", this::dealWithBoolean);
        addVisit("This", this::dealWithThis);
        addVisit("Identifier", this::dealWithIdentifier);
    }

    private Type dealWithParenthesesExpr(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type checkBooleanExpression(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type checkOperandsType(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type dealWithArraySubscript(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type dealWithLengthFieldAccess(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type dealWithMethodCall(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type checkIntegerLength(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type dealWithObjectCreation(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type dealWithInteger(JmmNode jmmNode, Type type) {
        jmmNode.put("typename", "int");
        return new Type("int", false);
    }

    private Type dealWithBoolean(JmmNode jmmNode, Type type) {
        jmmNode.put("typename", "boolean");
        return new Type("boolean", false);
    }

    private Type dealWithThis(JmmNode jmmNode, Type type) {
        if (Objects.equals(this.methodName, "main")) {
            String message = "'this' expression cannot be used in a static method.";
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put("typename", "unknown");
            return new Type("unknown", false);
        }

        else {
            String className = analysis.getSymbolTable().getClassName();
            jmmNode.put("typename", className);
            return new Type(className, false);
        }
    }

    private Type dealWithIdentifier(JmmNode jmmNode, Type type) {
        return null;
    }
}