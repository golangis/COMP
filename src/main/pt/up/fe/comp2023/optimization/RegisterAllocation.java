package pt.up.fe.comp2023.optimization;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.Method;

import java.util.*;

public class RegisterAllocation {
    private Method method;
    private Map<Instruction, Set<Element>> defs = new HashMap<>();
    private Map<Instruction, Set<Element>> uses = new HashMap<>();
    private Map<Instruction, Set<Element>> in = new HashMap<>();
    private Map<Instruction, Set<Element>> out = new HashMap<>();
    public RegisterAllocation(Method method) {
        this.method = method;
        method.buildCFG();

        for (Instruction instruction : method.getInstructions()){
            this.defs.put(instruction, getDef(instruction));
            this.uses.put(instruction, getUse(instruction));
        }

        computeInOuts();
    }

    public Set<Element> getDef(Instruction instruction){
        //TODO
        return new HashSet<Element>();

    }

    public Set<Element> getUse(Instruction instruction){
        //TODO
        return new HashSet<Element>();
    }

    public void computeInOuts() {
        //TODO
    }
}
