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
    private final JmmNode classDeclNode;
    private final MySymbolTable symbolTable;
    private final List<Report> reports;
    private final List<JmmNode> methodNodes = new ArrayList<>();

    public ClassDeclAnalysis(JmmNode rootNode, MySymbolTable symbolTable, List<Report> reports){
        this.classDeclNode = rootNode.getJmmChild(rootNode.getNumChildren() - 1);
        this.symbolTable = symbolTable;
        this.reports = reports;
    }

    @Override
    protected void buildVisitor() {
        addVisit("MethodDecl", this::addMethodNode);
        addVisit("VoidMethodDecl", this::addMethodNode);
        addVisit("MainMethodDecl", this::addMethodNode);
    }

    public void checkImportedSuperClass() {
        String superClass = this.symbolTable.getSuper();

        if(superClass != null && !findImport(this.symbolTable.getImports(), superClass)){
            String message = "Cannot find super class '" + superClass + "'.";
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
        }

        for (JmmNode child: classDeclNode.getChildren())
            visit(child);
    }

    private Void addMethodNode(JmmNode methodNode, Void unused) {
        this.methodNodes.add(methodNode);
        return null;
    }

    public List<JmmNode> getMethodNodes() {
        return this.methodNodes;
    }
}
