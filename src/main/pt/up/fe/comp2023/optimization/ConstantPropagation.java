package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.Map;

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
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Identifier", this::dealWithIdentifier);
    }

    private Void setDefaultVisit(JmmNode jmmNode, Map<String, String> constants) {
        for (JmmNode child: jmmNode.getChildren())
            visit(child, constants);
        return null;
    }

    private Void dealWithAssignment(JmmNode jmmNode, Map<String, String> constants) {
        String varName = jmmNode.get("varname");
        JmmNode exprNode = jmmNode.getJmmChild(0);
        visit(exprNode);

        if (exprNode.getKind().equals("Integer") || exprNode.getKind().equals("Boolean"))
            constants.put(varName, exprNode.get("value"));
        else //Unknown Value
            constants.remove(varName);
        return null;
    }

    private Void dealWithIdentifier(JmmNode jmmNode, Map<String, String> constants) {
        //TODO

        return null;
    }


}
