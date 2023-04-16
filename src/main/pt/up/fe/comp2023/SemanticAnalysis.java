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
import static pt.up.fe.comp2023.SemanticUtils.BOOLEAN;

public class SemanticAnalysis extends AJmmVisitor<Void, Void> {
    private final MySymbolTable symbolTable;
    private final List<Report> reports;
    private String currentMethodName;
    private ExpressionAnalysis expressionAnalysis;

    public SemanticAnalysis (JmmNode rootNode, MySymbolTable symbolTable, List<Report> reports){
        this.symbolTable = symbolTable;
        this.reports = reports;
        visit(rootNode);
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::setDefaultVisit);
        addVisit("ClassDecl", this::checkImportedSuperClass);
        addVisit("MethodDecl", this::checkReturnType);
        addVisit("VoidMethodDecl", this::dealWithVoidMethod);
        addVisit("MainMethodDecl", this::checkMainMethodParameterType);
        addVisit("Condition", this::checkBooleanCondition);
        addVisit("Cycle", this::checkBooleanCondition);
        addVisit("Expr", this::expressionVisitor);
        addVisit("Assignment", this::checkCompatibleAssignment);
        addVisit("ArrayAssignment", this::validateArrayAssignment);
    }

    private Void setDefaultVisit(JmmNode jmmNode, Void unused) {
        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    public Void checkImportedSuperClass(JmmNode jmmNode, Void unused) {
        String superClass = this.symbolTable.getSuper();

        if(superClass != null && !findImport(this.symbolTable.getImports(), superClass)){
            String message = "Cannot find super class '" + superClass + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }

        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void checkReturnType(JmmNode jmmNode, Void unused) {
        this.currentMethodName = jmmNode.get("methodname");
        this.expressionAnalysis = new ExpressionAnalysis(this.currentMethodName, this.symbolTable, this.reports);

        JmmNode returnNode = jmmNode.getJmmChild(jmmNode.getNumChildren() - 1);
        Type returnType = this.symbolTable.getReturnType(jmmNode.get("methodname"));
        Type returnNodeType = expressionAnalysis.visit(returnNode);

        if(!returnNodeType.equals(returnType) && !returnNodeType.equals(UNDEFINED_TYPE)){
            String message = "Make method '" + jmmNode.get("methodname") +"' return " + returnType.print() + ".";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }

        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    public Void dealWithVoidMethod(JmmNode jmmNode, Void unused) {
        this.currentMethodName = jmmNode.get("methodname");
        this.expressionAnalysis = new ExpressionAnalysis(this.currentMethodName, this.symbolTable, this.reports);
        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void checkMainMethodParameterType(JmmNode jmmNode, Void unused) {
        this.currentMethodName = jmmNode.get("methodname");
        this.expressionAnalysis = new ExpressionAnalysis(this.currentMethodName, this.symbolTable, this.reports);

        String parameterType = jmmNode.get("parametertype");
        if(!Objects.equals(parameterType, "String")) {
            String message = "Main method expected a parameter of type 'String[]' but found '" + parameterType + "[]'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }

        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void checkBooleanCondition(JmmNode jmmNode, Void unused) {
        JmmNode expressionNode = jmmNode.getJmmChild(0);

        Type conditionType = expressionAnalysis.visit(expressionNode);
        if(!conditionType.equals(BOOLEAN_TYPE)) {
            String message = "Expected condition of type '" + BOOLEAN + "' but found '" + conditionType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }

        for(int i= 1; i < jmmNode.getNumChildren(); i++){
            visit(jmmNode.getJmmChild(i));
        }
        return null;
    }

    private Void expressionVisitor(JmmNode jmmNode, Void unused) {
        expressionAnalysis.visit(jmmNode.getJmmChild(0));
        return null;
    }

    private Void checkCompatibleAssignment(JmmNode jmmNode, Void unused) {
        String varName = jmmNode.get("varname");
        JmmNode expressionNode = jmmNode.getJmmChild(0);
        Type left = getIdentifierType(this.currentMethodName, varName, this.symbolTable);
        Type right = expressionAnalysis.visit(expressionNode);
        String superClass = this.symbolTable.getSuper();
        String className = this.symbolTable.getClassName();

        if(left.equals(UNKNOWN_TYPE)){
            String message = "'" + varName + "' is not declared.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }
        else if (right.equals(UNDEFINED_TYPE))
            expressionNode.put(TYPENAME, left.print());

        else if(right.equals(left))
            return null;

        else if (Objects.equals(left.print(), superClass) && Objects.equals(right.print(), className))
            return null;

        else if (findImport(symbolTable.getImports(), left.print()) && findImport(symbolTable.getImports(), right.print()))
            return null;

        String message = "Type of the assignee is not compatible with the assigned.";
        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        return null;
    }

    private Void validateArrayAssignment(JmmNode jmmNode, Void unused) {
        String varName = jmmNode.get("arrayname");
        JmmNode valueNode = jmmNode.getJmmChild(1);
        Type varType = getIdentifierType(this.currentMethodName, varName, this.symbolTable);
        Type indexType = expressionAnalysis.visit(jmmNode.getJmmChild(0));
        Type valueType = expressionAnalysis.visit(valueNode);

        if(!valueType.isArray()){
            String message = "'" + varName + "' must be an array.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message));
        }
        if(!indexType.equals(INT_TYPE)){
            String message = "Expected index expression of type '" + INT +"' but found '" + indexType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message));
        }

        if(valueType.equals(UNDEFINED_TYPE)){
            valueNode.put(TYPENAME, INT);
        }
        else if(!valueType.equals(INT_TYPE)){
            String message = "Type of the assignee is not compatible with the assigned.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }
        return null;
    }
}