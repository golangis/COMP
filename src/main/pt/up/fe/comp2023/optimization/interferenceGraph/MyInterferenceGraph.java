package pt.up.fe.comp2023.optimization.interferenceGraph;

import java.util.*;

public class MyInterferenceGraph {
    private final List<MyNode> nodes = new ArrayList<>();
    private final Map<String, Integer> nodeColor = new HashMap<>();

    public MyInterferenceGraph deepCopy(){
        MyInterferenceGraph copy = new MyInterferenceGraph();

        for(MyNode node : this.nodes)
            copy.addNode(node.getVariable());
        return copy;
    }

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
        getNode(src).addNeighbour(dest);
        getNode(dest).addNeighbour(src);
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

    public void removeNode(MyNode node){
        for(String neighbour : node.getNeighbours()){
            MyNode neighbourNode = getNode(neighbour);
            neighbourNode.removeNeighbour(node.getVariable());
        }
        this.nodes.remove(node);
    }

    private boolean isValidColor(MyNode node, int color){
        for(String neighbour : node.getNeighbours()){
            if(nodeColor.get(neighbour) == color)
                return false;
        }
        return true;
    }

    public Stack<String> computeMColoringStack(int maxColors){
        Stack<String> stack = new Stack<>();

        while (this.nodes.size() > 0) {
            MyNode nodeToRemove = null;

            for(MyNode node : this.nodes){
                if(node.getDegree() < maxColors){
                    nodeToRemove = node;
                    break;
                }
            }
            if(nodeToRemove != null){
                stack.push(nodeToRemove.getVariable());
                this.removeNode(nodeToRemove);
            }
            else
                throw new RuntimeException("The provided number of registers is not enough to store the variables.");
        }
        return stack;
    }

    public Stack<String> computeOptimalColoringStack(int maxColors){
        Stack<String> stack = new Stack<>();

        while (this.nodes.size() > 0) {
            MyNode nodeToRemove = null;

            for(MyNode node : this.nodes){
                if(node.getDegree() < maxColors){
                    nodeToRemove = node;
                    break;
                }
            }
            if(nodeToRemove != null){
                stack.push(nodeToRemove.getVariable());
                this.removeNode(nodeToRemove);
            }
            else{
                stack.clear();
                return stack;
            }
        }
        return stack;
    }

    public Map<String, Integer> isMColoringFeasible(int maxColors){
        MyInterferenceGraph copyGraph = deepCopy();
        Stack<String> stack = copyGraph.computeMColoringStack(maxColors);

        while (!stack.isEmpty()){
            String nodeName = stack.pop();
            for(int color = 0; color < maxColors; color++){
                if(isValidColor(getNode(nodeName), color))
                    this.nodeColor.put(nodeName, color);
            }
        }
        return this.nodeColor;
    }

    public Map<String, Integer> findOptimalColoring(){
        int maxColors = this.nodes.size();

        for(int currentMaxColors = 1; currentMaxColors <= maxColors; currentMaxColors++){
            this.nodeColor.clear();
            MyInterferenceGraph copyGraph = deepCopy();
            Stack<String> stack = copyGraph.computeOptimalColoringStack(currentMaxColors);

            if(stack.isEmpty())
                continue;

            while (!stack.isEmpty()){
                String nodeName = stack.pop();
                for(int color = 0; color < maxColors; color++){
                    if(isValidColor(getNode(nodeName), color))
                        this.nodeColor.put(nodeName, color);
                }
            }
            break;
        }
        return this.nodeColor;
    }

}
