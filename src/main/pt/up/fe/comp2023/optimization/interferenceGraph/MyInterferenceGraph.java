package pt.up.fe.comp2023.optimization.interferenceGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyInterferenceGraph {
    private final List<MyNode> nodes = new ArrayList<>();
    private Map<String, Integer> nodeColor = new HashMap<>();

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

    public Map<String, Integer> isMColoringFeasible(int maxColors){
        //TODO

        return this.nodeColor;
    }

    public Map<String, Integer> findOptimalColoring(){
        int maxColors = this.nodes.size();

        for(MyNode node : this.nodes){
            for(int color = 0; color < maxColors; color++){
                if(isValidColor(node, color))
                    nodeColor.put(node.getVariable(), color);
            }
        }
        return this.nodeColor;
    }

    private boolean isValidColor(MyNode node, int color){
        for(String adj : node.getAdj()){
            if(nodeColor.get(adj) == color)
                return false;
        }
        return true;
    }
}
