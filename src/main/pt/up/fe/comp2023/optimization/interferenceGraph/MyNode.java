package pt.up.fe.comp2023.optimization.interferenceGraph;

import org.specs.comp.ollir.Element;
import java.util.HashSet;
import java.util.Set;

public class MyNode {
    private final String varName;
    private final Set<String> adj = new HashSet<>();

    public MyNode(String varName){
        this.varName = varName;
    }

    public String  getVariable() {
        return this.varName;
    }

    public Set<String> getAdj() {
        return adj;
    }

    public void addAdj(String varName) {
        this.adj.add(varName);
    }

    public int getNodeDegree() {
        return this.adj.size();
    }

    public void removeAdj(String varName){
        this.adj.remove(varName);
    }
}
