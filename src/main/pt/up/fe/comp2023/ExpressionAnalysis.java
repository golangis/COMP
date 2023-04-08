package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Objects;

import static pt.up.fe.comp2023.SemanticUtils.*;

public class ExpressionAnalysis extends AJmmVisitor<Type, Type> {
    private String methodName;
    private final Analysis analysis;
    public ExpressionAnalysis (String methodName, Analysis analysis){
        this.analysis = analysis;
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
        this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        jmmNode.put(TYPENAME, UNKNOWN);
        return UNKNOWN_TYPE;
    }

    private Type checkIntegerOperands(JmmNode jmmNode, Type type) {
        Type leftOperandType = visit(jmmNode.getJmmChild(0));
        Type rightOperandType = visit(jmmNode.getJmmChild(1));

        if(leftOperandType.equals(INT_TYPE)) {
            String message = "Expected operand of type '" + INT + "' but found '" + leftOperandType.print() + "'.";
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put(TYPENAME, UNKNOWN);
        }

        if(rightOperandType.equals(INT_TYPE)) {
            String message = "Expected operand of type '" + INT + "' but found '" + leftOperandType.print() + "'.";
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
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

        if(leftOperandType.equals(BOOLEAN_TYPE)) {
            String message = "Expected operand of type '" + BOOLEAN + "' but found '" + leftOperandType.print() + "'.";
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put(TYPENAME, UNKNOWN);
        }

        if(rightOperandType.equals(BOOLEAN_TYPE)) {
            String message = "Expected operand of type '" + BOOLEAN + "' but found '" + leftOperandType.print() + "'.";
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
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

        if (variableType.equals(ARRAY_TYPE)) {
            String message = "Expected '" + ARRAY_TYPE + "' type but found '" + variableType.print() + "'.";
            analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put(TYPENAME, UNKNOWN);
        }

        if (indexType.equals(INT_TYPE)) {
            String message = "Expected index expression of type '" + INT +"' but found '" + indexType.print() + "'.";
            analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            if(!jmmNode.hasAttribute(TYPENAME))
                jmmNode.put(TYPENAME, UNKNOWN);
        }

        if(jmmNode.hasAttribute(TYPENAME)) //Semantic errors were found
            return UNKNOWN_TYPE;
        return INT_TYPE;
    }

    private Type dealWithLengthFieldAccess(JmmNode jmmNode, Type type) {
        Type expressionType = visit(jmmNode.getJmmChild(0));

        if(expressionType.equals(ARRAY_TYPE)){
            jmmNode.put(TYPENAME, INT);
            return INT_TYPE;
        }
        String message = "Cannot resolve symbol 'length'.";
        analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        jmmNode.put(TYPENAME, UNKNOWN);
        return UNKNOWN_TYPE;
    }

    private Type dealWithMethodCall(JmmNode jmmNode, Type type) {
        String method = jmmNode.get("methodcall");
        String declaredClass = analysis.getSymbolTable().getClassName();

        if(analysis.getSymbolTable().getMethods().contains(method)){
            String expressionType = visit(jmmNode.getJmmChild(0)).print();

            if(!Objects.equals(expressionType, declaredClass)){
                String message = "Expected expression of type '" + declaredClass + "' but found '" + expressionType + "'.";
                analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
                jmmNode.put(TYPENAME, UNKNOWN);
            }

            //TODO: check parameters

            if(jmmNode.hasAttribute(TYPENAME)) //Semantic errors were found
                return UNKNOWN_TYPE;
            else
                return analysis.getSymbolTable().getReturnType(method);
        }

        //TODO: if the class extends another class:
            //TODO: check if class is imported
                //TODO: check if expression.type == declaredClass or superClass
                //TODO: compute node type(undefined)

        //TODO: else (it is a method of a imported class)
            //TODO: check if expression.type (=classname) is imported
            //TODO: compute node type

        return null;
    }

    private Type checkIntegerLength(JmmNode jmmNode, Type type) {
        Type lengthType = visit(jmmNode.getJmmChild(0));

        if (lengthType.equals(INT_TYPE)){
            jmmNode.put(TYPENAME, ARRAY);
            return ARRAY_TYPE;
        }
        String message = "Expected array length to be '" + INT + "' but found '" + lengthType.print() + "'.";
        this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        jmmNode.put(TYPENAME, UNKNOWN);
        return UNKNOWN_TYPE;
    }

    private Type dealWithObjectCreation(JmmNode jmmNode, Type type) {
        String declaredClassName = analysis.getSymbolTable().getClassName();
        String objectClassName = jmmNode.get("classname");

        if(Objects.equals(objectClassName, declaredClassName)){
            jmmNode.put(TYPENAME, declaredClassName);
            return new Type(declaredClassName, false);
        }

        if(findImport(analysis.getSymbolTable().getImports(), objectClassName)){
            jmmNode.put(TYPENAME, objectClassName);
            return new Type(objectClassName, false);
        }

        String message = "Cannot find '" + objectClassName + "'.";
        this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
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
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put(TYPENAME, UNKNOWN);
            return UNKNOWN_TYPE;
        }
        String className = analysis.getSymbolTable().getClassName();
        jmmNode.put(TYPENAME, className);
        return new Type(className, false);
    }

    private Type dealWithIdentifier(JmmNode jmmNode, Type type) {
        String identifier = jmmNode.get("value");
        Type identifierType = getIdentifierType(this.methodName, identifier, analysis.getSymbolTable());

        if(Objects.equals(identifierType.print(), UNKNOWN)){
            String message = "'" + identifier + "' is not declared.";
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put(TYPENAME, UNKNOWN);
        }
        jmmNode.put("typename", identifierType.print());
        return identifierType;
    }
}