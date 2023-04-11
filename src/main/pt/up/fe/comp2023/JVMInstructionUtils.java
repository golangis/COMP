package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;

import java.util.HashMap;

import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class JVMInstructionUtils {

    public static String getLoadInstruction(ElementType elementType, Element element, HashMap<String, Descriptor> varTable) {
        switch (elementType) {
            case THIS:
                return "aload_0\n";
            case STRING: case OBJECTREF: case ARRAYREF:
                return "aload_" + varTable.get(((Operand)element).getName()).getVirtualReg()+ '\n';
            case BOOLEAN:
                return "iload_" + varTable.get(((Operand)element).getName()).getVirtualReg() + '\n';
            case INT32:
                int literal = parseInt(((LiteralElement)element).getLiteral());
                if (literal >= 0 && literal <= 5)
                    return "iconst_" + literal + '\n';
                if (literal == -1)
                    return "iconst_m" + abs(literal) + '\n';
                if (abs(literal) < pow(2, 7))
                    return "bipush " + literal + '\n';
                if (abs(literal) < pow(2, 15))
                    return "sipush " + literal + '\n';
                return "ldc " + literal + '\n';
        }
        return "";
    }

    public static String createReturnStatement(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        ElementType returnType = instruction.getElementType();
        Element returnElement = instruction.getOperand();
        String statement = "";

        switch (returnType) {
            case VOID:
                return "return\n";
            case INT32: case BOOLEAN:
                statement += getLoadInstruction(returnType, returnElement, varTable);
                statement += "ireturn\n";
                break;
            case STRING: case OBJECTREF: case ARRAYREF: case THIS:
                statement += getLoadInstruction(returnType, returnElement, varTable);
                statement += "areturn\n";
        }
        return statement;
    }
}
