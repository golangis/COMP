package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;

import java.util.HashMap;

import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class JVMInstructionUtils {

    public static String getLoadInstruction(Element element, HashMap<String, Descriptor> varTable) {
        if (element.isLiteral()) {
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

        ElementType elementType = element.getType().getTypeOfElement();
        switch (elementType) {
            case THIS:
                return "aload_0\n";
            case STRING: case OBJECTREF: case ARRAYREF:
                return "aload_" + varTable.get(((Operand)element).getName()).getVirtualReg()+ '\n';
            case INT32: case BOOLEAN:
                return "iload_" + varTable.get(((Operand)element).getName()).getVirtualReg() + '\n';
        }
        return "";
    }

    public static String getStoreInstruction(Element element, HashMap<String, Descriptor> varTable) {
        return "";
    }

    public static String createAssignStatement(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        Element assignElement = instruction.getDest();
        String statementList = "";

        statementList += JasminUtils.handleInstruction(instruction.getRhs(), varTable);
        statementList += getStoreInstruction(assignElement, varTable);

        return statementList;
    }

    public static String createReturnStatement(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        ElementType returnType = instruction.getElementType();
        Element returnElement = instruction.getOperand();
        String statementList = "";

        switch (returnType) {
            case VOID:
                return "return\n";
            case INT32: case BOOLEAN:
                statementList += getLoadInstruction(returnElement, varTable);
                statementList += "ireturn\n";
                break;
            case STRING: case OBJECTREF: case ARRAYREF: case THIS:
                statementList += getLoadInstruction(returnElement, varTable);
                statementList += "areturn\n";
        }
        return statementList;
    }
}
