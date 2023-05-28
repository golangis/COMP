package pt.up.fe.comp2023.optimization.interferenceGraph;

import java.util.HashSet;
import java.util.Set;

public class MyNode {
    private final String varName;
    private final Set<String> neighbours = new HashSet<>();

    public MyNode(String varName){
        this.varName = varName;
    }

    public String  getVariable() {
        return this.varName;
    }

    public Set<String> getNeighbours() {
        return neighbours;
    }

    public void addNeighbour(String varName) {
        this.neighbours.add(varName);
    }

    public int getDegree() {
        return this.neighbours.size();
    }

    public void removeNeighbour(String varName){
        this.neighbours.remove(varName);
    }
}
