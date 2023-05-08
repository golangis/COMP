package pt.up.fe.comp2023.jasmin;

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
                return "\ticonst_" + literal + '\n';
            if (literal == -1)
                return "\ticonst_m" + abs(literal) + '\n';
            if (abs(literal) < pow(2, 7))
                return "\tbipush " + literal + '\n';
            if (abs(literal) < pow(2, 15))
                return "\tsipush " + literal + '\n';
            return "\tldc " + literal + '\n';
        }

        ElementType elementType = element.getType().getTypeOfElement();
        int virtualReg = varTable.get(((Operand)element).getName()).getVirtualReg();
        switch (elementType) {
            case THIS:
                return "\taload_0\n";
            case STRING: case OBJECTREF: case ARRAYREF:
                if (virtualReg >= 0 && virtualReg <= 3)
                    return "\taload_" + virtualReg + '\n';
                else
                    return "\taload " + virtualReg + '\n';
            case INT32: case BOOLEAN:
                if (virtualReg >= 0 && virtualReg <= 3)
                    return "\tiload_" + virtualReg + '\n';
                else
                    return "\tiload " + virtualReg + '\n';
        }
        return "";
    }

    public static String getStoreInstruction(Element element, HashMap<String, Descriptor> varTable) {
        int virtualReg = varTable.get(((Operand)element).getName()).getVirtualReg();

        if (element.isLiteral()) {
            int literal = parseInt(((LiteralElement)element).getLiteral());
            if (virtualReg >= 0 && virtualReg <= 3)
                return "\tistore_" + virtualReg + '\n';
            else
                return  "\tistore " + virtualReg + '\n';
        }

        ElementType elementType = element.getType().getTypeOfElement();
        switch (elementType) {
            case THIS:
                return "\tastore_0\n";
            case STRING: case OBJECTREF: case ARRAYREF:
                if (virtualReg >= 0 && virtualReg <= 3)
                    return "\tastore_" + virtualReg + '\n';
                else
                    return  "\tastore " + virtualReg + '\n';
            case INT32: case BOOLEAN:
                if (virtualReg >= 0 && virtualReg <= 3)
                    return "\tistore_" + virtualReg + '\n';
                else
                    return  "\tistore " + virtualReg + '\n';
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
        statementList += "\tinvokevirtual " + createInvokeInstructionArgument(instruction, false);
        return statementList;
    }

    public static String getInvokeStaticInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += loadInvokeArguments(instruction.getListOfOperands(), varTable);
        statementList += "\tinvokestatic " + createInvokeInstructionArgument(instruction, true);
        return statementList;
    }

    public static String getInvokeSpecialInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        return "\tinvokespecial " + createInvokeInstructionArgument(instruction, false);
    }

    public static String getNewInstruction(Operand firstArg) {
        String statementList = "";
        statementList += "\tnew " + firstArg.getName() + '\n';
        statementList += "\tdup\n";
        return statementList;
    }

    public static String createUnaryOpStatement(UnaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += getLoadInstruction(instruction.getOperand(), varTable);

        switch (instruction.getOperation().getOpType()) {
            case NOT: case NOTB:
                statementList += "\tineg\n";
                break;
        }
        return statementList;
    }

    public static String createBinaryOpInstruction(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += getLoadInstruction(instruction.getLeftOperand(), varTable);
        statementList += getLoadInstruction(instruction.getRightOperand(), varTable);

        switch (instruction.getOperation().getOpType()) {
            case ADD:
                statementList += "\tiadd\n";
                break;
            case SUB:
                statementList += "\tisub\n";
                break;
            case MUL:
                statementList += "\timul\n";
                break;
            case DIV:
                statementList += "\tidiv\n";
                break;
            case AND: case ANDB:
                statementList += "\tiand\n";
                break;
            case OR: case ORB:
                statementList += "\tior\n";
                break;
            case LTH:
                statementList += "\tif_icmpgt ";
                break;
            case GTH:
                statementList += "\tif_icmplt ";
                break;
        }
        return statementList;
    }

    public static String createNoperInstruction(SingleOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        return getLoadInstruction(instruction.getSingleOperand(), varTable);
    }

    public static String createAssignStatement(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        Element assignElement = instruction.getDest();

        String statementList = "";
        statementList += JasminUtils.handleInstruction(instruction.getRhs(), varTable, true);
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
            case ldc:
                statementList += "\tldc " + ((LiteralElement)instruction.getFirstArg()).getLiteral() + '\n';
                break;
        }
        return statementList;
    }

    public static String createGetfieldStatement(GetFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += getLoadInstruction(instruction.getFirstOperand(), varTable);
        statementList += "\tgetfield "
                +  JasminUtils.getTypeDescriptor(instruction.getFirstOperand().getType(), false)
                + "/" + ((Operand)instruction.getSecondOperand()).getName() + " "
                + JasminUtils.getTypeDescriptor(instruction.getFieldType(), true) + '\n';
        return statementList;
    }

    public static String createPutfieldStatement(PutFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        ArrayList<Element> aux = new ArrayList<>();
        aux.add(instruction.getThirdOperand());

        String statementList = "";
        statementList += getLoadInstruction(instruction.getFirstOperand(), varTable);
        statementList += loadInvokeArguments(aux, varTable);
        statementList += "\tputfield "
                +  JasminUtils.getTypeDescriptor(instruction.getFirstOperand().getType(), false)
                + '/' + ((Operand)instruction.getSecondOperand()).getName() + " "
                + JasminUtils.getTypeDescriptor(instruction.getThirdOperand().getType(), true) + '\n';
        return statementList;
    }

    public static String createSingleOpConditionStatement(SingleOpCondInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += createNoperInstruction(instruction.getCondition(), varTable);
        statementList += "\tifne " + instruction.getLabel() + "\n";
        return statementList;
    }

    public static String createOpConditionStatement(OpCondInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        if (instruction.getCondition() instanceof BinaryOpInstruction)
            statementList += createBinaryOpInstruction((BinaryOpInstruction)instruction.getCondition(), varTable);
        else
            statementList += createUnaryOpStatement((UnaryOpInstruction)instruction.getCondition(), varTable);
        statementList += instruction.getLabel() + "\n";
        return statementList;
    }

    public static String createBranchStatement(CondBranchInstruction instruction, HashMap<String, Descriptor> varTable) {
        if (instruction instanceof SingleOpCondInstruction)
            return createSingleOpConditionStatement((SingleOpCondInstruction)instruction, varTable);
        if (instruction instanceof OpCondInstruction)
            return createOpConditionStatement((OpCondInstruction)instruction, varTable);
        return "";
    }

    public static String createGotoStatement(GotoInstruction instruction, HashMap<String, Descriptor> varTable) {
        return "\tgoto " + instruction.getLabel() + "\n";
    }

    public static String createReturnStatement(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        ElementType returnType = instruction.getElementType();
        Element returnElement = instruction.getOperand();
        String statementList = "";

        switch (returnType) {
            case VOID:
                return "\treturn\n";
            case INT32: case BOOLEAN:
                statementList += getLoadInstruction(returnElement, varTable);
                statementList += "\tireturn\n";
                break;
            case STRING: case OBJECTREF: case ARRAYREF: case THIS:
                statementList += getLoadInstruction(returnElement, varTable);
                statementList += "\tareturn\n";
        }
        return statementList;
    }
}
