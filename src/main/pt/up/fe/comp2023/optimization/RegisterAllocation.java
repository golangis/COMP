package pt.up.fe.comp2023.optimization;

import org.specs.comp.ollir.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import static org.specs.comp.ollir.InstructionType.*;
import static pt.up.fe.comp2023.optimization.OptimizationUtils.*;

public class RegisterAllocation {
    private final Method method;
    private Map<Instruction, Set<Element>> defs = new HashMap<>();
    private Map<Instruction, Set<Element>> uses = new HashMap<>();
    private Map<Instruction, Set<Element>> in = new HashMap<>();
    private Map<Instruction, Set<Element>> out = new HashMap<>();
    public RegisterAllocation(Method method) {
        this.method = method;
        method.buildCFG();

        //TODO: remove
        System.out.println(method.getMethodName());
        System.out.println("------------------");

        for (Instruction instruction : method.getInstructions()){
            this.defs.put(instruction, getDef(instruction));
            this.uses.put(instruction, getUse(instruction, new HashSet<>()));

            //TODO: remove
            instruction.show();
            System.out.println("Defs:" + defs.get(instruction));
            System.out.println("Uses:" + uses.get(instruction));
        }
        //computeLiveInOuts();
    }

    public Set<Element> getDef(Instruction instruction){
        Set<Element> def = new HashSet<>();

        if(instruction.getInstType() == ASSIGN) {
            AssignInstruction assignInst = (AssignInstruction)instruction;
            if(isLocalVar(assignInst.getDest(), this.method))
                def.add(assignInst.getDest());
        }
        return def;
    }

    public Set<Element> getUse(Instruction instruction, Set<Element> result){
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
                        result.add(argument);
                }
            }
            case RETURN -> {
                ReturnInstruction returnInst = (ReturnInstruction) instruction;
                Element returnElement = returnInst.getOperand();
                if (returnElement != null && !returnElement.isLiteral() && isLocalVar(returnElement, this.method))
                    result.add(returnElement);
            }
            case UNARYOPER -> {
                UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) instruction;
                Element operand = unaryOpInstruction.getOperand();
                if (!operand.isLiteral() && isLocalVar(operand, this.method))
                    result.add(operand);
            }
            case BINARYOPER -> {
                BinaryOpInstruction binInst = (BinaryOpInstruction) instruction;
                Element leftOperand = binInst.getLeftOperand();
                Element rightOperand = binInst.getRightOperand();
                if (!leftOperand.isLiteral() && isLocalVar(leftOperand, this.method))
                    result.add(leftOperand);
                if (!rightOperand.isLiteral() && isLocalVar(rightOperand, this.method))
                    result.add(rightOperand);
            }
            case NOPER -> {
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) instruction;
                Element rightOperand = singleOpInstruction.getSingleOperand();
                if (!rightOperand.isLiteral() && isLocalVar(rightOperand, this.method))
                    result.add(rightOperand);
            }
            case PUTFIELD -> {
                PutFieldInstruction putFieldInstruction = (PutFieldInstruction) instruction;
                Element rightOperand = putFieldInstruction.getThirdOperand();
                if (!rightOperand.isLiteral() && isLocalVar(rightOperand, this.method))
                    result.add(rightOperand);
            }
        }
        return result;
    }

    public void computeLiveInOuts() {
        for (Instruction instruction : method.getInstructions()){
            this.in.put(instruction, new HashSet<>());
            this.out.put(instruction, new HashSet<>());
        }

        boolean liveChanged;
        do {
            liveChanged = false;
            for(Instruction instruction : method.getInstructions()){
                //Save current liveIn and liveOut
                Set<Element> liveInAux = new HashSet<>(this.in.get(instruction));
                Set<Element> liveOutAux = new HashSet<>(this.out.get(instruction));

                //Update liveIn
                Set<Element> difference = differenceSets(this.out.get(instruction), this.defs.get(instruction));
                Set<Element> newLiveIn = unionSets(this.uses.get(instruction), difference);

                //Update liveOut
                Set<Element> newLiveOut = new HashSet<>();
                for(Node node : instruction.getSuccessors()){
                    Instruction successor = (Instruction) node;
                    newLiveOut.addAll(this.in.get(successor));
                }
                this.out.put(instruction, newLiveOut);

                //Check if liveIn or liveOut changed
                if(!liveOutAux.equals(newLiveOut) || !liveInAux.equals(newLiveIn))
                    liveChanged = true;
            }
        } while(liveChanged);
    }
}
