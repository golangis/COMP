package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;

public class ClassDeclAnalysis extends AJmmVisitor<Void, Void> {
    private JmmNode classDeclNode;
    private final List<JmmNode> methodNodes = new ArrayList<>();

    public ClassDeclAnalysis(JmmNode rootNode){
        this.classDeclNode = rootNode.getJmmChild(rootNode.getNumChildren() - 1);
    }

    @Override
    protected void buildVisitor() {
        addVisit("MethodDecl", this::addMethodNode);
        addVisit("VoidMethodDecl", this::addMethodNode);
        addVisit("MainMethodDecl", this::addMethodNode);
    }

    public void checkImportedSuperClass() {
    }

    private Void addMethodNode(JmmNode methodNode, Void unused) {
        return null;
    }

    public List<JmmNode> getMethodNodes() {
        return this.methodNodes;
    }
}
