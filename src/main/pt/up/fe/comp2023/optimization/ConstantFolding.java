package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class ConstantFolding extends AJmmVisitor<Void, Void> {
    private final JmmSemanticsResult semanticsResult;
    private SymbolTable symbolTable;
    private String currentMethodName;
    private boolean codeModified;
    public ConstantFolding (JmmSemanticsResult semanticsResult){
        this.semanticsResult = semanticsResult;
        this.symbolTable = semanticsResult.getSymbolTable();
    }

    public boolean apply(){
        this.codeModified = false;
        visit(semanticsResult.getRootNode());
        return this.codeModified;
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::setDefaultVisit);
        addVisit("ParenthesesExpr", this::computeParenthesesExprResult);
        addVisit("NegationExpr", this::negateBooleanExpr);
        addVisit("ArithmeticExpr", this::computeArithmeticExprResult);
        addVisit("ComparisonExpr", this::computeComparisonResult);
        addVisit("LogicalExpr", this::computeLogicalExprResult);
    }

    private Void setDefaultVisit(JmmNode jmmNode, Void unused) {
        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void computeParenthesesExprResult(JmmNode jmmNode, Void unused) {
        System.out.println("Parentheses Expr");
        visit(jmmNode.getJmmChild(0));
        JmmNode exprNode = jmmNode.getJmmChild(0);

        if(exprNode.getKind().equals("Integer") || exprNode.getKind().equals("Boolean")){
            this.codeModified = true;
            jmmNode.replace(exprNode);
        }

        return null;
    }

    private Void negateBooleanExpr(JmmNode jmmNode, Void unused) {
        visit(jmmNode.getJmmChild(0));
        JmmNode exprNode = jmmNode.getJmmChild(0);

        if (exprNode.getKind().equals("Boolean")) {
            this.codeModified = true;
            boolean exprValue = Boolean.parseBoolean(exprNode.get("value"));
            exprNode.put("value", String.valueOf(!exprValue));
            jmmNode.replace(exprNode);
        }

        return null;
    }

    private Void computeArithmeticExprResult(JmmNode jmmNode, Void unused) {
        //TODO
        return null;
    }

    private Void computeComparisonResult(JmmNode jmmNode, Void unused) {
        //TODO
        return null;
    }

    private Void computeLogicalExprResult(JmmNode jmmNode, Void unused) {
        //TODO
        return null;
    }
}
