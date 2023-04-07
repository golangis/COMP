package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;

public class MethodAnalysis extends AJmmVisitor<Void, Void> {
    private Analysis analysis;
    private final List<JmmNode> statementNodes = new ArrayList<>();

    public MethodAnalysis(JmmNode methodNode, Analysis analysis) {
        this.analysis = analysis;
        visit(methodNode);
    }

    @Override
    protected void buildVisitor() {
        addVisit("MainMethodDecl", this::checkMainMethodParameterType);
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
        return null;
    }

    private Void addStatement(JmmNode jmmNode, Void unused) {
        return null;
    }

    public List<JmmNode> getStatementNodes() {
        return this.statementNodes;
    }
}
