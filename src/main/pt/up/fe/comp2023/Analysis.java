package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public class Analysis implements JmmAnalysis {
    private MySymbolTable symbolTable;
    private final ArrayList<Report> reports = new ArrayList<>();
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        JmmNode root = parserResult.getRootNode();
        this.symbolTable = new MySymbolTable(parserResult.getRootNode());

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis(root, this.symbolTable, this.reports);

        return new JmmSemanticsResult(parserResult, this.symbolTable, this.reports);
    }

    public MySymbolTable getSymbolTable(){
        return this.symbolTable;
    }

    public List<Report> getReports(){
        return this.reports;
    }

    public void addReport(Report report) {
        this.reports.add(report);
    }
}
