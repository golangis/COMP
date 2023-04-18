package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;
import java.util.Objects;

import static pt.up.fe.comp2023.SemanticUtils.*;

public class ExpressionAnalysis extends AJmmVisitor<Type, Type> {
    private String methodName;
    private final MySymbolTable symbolTable;
    private final List<Report> reports;
    private final String className;
    private final String superClass;
    private final List<String> imports;
    public ExpressionAnalysis (String methodName, MySymbolTable symbolTable, List<Report> reports){
        this.methodName = methodName;
        this.symbolTable = symbolTable;
        this.reports = reports;
        this.className = this.symbolTable.getClassName();
        this.superClass = this.symbolTable.getSuper();
        this.imports = this.symbolTable.getImports();
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    protected void buildVisitor() {
        addVisit("ParenthesesExpr", this::dealWithParenthesesExpr);
        addVisit("NegationExpr", this::checkBooleanExpression);
        addVisit("ArithmeticExpr", this::checkIntegerOperands);
        addVisit("ComparisonExpr", this::checkIntegerOperands);
        addVisit("LogicalExpr", this::checkBooleanOperands);
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
        Type expressionType = visit(jmmNode.getJmmChild(0));
        jmmNode.put(TYPENAME, expressionType.print());
        return expressionType;
    }

    private Type checkBooleanExpression(JmmNode jmmNode, Type type) {
        JmmNode expressionNode = jmmNode.getJmmChild(0);
        Type expressionType = visit(expressionNode);

        if(!expressionType.equals(BOOLEAN_TYPE)){
            String message = "Expected expression of type '" +  BOOLEAN + "' but found '" + expressionType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(expressionNode), getNodeColumn(expressionNode), message));
        }

