package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Objects;

import static pt.up.fe.comp2023.SemanticUtils.findImport;

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
        if(expressionType.isArray())
            jmmNode.put("typename", "array");
        else
            jmmNode.put("typename", expressionType.getName());
        return expressionType;
    }

    private Type checkBooleanExpression(JmmNode jmmNode, Type type) {
        String expressionType = visit(jmmNode.getJmmChild(0)).getName();

        if(Objects.equals(expressionType, "boolean")){
            jmmNode.put("typename", "boolean");
            return new Type("boolean", false);
        }
        String message = "Expected expression of type 'boolean' but found '" + expressionType + "'.";
        this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        jmmNode.put("typename", "unknown");
        return new Type("unknown", false);
    }

    private Type checkIntegerOperands(JmmNode jmmNode, Type type) {
        Type leftOperandType = visit(jmmNode.getJmmChild(0));
        Type rightOperandType = visit(jmmNode.getJmmChild(1));

        if(!Objects.equals(leftOperandType.getName(), "int") || leftOperandType.isArray()) {
            String message = "Expected operand of type 'int' but found '" + leftOperandType.getName() + "'.";
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put("typename", "unknown");
        }

        if(!Objects.equals(rightOperandType.getName(), "int") || rightOperandType.isArray()) {
            String message = "Expected operand of type 'int' but found '" + leftOperandType.getName() + "'.";
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            if(!jmmNode.getAttributes().contains("typename"))
                jmmNode.put("typename", "unknown");
        }

        if(jmmNode.getAttributes().contains("typename"))    //Semantic errors were found
            return new Type("unknown", false);

        if(Objects.equals(jmmNode.getKind(), "ArithmeticExpr")) {
            jmmNode.put("typename", "int");
            return new Type("int", false);
        }

        jmmNode.put("typename", "boolean");
        return new Type("boolean", false);
    }

    private Type checkBooleanOperands(JmmNode jmmNode, Type type) {
        Type leftOperandType = visit(jmmNode.getJmmChild(0));
        Type rightOperandType = visit(jmmNode.getJmmChild(1));

        if(!Objects.equals(leftOperandType.getName(), "boolean") || leftOperandType.isArray()) {
            String message = "Expected operand of type 'boolean' but found '" + leftOperandType.getName() + "'.";
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            jmmNode.put("typename", "unknown");
        }

        if(!Objects.equals(rightOperandType.getName(), "boolean") || rightOperandType.isArray()) {
            String message = "Expected operand of type 'boolean' but found '" + leftOperandType.getName() + "'.";
            this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
            if(!jmmNode.getAttributes().contains("typename"))
                jmmNode.put("typename", "unknown");
        }

        if(jmmNode.getAttributes().contains("typename"))    //Semantic errors were found
            return new Type("unknown", false);
        jmmNode.put("typename", "boolean");
        return new Type("boolean", false);
    }

    private Type dealWithArraySubscript(JmmNode jmmNode, Type type) {
        //TODO
        return null;
    }

    private Type dealWithLengthFieldAccess(JmmNode jmmNode, Type type) {
        //TODO
        return null;
    }

    private Type dealWithMethodCall(JmmNode jmmNode, Type type) {
        //TODO
        return null;
    }

    private Type checkIntegerLength(JmmNode jmmNode, Type type) {
        Type lengthType = visit(jmmNode.getJmmChild(0));

        if (Objects.equals(lengthType.getName(), "int") && !lengthType.isArray()){
            jmmNode.put("typename", "array");
            return new Type("int", true);
        }
        String message = "Expected array length to be 'int' but found '" + lengthType + "'.";
        this.analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        jmmNode.put("typename", "unknown");
        return new Type("unknown", false);
    }

    private Type dealWithObjectCreation(JmmNode jmmNode, Type type) {
        String declaredClassName = analysis.getSymbolTable().getClassName();
        String objectClassName = jmmNode.get("classname");

        if(Objects.equals(objectClassName, declaredClassName)){
            jmmNode.put("typename", declaredClassName);
            return new Type(declaredClassName, false);
        }

        if(findImport(analysis.getSymbolTable().getImports(), objectClassName)){
            jmmNode.put("typename", objectClassName);
            return new Type(objectClassName, false);
        }

        return new Type("unknown", false);
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
        String className = analysis.getSymbolTable().getClassName();
        jmmNode.put("typename", className);
        return new Type(className, false);
    }

    private Type dealWithIdentifier(JmmNode jmmNode, Type type) {
        //TODO
        return null;
    }
}