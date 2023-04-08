package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class StatementAnalysis extends AJmmVisitor<Void, Void> {
    private Analysis analysis;
    private ExpressionAnalysis expressionAnalysis;

    public StatementAnalysis(JmmNode statementNode, Analysis analysis){
        this.analysis = analysis;
        String methodName = statementNode.getJmmParent().get("methodname");
        this.expressionAnalysis = new ExpressionAnalysis(methodName, analysis);

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
        //TODO
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
