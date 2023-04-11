package pt.up.fe.comp2023;

import org.specs.comp.ollir.ReturnInstruction;

public class JVMInstructionUtils {

    public static String createReturnStatement(ReturnInstruction instruction) {
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
