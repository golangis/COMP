package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class ConstantPropagation extends AJmmVisitor<Void, Void> {
    private SymbolTable symbolTable;
    private final JmmSemanticsResult semanticsResult;
    private boolean codeModified;

    public ConstantPropagation (JmmSemanticsResult semanticsResult){
        this.symbolTable = semanticsResult.getSymbolTable();
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
    }

    private Void setDefaultVisit(JmmNode jmmNode, Void unused) {
        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }
}
