package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.*;

import static pt.up.fe.comp2023.optimization.OptimizationUtils.*;

public class ConstantPropagation extends AJmmVisitor<Map<String, String>, Void> {
    private final JmmSemanticsResult semanticsResult;
    private boolean codeModified;

    public ConstantPropagation (JmmSemanticsResult semanticsResult){
        this.semanticsResult = semanticsResult;
    }

    public boolean apply(){
        this.codeModified = false;
        Map<String, String> constants = new HashMap<>();

        visit(semanticsResult.getRootNode(), constants);
        return this.codeModified;
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::setDefaultVisit);
        addVisit("MethodDecl", this::clearConstants);
        addVisit("VoidMethodDecl", this::clearConstants);
        addVisit("MainMethodDecl", this::clearConstants);
        addVisit("Condition", this::dealWithCondition);
        addVisit("Cycle", this::dealWithCycle);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Identifier", this::dealWithIdentifier);
    }

    private Void setDefaultVisit(JmmNode jmmNode, Map<String, String> constants) {
        for (JmmNode child: jmmNode.getChildren())
            visit(child, constants);
        return null;
    }

    private Void clearConstants(JmmNode jmmNode, Map<String, String> constants) {
        constants.clear();

        for (JmmNode child: jmmNode.getChildren())
            visit(child, constants); //each statement modifies the map
        return null;
    }

    private Void dealWithCondition(JmmNode jmmNode, Map<String, String> constants) {
        JmmNode conditionNode = jmmNode.getJmmChild(0);
        JmmNode ifCode = jmmNode.getJmmChild(1);
        JmmNode elseCode = jmmNode.getJmmChild(2);
        Map<String, String> ifConstants =  new HashMap<>(constants);
        Map<String, String> elseConstants =  new HashMap<>(constants);

        visit(conditionNode, constants);
        visit(ifCode, ifConstants);
        visit(elseCode, elseConstants);
        intersectMaps(ifConstants, elseConstants, constants);
        return null;
    }

    private Void dealWithCycle(JmmNode jmmNode, Map<String, String> constants) {
        Map <String, String> oldConstants = new HashMap<>(constants);
        Map <String, String> cycleConstants = new HashMap<>(constants);

        for (JmmNode child: jmmNode.getChildren())
            visit(child, cycleConstants);

        intersectMaps(cycleConstants, oldConstants, constants);
        return null;
    }

    private Void dealWithAssignment(JmmNode jmmNode, Map<String, String> constants) {
        String varName = jmmNode.get("varname");
        JmmNode exprNode = jmmNode.getJmmChild(0);
        visit(exprNode, constants);

        if (exprNode.getKind().equals("Integer") || exprNode.getKind().equals("Boolean"))
            constants.put(varName, exprNode.get("value"));
        else //Unknown Value
            constants.remove(varName);
        return null;
    }

    private Void dealWithIdentifier(JmmNode jmmNode, Map<String, String> constants) {
        String identifierName = jmmNode.get("value");
        String constant = constants.get(identifierName);

        if(constant != null) {
            JmmNode newNode;
            if(constant.equals("true") || constant.equals("false")) //Boolean constant
                newNode = new JmmNodeImpl("Boolean");
            else  //Integer constant
                newNode = new JmmNodeImpl("Integer");
            newNode.put("value", constant);
            jmmNode.replace(newNode);
            this.codeModified = true;
        }
        return null;
    }
}
