package pt.up.fe.comp2023.optimization;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.optimization.interferenceGraph.MyInterferenceGraph;

import java.util.*;

import static org.specs.comp.ollir.InstructionType.*;
import static pt.up.fe.comp2023.optimization.OptimizationUtils.*;

public class RegisterAllocation {
    private final Method method;
    private final int registerAllocationOption;
    private final Map<Node, Set<String>> defs = new HashMap<>();
    private final Map<Node, Set<String>> uses = new HashMap<>();
    private final Map<Node, Set<String>> in = new HashMap<>();
    private final Map<Node, Set<String>> out = new HashMap<>();
    private final MyInterferenceGraph interferenceGraph = new MyInterferenceGraph();
    public RegisterAllocation(Method method, int registerAllocationOption) {
        this.registerAllocationOption = registerAllocationOption;
        this.method = method;

        method.buildCFG();
        for (Instruction instruction : method.getInstructions()){
            this.defs.put(instruction, getDef(instruction));
            this.uses.put(instruction, getUse(instruction, new HashSet<>()));
        }
        computeLiveInOut();
        createInterferenceGraph();

        //TODO: remove
        System.out.println(method.getMethodName());
        System.out.println("------------------");
        for(Instruction instruction : method.getInstructions()){
            instruction.show();
            System.out.println("Defs:" + defs.get(instruction));
            System.out.println("Uses:" + uses.get(instruction));
            System.out.println("In:" + in.get(instruction));
            System.out.println(("Out: "+ out.get(instruction)));
        }
    }

    public Set<String> getDef(Instruction instruction){
        Set<String> def = new HashSet<>();

        if(instruction.getInstType() == ASSIGN) {
            AssignInstruction assignInst = (AssignInstruction)instruction;
            if(isLocalVar(assignInst.getDest(), this.method)){
                Element dest = assignInst.getDest();
                def.add(toVarName(dest));
            }
        }
        return def;
    }

    public Set<String> getUse(Instruction instruction, Set<String> result){
        switch (instruction.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInst = (AssignInstruction) instruction;
                return getUse(assignInst.getRhs(), result);
            }
            case CALL -> {
                CallInstruction callInst = (CallInstruction) instruction;
                List<Element> arguments = callInst.getListOfOperands();
                for (Element argument : arguments) {
                    if (!argument.isLiteral() && isLocalVar(argument, this.method))
                        result.add(toVarName(argument));
                }
            }
            case RETURN -> {
                ReturnInstruction returnInst = (ReturnInstruction) instruction;
                Element returnElement = returnInst.getOperand();
                if (returnElement != null && !returnElement.isLiteral() && isLocalVar(returnElement, this.method))
                    result.add(toVarName(returnElement));
            }
            case UNARYOPER -> {
                UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) instruction;
                Element operand = unaryOpInstruction.getOperand();
                if (!operand.isLiteral() && isLocalVar(operand, this.method))
                    result.add(toVarName(operand));
            }
            case BINARYOPER -> {
                BinaryOpInstruction binInst = (BinaryOpInstruction) instruction;
                Element leftOperand = binInst.getLeftOperand();
                Element rightOperand = binInst.getRightOperand();
                if (!leftOperand.isLiteral() && isLocalVar(leftOperand, this.method))
                    result.add(toVarName(leftOperand));
                if (!rightOperand.isLiteral() && isLocalVar(rightOperand, this.method))
                    result.add(toVarName(rightOperand));
            }
            case NOPER -> {
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) instruction;
                Element rightOperand = singleOpInstruction.getSingleOperand();
                if (!rightOperand.isLiteral() && isLocalVar(rightOperand, this.method))
                    result.add(toVarName(rightOperand));
            }
            case PUTFIELD -> {
                PutFieldInstruction putFieldInstruction = (PutFieldInstruction) instruction;
                Element rightOperand = putFieldInstruction.getThirdOperand();
                if (!rightOperand.isLiteral() && isLocalVar(rightOperand, this.method))
                    result.add(toVarName(rightOperand));
            }
        }
        return result;
    }

    public void computeLiveInOut() {
        for (Instruction instruction : method.getInstructions()){
            this.in.put(instruction, new HashSet<>());
            this.out.put(instruction, new HashSet<>());
        }

        boolean liveChanged;
        do {
            liveChanged = false;
            for(Instruction instruction : method.getInstructions()){
                //Save current liveIn and liveOut
                Set<String> liveInAux = new HashSet<>(this.in.get(instruction));
                Set<String> liveOutAux = new HashSet<>(this.out.get(instruction));

                //Update liveIn
                Set<String> difference = differenceSets(this.out.get(instruction), this.defs.get(instruction));
                Set<String> newLiveIn = unionSets(this.uses.get(instruction), difference);
                this.in.put(instruction, newLiveIn);

                //Update liveOut
                Set<String> newLiveOut = new HashSet<>();

                for(Node successor : instruction.getSuccessors()){
                    Set<String> liveInSuccessor =  this.in.get(successor);
                    newLiveOut = unionSets(newLiveOut, liveInSuccessor);
                }
                this.out.put(instruction, newLiveOut);

                //Check if liveIn or liveOut changed
                if(!liveInAux.equals(newLiveIn) || !liveOutAux.equals(newLiveOut))
                    liveChanged = true;
            }
        } while(liveChanged);
    }

    private void createInterferenceGraph() {
        List<String> localVars = getLocalVars(this.method);

        //Add a node for each variable
        for(String var : localVars)
            this.interferenceGraph.addNode(var);

        //Compute edges
        for(Instruction instruction : this.method.getInstructions()){
            List<String> liveIn = new ArrayList<>(this.in.get(instruction));
            List<String> defAndLiveOut = new ArrayList<>(unionSets(this.defs.get(instruction), this.out.get(instruction)));

            this.interferenceGraph.connectInterferingVariables(liveIn);
            this.interferenceGraph.connectInterferingVariables(defAndLiveOut);
        }
    }
}
