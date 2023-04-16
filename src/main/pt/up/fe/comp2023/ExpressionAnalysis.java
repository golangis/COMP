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
    private final String methodName;
    private final MySymbolTable symbolTable;
    private final List<Report> reports;
    public ExpressionAnalysis (String methodName, MySymbolTable symbolTable, List<Report> reports){
        this.methodName = methodName;
        this.symbolTable = symbolTable;
        this.reports = reports;
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
        Type expressionType = visit(jmmNode.getJmmChild(0));

        if(expressionType.equals(BOOLEAN_TYPE)){
            jmmNode.put(TYPENAME, BOOLEAN);
            return BOOLEAN_TYPE;
        }
        String message = "Expected expression of type '" +  BOOLEAN + "' but found '" + expressionType.print() + "'.";
        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        jmmNode.put(TYPENAME, UNKNOWN);
        return UNKNOWN_TYPE;
    }

    private Type checkIntegerOperands(JmmNode jmmNode, Type type) {
        Type leftOperandType = visit(jmmNode.getJmmChild(0));
        Type rightOperandType = visit(jmmNode.getJmmChild(1));

        if(!leftOperandType.equals(INT_TYPE)) {
            String message = "Expected operand of type '" + INT + "' but found '" + leftOperandType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put(TYPENAME, UNKNOWN);
        }

        if(!rightOperandType.equals(INT_TYPE)) {
            String message = "Expected operand of type '" + INT + "' but found '" + leftOperandType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            if(!jmmNode.hasAttribute(TYPENAME))
                jmmNode.put(TYPENAME, UNKNOWN);
        }

        if(jmmNode.hasAttribute(TYPENAME))    //Semantic errors were found
            return UNKNOWN_TYPE;
        if(Objects.equals(jmmNode.getKind(), "ArithmeticExpr")) {
            jmmNode.put(TYPENAME, INT);
            return INT_TYPE;
        }
        jmmNode.put(TYPENAME, BOOLEAN);
        return BOOLEAN_TYPE;
    }

    private Type checkBooleanOperands(JmmNode jmmNode, Type type) {
        Type leftOperandType = visit(jmmNode.getJmmChild(0));
        Type rightOperandType = visit(jmmNode.getJmmChild(1));

        if(!leftOperandType.equals(BOOLEAN_TYPE)) {
            String message = "Expected operand of type '" + BOOLEAN + "' but found '" + leftOperandType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put(TYPENAME, UNKNOWN);
        }

        if(!rightOperandType.equals(BOOLEAN_TYPE)) {
            String message = "Expected operand of type '" + BOOLEAN + "' but found '" + leftOperandType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            if(!jmmNode.hasAttribute(TYPENAME))
                jmmNode.put(TYPENAME, UNKNOWN);
        }

        if(jmmNode.hasAttribute(TYPENAME)) //Semantic errors were found
            return UNKNOWN_TYPE;
        jmmNode.put(TYPENAME, BOOLEAN);
        return BOOLEAN_TYPE;
    }

    private Type dealWithArraySubscript(JmmNode jmmNode, Type type) {
        Type variableType = visit(jmmNode.getJmmChild(0));
        Type indexType = visit(jmmNode.getJmmChild(1));

        if (!variableType.equals(ARRAY_TYPE)) {
            String message = "Expected '" + ARRAY_TYPE + "' type but found '" + variableType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put(TYPENAME, UNKNOWN);
        }

        if (!indexType.equals(INT_TYPE)) {
            String message = "Expected index expression of type '" + INT +"' but found '" + indexType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            if(!jmmNode.hasAttribute(TYPENAME))
                jmmNode.put(TYPENAME, UNKNOWN);
        }

        if(jmmNode.hasAttribute(TYPENAME)) //Semantic errors were found
            return UNKNOWN_TYPE;
        jmmNode.put(TYPENAME, INT);
        return INT_TYPE;
    }

    private Type dealWithLengthFieldAccess(JmmNode jmmNode, Type type) {
        Type expressionType = visit(jmmNode.getJmmChild(0));

        if(expressionType.equals(ARRAY_TYPE)){
            jmmNode.put(TYPENAME, INT);
            return INT_TYPE;
        }
        String message = "Cannot resolve symbol 'length'.";
        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        jmmNode.put(TYPENAME, UNKNOWN);
        return UNKNOWN_TYPE;
    }

    private Type dealWithMethodCall(JmmNode jmmNode, Type type) {
        String declaredClass = this.symbolTable.getClassName();
        String superClass = this.symbolTable.getSuper();
        String expressionType = visit(jmmNode.getJmmChild(0)).print();
        String method = jmmNode.get("methodcall");

        if(this.symbolTable.getMethods().contains(method)){
            if(!Objects.equals(expressionType, declaredClass)){
                String message = "Expected expression of type '" + declaredClass + "' but found '" + expressionType + "'.";
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            }
            this.verifyArgumentTypes(jmmNode.getJmmChild(1), method);
            jmmNode.put(TYPENAME, symbolTable.getReturnType(method).print());
            return this.symbolTable.getReturnType(method);
        }

        else if(Objects.equals(expressionType, declaredClass) || Objects.equals(expressionType, superClass)){
            if(!findImport(this.symbolTable.getImports(), superClass)){
                String message = "Cannot find super class '" + superClass + "'.";
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            }
        }

        else if(!findImport(this.symbolTable.getImports(), expressionType)){
            String message = "'" + expressionType + "' is not declared.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }
        jmmNode.put(TYPENAME, UNDEFINED);
        return UNDEFINED_TYPE;
    }

    private void verifyArgumentTypes(JmmNode jmmNode, String method) {
        int numDeclaredParams = symbolTable.getParameters(method).size();
        int numCallParams = jmmNode.getNumChildren();

        if(numDeclaredParams != numCallParams){
            String message = "Method '" + method + "' expected " + numDeclaredParams + " but found " + numCallParams + ".";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }

        for(int i=0; i < numDeclaredParams && i < numCallParams; i++){
            Type declaredParamType = symbolTable.getParameters(method).get(i).getType();
            Type callParamType = visit(jmmNode.getJmmChild(i));

            if(!declaredParamType.equals(callParamType)){
                String declaredArgumentName = symbolTable.getParameters(method).get(i).getName();
                String message = "Method '" + method + "' expected argument '" + declaredArgumentName + "' to be '" + declaredParamType.print() + "' but found '" + callParamType.print() + ".";
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            }
        }
    }

    private Type checkIntegerLength(JmmNode jmmNode, Type type) {
        Type lengthType = visit(jmmNode.getJmmChild(0));

        if (lengthType.equals(INT_TYPE)){
            jmmNode.put(TYPENAME, ARRAY);
            return ARRAY_TYPE;
        }
        String message = "Expected array length to be '" + INT + "' but found '" + lengthType.print() + "'.";
        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        jmmNode.put(TYPENAME, UNKNOWN);
        return UNKNOWN_TYPE;
    }

    private Type dealWithObjectCreation(JmmNode jmmNode, Type type) {
        String declaredClassName = this.symbolTable.getClassName();
        String objectClassName = jmmNode.get("classname");

        if(Objects.equals(objectClassName, declaredClassName)){
            jmmNode.put(TYPENAME, declaredClassName);
            return new Type(declaredClassName, false);
        }

        if(findImport(this.symbolTable.getImports(), objectClassName)){
            jmmNode.put(TYPENAME, objectClassName);
            return new Type(objectClassName, false);
        }

        String message = "Cannot find '" + objectClassName + "'.";
        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
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
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put(TYPENAME, UNKNOWN);
            return UNKNOWN_TYPE;
        }
        String className = this.symbolTable.getClassName();
        jmmNode.put(TYPENAME, className);
        return new Type(className, false);
    }

    private Type dealWithIdentifier(JmmNode jmmNode, Type type) {
        String identifier = jmmNode.get("value");
        Type identifierType = getIdentifierType(this.methodName, identifier, this.symbolTable);

        if(Objects.equals(identifierType.print(), UNKNOWN)){
            String message = "'" + identifier + "' is not declared.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put(TYPENAME, UNKNOWN);
        }
        jmmNode.put("typename", identifierType.print());
        return identifierType;
    }
}