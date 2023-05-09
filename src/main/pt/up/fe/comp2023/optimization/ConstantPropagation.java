package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

public class ConstantPropagation extends AJmmVisitor<Void, Void> {
    private SymbolTable symbolTable;
    private String currentMethodName;

    public ConstantPropagation (JmmSemanticsResult semanticsResult){
        this.symbolTable = semanticsResult.getSymbolTable();
    }

    @Override
    protected void buildVisitor() {

    }
}
