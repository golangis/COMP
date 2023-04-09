package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

import static pt.up.fe.comp2023.SemanticUtils.*;

public class StatementAnalysis extends AJmmVisitor<Void, Void> {
    private final MySymbolTable symbolTable;
    private final List<Report> reports;
    private final ExpressionAnalysis expressionAnalysis;

    public StatementAnalysis(JmmNode statementNode,  MySymbolTable symbolTable, List<Report> reports){
        this.symbolTable = symbolTable;
        this.reports = reports;
        String methodName = statementNode.getJmmParent().get("methodname");
        this.expressionAnalysis = new ExpressionAnalysis(methodName, symbolTable, reports);

        visit(statementNode);
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::setDefaultVisit);
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

    private Void checkBooleanCondition(JmmNode jmmNode, Void unused) {
        JmmNode expressionNode = jmmNode.getJmmChild(0);

        Type conditionType = expressionAnalysis.visit(expressionNode);
        if(conditionType.equals(BOOLEAN_TYPE)) {
            String message = "Expected condition of type '" + BOOLEAN + "' but found '" + conditionType.print() + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }

        for(int i= 1; i < jmmNode.getNumChildren(); i++){
            visit(jmmNode.getJmmChild(i));
        }
        return null;
    }

    private Void expressionVisitor(JmmNode jmmNode, Void unused) {
        //TODO
        return null;
    }

    private Void checkCompatibleAssignment(JmmNode jmmNode, Void unused) {
        //TODO
        return null;
    }

    private Void validateArrayAssignment(JmmNode jmmNode, Void unused) {
        //TODO
        return null;
    }
}
