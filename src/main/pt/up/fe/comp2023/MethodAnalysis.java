package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MethodAnalysis extends AJmmVisitor<Void, Void>  {
    private final MySymbolTable symbolTable;
    private final List<Report> reports;
    private final List<JmmNode> statementNodes = new ArrayList<>();
    private final ExpressionAnalysis expressionAnalysis;

    public MethodAnalysis(JmmNode methodNode, MySymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        this.expressionAnalysis = new ExpressionAnalysis(methodNode.get("methodname"), symbolTable, reports);
        visit(methodNode);
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::setDefaultVisit);
        addVisit("MainMethodDecl", this::checkMainMethodParameterType);
        addVisit("MethodDecl", this::checkReturnType);
        addVisit("CodeBlock", this::addStatement);
        addVisit("Condition", this::addStatement);
        addVisit("Cycle", this::addStatement);
        addVisit("Expr", this::addStatement);
        addVisit("Assignment", this::addStatement);
        addVisit("ArrayAssignment", this::addStatement);
    }

    private Void setDefaultVisit(JmmNode jmmNode, Void unused) {
        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void checkMainMethodParameterType(JmmNode jmmNode, Void unused) {
        String parameterType = jmmNode.get("parametertype");
        if(!Objects.equals(parameterType, "String")) {
            String message = "Main method expected a parameter of type 'String[]' but found '" + parameterType + "[]'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }

        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void checkReturnType(JmmNode jmmNode, Void unused) {
        JmmNode returnNode = jmmNode.getJmmChild(jmmNode.getNumChildren() - 1);
        Type returnType = this.symbolTable.getReturnType(jmmNode.get("methodname"));
        Type returnNodeType = expressionAnalysis.visit(returnNode);

        if(!returnType.equals(returnNodeType)){
            String message = "Make method '" + jmmNode.get("methodname") +"' return " + returnType.print() + ".";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }

        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void addStatement(JmmNode jmmNode, Void unused) {
        this.statementNodes.add(jmmNode);
        return null;
    }

    public List<JmmNode> getStatementNodes() {
        return this.statementNodes;
    }
}
