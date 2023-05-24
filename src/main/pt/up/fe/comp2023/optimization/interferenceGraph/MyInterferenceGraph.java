package pt.up.fe.comp2023.optimization.interferenceGraph;

import java.util.ArrayList;
import java.util.List;

public class MyInterferenceGraph {
    private final List<MyNode> nodes = new ArrayList<>();

    public void addNode(String variable){
        MyNode newNode = new MyNode(variable);
        this.nodes.add(newNode);
    }

    public MyNode getNode(String varName){
        for(MyNode node : this.nodes){
            if(node.getVariable().equals(varName))
                return node;
        }
        return null;
    }
    public void addInterferenceEdge(String src, String dest){
        getNode(src).getAdj().add(dest);
        getNode(dest).getAdj().add(src);
    }

    public void connectInterferingVariables(List<String> variables){
        for(int i = 0; i < variables.size(); i++){
            for(int j = i + 1; j < variables.size(); j++){
                String src = variables.get(i);
                String dest = variables.get(j);

                addInterferenceEdge(src, dest);
            }
        }
    }

    public void removeNode(String varName){
        MyNode node = getNode(varName);

        for(String adj : node.getAdj()){
            MyNode adjNode = getNode(adj);
            adjNode.removeAdj(varName);
        }
        this.nodes.remove(node);
    }
}
