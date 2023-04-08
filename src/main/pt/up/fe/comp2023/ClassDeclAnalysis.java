package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fe.comp2023.SemanticUtils.findImport;

public class ClassDeclAnalysis extends AJmmVisitor<Void, Void> {
    private final MySymbolTable symbolTable;
    private final List<Report> reports;
    private final List<JmmNode> methodNodes = new ArrayList<>();

    public ClassDeclAnalysis(JmmNode rootNode, MySymbolTable symbolTable, List<Report> reports){
        this.symbolTable = symbolTable;
        this.reports = reports;
        visit(rootNode);
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::setDefaultVisit);
        addVisit("ClassDecl", this::checkImportedSuperClass);
        addVisit("MethodDecl", this::addMethodNode);
        addVisit("VoidMethodDecl", this::addMethodNode);
        addVisit("MainMethodDecl", this::addMethodNode);
    }

    private Void setDefaultVisit(JmmNode jmmNode, Void unused) {
        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    public Void checkImportedSuperClass(JmmNode jmmNode, Void unused) {
        String superClass = this.symbolTable.getSuper();

        if(superClass != null && !findImport(this.symbolTable.getImports(), superClass)){
            String message = "Cannot find super class '" + superClass + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }

        for (JmmNode child: jmmNode.getChildren())
            visit(child);
        return null;
    }

    private Void addMethodNode(JmmNode methodNode, Void unused) {
        this.methodNodes.add(methodNode);
        return null;
    }

    public List<JmmNode> getMethodNodes() {
        return this.methodNodes;
    }
}
