package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;

import java.util.ArrayList;
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
        int virtualReg = varTable.get(((Operand)element).getName()).getVirtualReg();
        switch (elementType) {
            case THIS:
                return "aload_0\n";
            case STRING: case OBJECTREF: case ARRAYREF:
                if (virtualReg >= 0 && virtualReg <= 3)
                    return "aload_" + virtualReg + '\n';
                else
                    return "aload " + virtualReg + '\n';
            case INT32: case BOOLEAN:
                if (virtualReg >= 0 && virtualReg <= 3)
                    return "iload_" + virtualReg + '\n';
                else
                    return "iload " + virtualReg + '\n';
        }
        return "";
    }

    public static String getStoreInstruction(Element element, HashMap<String, Descriptor> varTable) {
        int virtualReg = varTable.get(((Operand)element).getName()).getVirtualReg();

        if (element.isLiteral()) {
            int literal = parseInt(((LiteralElement)element).getLiteral());
            if (virtualReg >= 0 && virtualReg <= 3)
                return "istore_" + virtualReg + '\n';
            else
                return  "istore " + virtualReg + '\n';
        }

        ElementType elementType = element.getType().getTypeOfElement();
        switch (elementType) {
            case THIS:
                return "astore_0\n";
            case STRING: case OBJECTREF: case ARRAYREF:
                if (virtualReg >= 0 && virtualReg <= 3)
                    return "astore_" + virtualReg + '\n';
                else
                    return  "astore " + virtualReg + '\n';
            case INT32: case BOOLEAN:
                if (virtualReg >= 0 && virtualReg <= 3)
                    return "istore_" + virtualReg + '\n';
                else
                    return  "istore " + virtualReg + '\n';
        }
        return "";
    }

    public static String loadInvokeArguments(ArrayList<Element> listOfOperands, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        for (Element argument: listOfOperands) {
            statementList += getLoadInstruction(argument, varTable); }
        return statementList;
    }

    public static String createInvokeInstructionArgument(CallInstruction instruction, boolean isStatic) {
        return (isStatic ? ((Operand)instruction.getFirstArg()).getName() :
                JasminUtils.getTypeDescriptor(instruction.getFirstArg().getType(), false)) +
                "/" + JasminUtils.createMethodSignature(
                    ((LiteralElement)instruction.getSecondArg()).getLiteral().replace("\"", ""),
                    instruction.getListOfOperands(),
                     instruction.getReturnType(),
                    true
                );
    }

    public static String getInvokeVirtualInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += getLoadInstruction(instruction.getFirstArg(), varTable);
        statementList += loadInvokeArguments(instruction.getListOfOperands(), varTable);
        statementList += "invokevirtual " + createInvokeInstructionArgument(instruction, false);
        return statementList;
    }

    public static String getInvokeStaticInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += loadInvokeArguments(instruction.getListOfOperands(), varTable);
        statementList += "invokestatic " + createInvokeInstructionArgument(instruction, true);
        return statementList;
    }

    public static String getInvokeSpecialInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        return "invokespecial " + createInvokeInstructionArgument(instruction, false);
    }

    public static String getNewInstruction(Operand firstArg) {
        String statementList = "";
        statementList += "new " + firstArg.getName() + '\n';
        statementList += "dup\n";
        return statementList;
    }

    public static String createInstructionRhs(SingleOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        return getLoadInstruction(instruction.getSingleOperand(), varTable);
    }

    public static String createAssignStatement(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        Element assignElement = instruction.getDest();
        String statementList = "";

        statementList += JasminUtils.handleInstruction(instruction.getRhs(), varTable);
        statementList += getStoreInstruction(assignElement, varTable);

        return statementList;
    }

    public static String createCallStatement(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";

        switch (instruction.getInvocationType()) {
            case NEW:
                statementList += getNewInstruction((Operand)instruction.getFirstArg());
                break;
            case invokespecial:
                statementList += getInvokeSpecialInstruction(instruction, varTable);
                break;
            case invokestatic:
                statementList += getInvokeStaticInstruction(instruction, varTable);
                break;
            case invokevirtual:
                statementList += getInvokeVirtualInstruction(instruction, varTable);
                break;
            case arraylength:
                break;
        }
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
