package pt.up.fe.comp2023.optimization;

import org.specs.comp.ollir.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import static org.specs.comp.ollir.InstructionType.ASSIGN;

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
        computeInOuts();
    }

    public Set<Element> getDef(Instruction instruction){
        Set<Element> def = new HashSet<>();

        if(instruction.getInstType() == ASSIGN) {
            AssignInstruction assignInst = (AssignInstruction)instruction;
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
                    if (!argument.isLiteral())
                        result.add(argument);
                }
            }
            case RETURN -> {
                ReturnInstruction returnInst = (ReturnInstruction) instruction;
                Element returnElement = returnInst.getOperand();
                if (returnElement != null && !returnElement.isLiteral())
                    result.add(returnElement);
            }
            case BINARYOPER -> {
                BinaryOpInstruction binInst = (BinaryOpInstruction) instruction;
                if (!binInst.getLeftOperand().isLiteral())
                    result.add(binInst.getLeftOperand());
                if (!binInst.getRightOperand().isLiteral())
                    result.add(binInst.getRightOperand());
            }
            case NOPER -> {
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) instruction;
                Element operand = singleOpInstruction.getSingleOperand();
                if (!operand.isLiteral())
                    result.add(operand);
            }
        }
        return result;
    }

    public void computeInOuts() {
        //TODO
    }
}
