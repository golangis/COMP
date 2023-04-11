package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;

import java.util.HashMap;

public class JVMInstructionUtils {

    public static String getLoadInstruction(ElementType elementType, Element element, HashMap<String, Descriptor> varTable) {
        switch (elementType) {
            case THIS:
                return "aload_0\n";
            case STRING: case OBJECTREF: case ARRAYREF:
                return "aload_" + varTable.get(((Operand)element).getName()) + '\n';
        }
        return "";
    }

    public static String createReturnStatement(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statement = "";
        switch (instruction.getReturnType().getTypeOfElement()) {
            case VOID:
                return "return\n";
            case INT32: case BOOLEAN:
                // TODO: iload
                statement += "ireturn\n";
                break;
            case STRING: case OBJECTREF: case ARRAYREF: case THIS:
                // TODO: aload
                statement += "areturn\n";
        }
        return statement;
    }
}
