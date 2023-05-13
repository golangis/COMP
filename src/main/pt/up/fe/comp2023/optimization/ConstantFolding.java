package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import static pt.up.fe.comp2023.optimization.OptimizationUtils.*;

public class ConstantFolding extends AJmmVisitor<Void, Void> {
    private final JmmSemanticsResult semanticsResult;
    private boolean codeModified;
    public ConstantFolding (JmmSemanticsResult semanticsResult){
        this.semanticsResult = semanticsResult;
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
        JmmNode exprNode = jmmNode.getJmmChild(0);
        visit(exprNode);

        if(exprNode.getKind().equals("Integer") || exprNode.getKind().equals("Boolean")){
            this.codeModified = true;
            jmmNode.replace(exprNode);
        }
        return null;
    }

    private Void negateBooleanExpr(JmmNode jmmNode, Void unused) {
        JmmNode exprNode = jmmNode.getJmmChild(0);
        visit(exprNode);

        if (exprNode.getKind().equals("Boolean")) {
            this.codeModified = true;
            boolean exprValue = Boolean.parseBoolean(exprNode.get("value"));
            exprNode.put("value", String.valueOf(!exprValue));
            jmmNode.replace(exprNode);
        }
        return null;
    }

    private Void computeArithmeticExprResult(JmmNode jmmNode, Void unused) {
        JmmNode leftExpr = jmmNode.getJmmChild(0);
        JmmNode rightExpr = jmmNode.getJmmChild(1);
        String operator = jmmNode.get("op");
        visit(leftExpr);
        visit(rightExpr);

        if (leftExpr.getKind().equals("Integer") && rightExpr.getKind().equals("Integer")){
            this.codeModified = true;
            int leftValue = Integer.parseInt(leftExpr.get("value"));
            int rightValue = Integer.parseInt(rightExpr.get("value"));

            switch (operator) {
                case "+" -> leftExpr.put("value", String.valueOf(leftValue + rightValue));
                case "-" -> leftExpr.put("value", String.valueOf(leftValue - rightValue));
                case "*" -> leftExpr.put("value", String.valueOf(leftValue * rightValue));
                case "/" -> leftExpr.put("value", String.valueOf(leftValue / rightValue));
            }
            jmmNode.replace(leftExpr);
        }
        return null;
    }

    private Void computeComparisonResult(JmmNode jmmNode, Void unused) {
        JmmNode leftExpr = jmmNode.getJmmChild(0);
        JmmNode rightExpr = jmmNode.getJmmChild(1);
        String operator = jmmNode.get("op");
        visit(leftExpr);
        visit(rightExpr);

        if (leftExpr.getKind().equals("Integer") && rightExpr.getKind().equals("Integer")){
            this.codeModified = true;
            int leftValue = Integer.parseInt(leftExpr.get("value"));
            int rightValue = Integer.parseInt(rightExpr.get("value"));
            JmmNodeImpl newNode = new JmmNodeImpl("Boolean");

            if (operator.equals("<"))
                newNode.put("value", String.valueOf(leftValue < rightValue));
            else
                newNode.put("value", String.valueOf(leftValue > rightValue));

            jmmNode.replace(newNode);
        }
        return null;
    }

    private Void computeLogicalExprResult(JmmNode jmmNode, Void unused) {
        JmmNode leftExpr = jmmNode.getJmmChild(0);
        JmmNode rightExpr = jmmNode.getJmmChild(1);
        String operator = jmmNode.get("op");
        visit(leftExpr);
        visit(rightExpr);

        if (leftExpr.getKind().equals("Boolean") && rightExpr.getKind().equals("Boolean")){
            this.codeModified = true;
            boolean leftValue = Boolean.parseBoolean(leftExpr.get("value"));
            boolean rightValue = Boolean.parseBoolean(rightExpr.get("value"));

            if (operator.equals("&&"))
                leftExpr.put("value", String.valueOf(leftValue && rightValue));
            else
                leftExpr.put("value", String.valueOf(leftValue || rightValue));
            jmmNode.replace(leftExpr);
        }
        return null;
    }
}
