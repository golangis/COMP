package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

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

    private Type dealWithArrayCreation(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type dealWithObjectCreation(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type dealWithInteger(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type dealWithBoolean(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type dealWithThis(JmmNode jmmNode, Type type) {
        return null;
    }

    private Type dealWithIdentifier(JmmNode jmmNode, Type type) {
        return null;
    }
}