        jmmNode.put(TYPENAME, BOOLEAN);
        return BOOLEAN_TYPE;
    }

    private Type checkIntegerOperands(JmmNode jmmNode, Type type) {
        JmmNode leftNode = jmmNode.getJmmChild(0);
        JmmNode rightNode = jmmNode.getJmmChild(1);
        Type leftOperandType = visit(leftNode);
        Type rightOperandType = visit(rightNode);

        if(!leftOperandType.equals(INT_TYPE)) {
            String message = "Expected operand of type '" + INT + "' but found '" + leftOperandType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(leftNode), getNodeColumn(leftNode), message));
        }
        if(!rightOperandType.equals(INT_TYPE)) {
            String message = "Expected operand of type '" + INT + "' but found '" + leftOperandType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(rightNode), getNodeColumn(rightNode), message));
        }

        if(Objects.equals(jmmNode.getKind(), "ArithmeticExpr")) {
            jmmNode.put(TYPENAME, INT);
            return INT_TYPE;
        }
        else {
            jmmNode.put(TYPENAME, BOOLEAN);
            return BOOLEAN_TYPE;
        }
    }

    private Type checkBooleanOperands(JmmNode jmmNode, Type type) {
        JmmNode leftNode = jmmNode.getJmmChild(0);
        JmmNode rightNode = jmmNode.getJmmChild(1);
        Type leftOperandType = visit(leftNode);
        Type rightOperandType = visit(rightNode);

        if(!leftOperandType.equals(BOOLEAN_TYPE)) {
            String message = "Expected operand of type '" + BOOLEAN + "' but found '" + leftOperandType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(leftNode), getNodeColumn(leftNode), message));
        }

        if(!rightOperandType.equals(BOOLEAN_TYPE)) {
            String message = "Expected operand of type '" + BOOLEAN + "' but found '" + leftOperandType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(rightNode), getNodeColumn(rightNode), message));
        }

        jmmNode.put(TYPENAME, BOOLEAN);
        return BOOLEAN_TYPE;
    }

    private Type dealWithArraySubscript(JmmNode jmmNode, Type type) {
        JmmNode variableNode = jmmNode.getJmmChild(0);
        JmmNode indexNode = jmmNode.getJmmChild(1);
        Type variableType = visit(variableNode);
        Type indexType = visit(indexNode);

        if (!variableType.equals(ARRAY_TYPE)) {
            String message = "Expected '" + ARRAY_TYPE + "' type but found '" + variableType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(variableNode), getNodeColumn(variableNode), message));
        }

        if (!indexType.equals(INT_TYPE)) {
            String message = "Expected index expression of type '" + INT +"' but found '" + indexType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(indexNode), getNodeColumn(indexNode), message));
        }

        jmmNode.put(TYPENAME, INT);
        return INT_TYPE;
    }

    private Type dealWithLengthFieldAccess(JmmNode jmmNode, Type type) {
        Type expressionType = visit(jmmNode.getJmmChild(0));

        if(!expressionType.equals(ARRAY_TYPE)){
            String message = "Cannot resolve symbol 'length'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(jmmNode), getNodeColumn(jmmNode), message));
        }

        jmmNode.put(TYPENAME, INT);
        return INT_TYPE;
    }

    private Type dealWithMethodCall(JmmNode jmmNode, Type type) {
        String expressionType = visit(jmmNode.getJmmChild(0)).print();
        String method = jmmNode.get("methodcall");

        if(this.symbolTable.getMethods().contains(method)){
            if(!Objects.equals(expressionType, this.className) && !Objects.equals(expressionType, this.superClass)){
                String message = "Expected expression of type '" + this.className + "' or '" + this.superClass + "' but found '" + expressionType + "'.";
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(jmmNode), getNodeColumn(jmmNode), message));
            }
            this.verifyArgumentTypes(jmmNode.getJmmChild(1), method);
            jmmNode.put(TYPENAME, this.symbolTable.getReturnType(method).print());
            return this.symbolTable.getReturnType(method);
        }

        else if(Objects.equals(expressionType, this.className) || Objects.equals(expressionType, this.superClass)){
            if(!findImport(this.imports, this.superClass)){
                String message = "Cannot find super class '" + this.superClass + "'.";
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(jmmNode), getNodeColumn(jmmNode), message));
            }
        }

        else if(!findImport(this.imports, expressionType)){
            String message = "'" + expressionType + "' is not declared.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(jmmNode), getNodeColumn(jmmNode), message));
        }
        jmmNode.put(TYPENAME, UNDEFINED);
        return UNDEFINED_TYPE;
    }

    private void verifyArgumentTypes(JmmNode jmmNode, String method) {
        int numDeclaredParams = symbolTable.getParameters(method).size();
        int numCallParams = jmmNode.getNumChildren();

        if(numDeclaredParams != numCallParams){
            String message = "Method '" + method + "' expected " + numDeclaredParams + " arguments but found " + numCallParams + ".";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(jmmNode), getNodeColumn(jmmNode), message));
        }

        for(int i=0; i < numDeclaredParams && i < numCallParams; i++){
            JmmNode callParamNode = jmmNode.getJmmChild(i);
            Type declaredParamType = symbolTable.getParameters(method).get(i).getType();
            Type callParamType = visit(callParamNode);

            if(!declaredParamType.equals(callParamType)){
                String declaredArgumentName = symbolTable.getParameters(method).get(i).getName();
                String message = "Method '" + method + "' expected argument '" + declaredArgumentName + "' to be '" + declaredParamType.print() + "' but found '" + callParamType.print() + ".";
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(callParamNode), getNodeColumn(callParamNode), message));
            }
        }
    }

    private Type checkIntegerLength(JmmNode jmmNode, Type type) {
        JmmNode lengthNode = jmmNode.getJmmChild(0);
        Type lengthType = visit(lengthNode);

        if (!lengthType.equals(INT_TYPE)){
            String message = "Expected array length to be '" + INT + "' but found '" + lengthType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(lengthNode), getNodeColumn(lengthNode), message));
        }

        jmmNode.put(TYPENAME, ARRAY);
        return ARRAY_TYPE;
    }

    private Type dealWithObjectCreation(JmmNode jmmNode, Type type) {
        String objectClassName = jmmNode.get("classname");

        if(Objects.equals(objectClassName, this.className)){
            jmmNode.put(TYPENAME, this.className);
            return new Type(this.className, false);
        }

        else if(findImport(this.imports, objectClassName)){
            jmmNode.put(TYPENAME, objectClassName);
            return new Type(objectClassName, false);
        }

        String message = "Cannot find '" + objectClassName + "'.";
        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(jmmNode), getNodeColumn(jmmNode), message)); //TODO: use column of 'classname'
        jmmNode.put(TYPENAME, UNKNOWN);
        return UNKNOWN_TYPE;
    }

    private Type dealWithInteger(JmmNode jmmNode, Type type) {
        jmmNode.put(TYPENAME, INT);
        return INT_TYPE;
    }

    private Type dealWithBoolean(JmmNode jmmNode, Type type) {
        jmmNode.put(TYPENAME, BOOLEAN);
        return BOOLEAN_TYPE;
    }

    private Type dealWithThis(JmmNode jmmNode, Type type) {
        if (Objects.equals(this.methodName, "main")) {
            String message = "'this' expression cannot be used in a static method.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(jmmNode), getNodeColumn(jmmNode), message));
            jmmNode.put(TYPENAME, UNKNOWN);
            return UNKNOWN_TYPE;
        }
        jmmNode.put(TYPENAME, this.className);
        return new Type(this.className, false);
    }

    private Type dealWithIdentifier(JmmNode jmmNode, Type type) {
        String identifier = jmmNode.get("value");
        Type identifierType = getIdentifierType(this.methodName, identifier, this.symbolTable);

        if(Objects.equals(identifierType.print(), UNKNOWN)){
            String message = "'" + identifier + "' is not declared.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, getNodeLine(jmmNode), getNodeColumn(jmmNode), message));
            jmmNode.put(TYPENAME, UNKNOWN);
        }
        jmmNode.put(TYPENAME, identifierType.print());
        return identifierType;
    }
}