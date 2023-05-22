package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class JVMInstructionUtils {

    public static int numLocals = 0;
    public static int stackSize = 0;
    public static int currStackSize = 0;

    public static void increaseStackSize(int n) {
        currStackSize += n;
        if (currStackSize > stackSize)
            stackSize = currStackSize;
    }

    public static void decreaseStackSize(int n) {
        currStackSize -= n;
        if (currStackSize > stackSize)
            stackSize = currStackSize;
    }

    public static String getLoadInstruction(Element element, HashMap<String, Descriptor> varTable) {
        increaseStackSize(1);
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

        ElementType elementType;
        if (element instanceof ArrayOperand)
            elementType = ElementType.ARRAYREF;
        else
            elementType = element.getType().getTypeOfElement();
        int virtualReg = varTable.get(((Operand)element).getName()).getVirtualReg();
        if (virtualReg > numLocals)
            numLocals = virtualReg;

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

    public static String getArrayLoadInstruction(ArrayOperand array, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += getLoadInstruction(array, varTable);
        statementList += getLoadInstruction(array.getIndexOperands().get(0), varTable);
        return statementList;
    }

    public static String getStoreInstruction(Element element, HashMap<String, Descriptor> varTable) {
        decreaseStackSize(1);
        int virtualReg = varTable.get(((Operand)element).getName()).getVirtualReg();
        if (virtualReg > numLocals)
            numLocals = virtualReg;

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
            statementList += getLoadInstruction(argument, varTable);
        }
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
        decreaseStackSize(instruction.getListOfOperands().size() + 1);
        return statementList;
    }

    public static String getInvokeStaticInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += loadInvokeArguments(instruction.getListOfOperands(), varTable);
        statementList += "\tinvokestatic " + createInvokeInstructionArgument(instruction, true);
        decreaseStackSize(instruction.getListOfOperands().size());
        return statementList;
    }

    public static String getInvokeSpecialInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        return "\tinvokespecial " + createInvokeInstructionArgument(instruction, false);
    }

    public static String getNewInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += loadInvokeArguments(instruction.getListOfOperands(), varTable);
        statementList += "\tnew " + ((Operand)instruction.getFirstArg()).getName() + '\n';
        statementList += "\tdup\n";
        increaseStackSize(2);
        return statementList;
    }

    public static String getNewArrayInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += loadInvokeArguments(instruction.getListOfOperands(), varTable);
        statementList += "\tnewarray int\n";
        return statementList;
    }

    public static String getArrayLengthInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += getLoadInstruction(instruction.getFirstArg(), varTable);
        statementList += "\tarraylength\n";
        return statementList;
    }

    public static String createUnaryOpStatement(UnaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += getLoadInstruction(instruction.getOperand(), varTable);

        switch (instruction.getOperation().getOpType()) {
            case NOT: case NOTB:
                statementList += "\tifeq ";
                statementList += createAuxBranchStatement();
                decreaseStackSize(1);
                break;
        }
        return statementList;
    }

    public static String createBinaryOpInstruction(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable, boolean isBranchCond) {
        String statementList = "";
        statementList += getLoadInstruction(instruction.getLeftOperand(), varTable);
        statementList += getLoadInstruction(instruction.getRightOperand(), varTable);

        switch (instruction.getOperation().getOpType()) {
            case ADD:
                statementList += "\tiadd\n";
                decreaseStackSize(1);
                break;
            case SUB:
                statementList += "\tisub\n";
                decreaseStackSize(1);
                break;
            case MUL:
                statementList += "\timul\n";
                decreaseStackSize(1);
                break;
            case DIV:
                statementList += "\tidiv\n";
                decreaseStackSize(1);
                break;
            case AND: case ANDB:
                statementList += "\tiand\n";
                decreaseStackSize(1);
                break;
            case OR: case ORB:
                statementList += "\tior\n";
                decreaseStackSize(1);
                break;
            case LTH:
                statementList += "\tif_icmplt ";
                if (!isBranchCond)
                    statementList += createAuxBranchStatement();
                decreaseStackSize(2);
                break;
            case LTE:
                statementList += "\tif_icmple ";
                if (!isBranchCond)
                    statementList += createAuxBranchStatement();
                decreaseStackSize(2);
                break;
            case GTH:
                statementList += "\tif_icmpgt ";
                if (!isBranchCond)
                    statementList += createAuxBranchStatement();
                decreaseStackSize(2);
                break;
            case GTE:
                statementList += "\tif_icmpge ";
                if (!isBranchCond)
                    statementList += createAuxBranchStatement();
                decreaseStackSize(2);
                break;
        }
        return statementList;
    }

    public static String createNoperInstruction(SingleOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        Element operand = instruction.getSingleOperand();
        if (operand instanceof ArrayOperand) {
            String statementList = "";
            statementList += getArrayLoadInstruction((ArrayOperand)operand, varTable);
            statementList += "\tiaload\n";
            decreaseStackSize(1);
            return statementList;
        }
        return getLoadInstruction(operand, varTable);
    }

    public static String createAssignStatement(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        Element assignElement = instruction.getDest();
        String statementList = "";

        if (assignElement instanceof ArrayOperand)
            statementList += getArrayLoadInstruction((ArrayOperand)assignElement, varTable);
        statementList += JasminUtils.handleInstruction(instruction.getRhs(), varTable, true);
        if (assignElement instanceof ArrayOperand) {
            statementList += "\tiastore\n";
            decreaseStackSize(3);
        }
        else
            statementList += getStoreInstruction(assignElement, varTable);
        return statementList;
    }

    public static String createCallStatement(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";

        switch (instruction.getInvocationType()) {
            case NEW:
                if (Objects.equals(((Operand) instruction.getFirstArg()).getName(), "array"))
                    statementList += getNewArrayInstruction(instruction, varTable);
                else
                    statementList += getNewInstruction(instruction, varTable);
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
                statementList += getArrayLengthInstruction(instruction, varTable);
                break;
            case ldc:
                statementList += "\tldc " + ((LiteralElement)instruction.getFirstArg()).getLiteral() + '\n';
                increaseStackSize(1);
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
        ArrayList<Element> arguments = new ArrayList<>();
        arguments.add(instruction.getThirdOperand());

        String statementList = "";
        statementList += getLoadInstruction(instruction.getFirstOperand(), varTable);
        statementList += loadInvokeArguments(arguments, varTable);
        statementList += "\tputfield "
                +  JasminUtils.getTypeDescriptor(instruction.getFirstOperand().getType(), false)
                + '/' + ((Operand)instruction.getSecondOperand()).getName() + " "
                + JasminUtils.getTypeDescriptor(instruction.getThirdOperand().getType(), true) + '\n';
        decreaseStackSize(arguments.size() + 1);
        return statementList;
    }

    public static String createSingleOpConditionStatement(SingleOpCondInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        statementList += createNoperInstruction(instruction.getCondition(), varTable);
        statementList += "\tifne " + instruction.getLabel() + "\n";
        decreaseStackSize(1);
        return statementList;
    }

    public static String createOpConditionStatement(OpCondInstruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        if (instruction.getCondition() instanceof BinaryOpInstruction)
            statementList += createBinaryOpInstruction((BinaryOpInstruction)instruction.getCondition(), varTable, true);
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

    public static String createAuxBranchStatement() {
        String statementList = "";
        // goto true section
        statementList += "true_" + JasminUtils.customLabelCounter + "\n";
        JasminUtils.customLabelCounter++;
        // if condition is false
        statementList += "\ticonst_0\n";
        increaseStackSize(1);
        // skip true section
        statementList += "\tgoto false_" + JasminUtils.customLabelCounter + "\n";
        JasminUtils.customLabelCounter++;
        // true section
        statementList += "\ttrue_" + (JasminUtils.customLabelCounter - 2) + ":\n";
        // if condition is true
        statementList += "\ticonst_1\n";
        increaseStackSize(1);
        // false section (for skipping true section)
        statementList += "\tfalse_" + (JasminUtils.customLabelCounter - 1) + ":\n";
        return statementList;
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
                decreaseStackSize(1);
                break;
            case STRING: case OBJECTREF: case ARRAYREF: case THIS:
                statementList += getLoadInstruction(returnElement, varTable);
                statementList += "\tareturn\n";
                decreaseStackSize(1);
        }
        return statementList;
    }
}
