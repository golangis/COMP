package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;

public class Analysis implements JmmAnalysis {
    private MySymbolTable symbolTable;
    private final ArrayList<Report> reports = new ArrayList<>();
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        this.symbolTable = new MySymbolTable(parserResult.getRootNode());
        return new JmmSemanticsResult(parserResult, this.symbolTable, this.reports);
    }
}
