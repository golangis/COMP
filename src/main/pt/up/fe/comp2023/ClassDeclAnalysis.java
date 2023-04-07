package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassDeclAnalysis extends AJmmVisitor<Void, Void> {
    private final JmmNode classDeclNode;
    private final Analysis analysis;
    private final List<JmmNode> methodNodes = new ArrayList<>();

    public ClassDeclAnalysis(JmmNode rootNode, Analysis analysis){
        this.classDeclNode = rootNode.getJmmChild(rootNode.getNumChildren() - 1);
        this.analysis = analysis;
    }

    @Override
    protected void buildVisitor() {
        addVisit("MethodDecl", this::addMethodNode);
        addVisit("VoidMethodDecl", this::addMethodNode);
        addVisit("MainMethodDecl", this::addMethodNode);
    }

    public void checkImportedSuperClass() {
        String superClass = analysis.getSymbolTable().getSuper();

        if(superClass != null && !findImport(superClass)){
            String message = "Cannot find super class '" + superClass + "'.";
            analysis.addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, 1, 1, message)); //TODO: change line and column values
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

    Boolean findImport (String string) {
        List<String> imports = analysis.getSymbolTable().getImports();

        for(String imported : imports){
            List<String> splitImport = List.of(imported.split("\\."));
            if (Objects.equals(splitImport.get(splitImport.size() - 1), string))
                return true;
        }
        return false;
    }
}
