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
    public static Map<String, String> varEquivalence = new HashMap<>();
    public static Map<String, String> iincVars = new HashMap<>();

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

    public static String checkInc(BinaryOpInstruction instruction, Element dest, HashMap<String, Descriptor> varTable) {
        OperationType operationType = instruction.getOperation().getOpType();
        Element leftOperand = instruction.getLeftOperand();
        Element rightOperand = instruction.getRightOperand();
        String destName = ((Operand)dest).getName();
        String iincVarEquivalent = varEquivalence.get(destName);

        if ((operationType == OperationType.ADD || operationType == OperationType.SUB) &&
            !(leftOperand instanceof LiteralElement) &&
            rightOperand instanceof LiteralElement) {
            String increment = "";
            if (operationType == OperationType.ADD && parseInt(((LiteralElement)rightOperand).getLiteral()) <= 127)
                increment = ((LiteralElement)rightOperand).getLiteral();
            else if (operationType == OperationType.SUB && parseInt(((LiteralElement)rightOperand).getLiteral()) <= 128)
                increment = "-" + ((LiteralElement)rightOperand).getLiteral();
            if (!Objects.equals(increment, "") && iincVarEquivalent != null &&
                iincVarEquivalent.equals(((Operand) leftOperand).getName()))
                iincVars.put(iincVarEquivalent, destName);
            if (!Objects.equals(increment, "") && (destName.equals(((Operand) leftOperand).getName()) ||
                (iincVarEquivalent != null && iincVarEquivalent.equals(((Operand) leftOperand).getName()))))
                return "\tiinc " + varTable.get(((Operand) leftOperand).getName()).getVirtualReg()
                        + " " + increment + "\n";
        }

        if ((operationType == OperationType.ADD || operationType == OperationType.SUB) &&
            leftOperand instanceof LiteralElement &&
            !(rightOperand instanceof LiteralElement)) {
            String increment = "";
            if (operationType == OperationType.ADD && parseInt(((LiteralElement)leftOperand).getLiteral()) <= 127)
                increment = ((LiteralElement)leftOperand).getLiteral();
            else if (operationType == OperationType.SUB && parseInt(((LiteralElement)leftOperand).getLiteral()) <= 128)
                increment = "-" + ((LiteralElement)leftOperand).getLiteral();
            if (!Objects.equals(increment, "") && iincVarEquivalent != null &&
                iincVarEquivalent.equals(((Operand)rightOperand).getName()))
                iincVars.put(iincVarEquivalent, destName);
            if (!Objects.equals(increment, "") && (destName.equals(((Operand)rightOperand).getName()) ||
                (iincVarEquivalent != null && iincVarEquivalent.equals(((Operand)rightOperand).getName()))))
                return "\tiinc " + varTable.get(((Operand) rightOperand).getName()).getVirtualReg()
                        + " " + increment + "\n";
        }

        return "";
    }

    public static String createArithmeticInstruction(OperationType operationType) {
        decreaseStackSize(1);

        switch (operationType) {
            case ADD:
                return "\tiadd\n";
            case SUB:
                return "\tisub\n";
            case MUL:
                return "\timul\n";
            case DIV:
                return "\tidiv\n";
        }
        return "";
    }

    public static String createLogicalInstruction(OperationType operationType) {
        decreaseStackSize(1);

        switch (operationType) {
            case AND: case ANDB:
                return "\tiand\n";
            case OR: case ORB:
                return "\tior\n";
        }
        return "";
    }

    public static String createComparisonInstruction(OperationType operationType, boolean isBranchCond) {
        decreaseStackSize(2);

        switch (operationType) {
            case LTH:
                return isBranchCond ? "\tif_icmplt " : "\tif_icmplt " + createAuxBranchStatement();
            case LTE:
                return isBranchCond ? "\tif_icmple " : "\tif_icmple " + createAuxBranchStatement();
            case GTH:
                return isBranchCond ? "\tif_icmpgt " : "\tif_icmpgt " + createAuxBranchStatement();
            case GTE:
                return isBranchCond ? "\tif_icmpge " : "\tif_icmpge " + createAuxBranchStatement();
        }
        return "";
    }

    public static String createZeroComparisonInstruction(OperationType operationType, boolean isBranchCond) {
        decreaseStackSize(1);

        switch (operationType) {
            case LTH:
                return isBranchCond ? "\tiflt " : "\tiflt " + createAuxBranchStatement();
            case LTE:
                return isBranchCond ? "\tifle " : "\tifle " + createAuxBranchStatement();
            case GTH:
                return isBranchCond ? "\tifgt " : "\tifgt " + createAuxBranchStatement();
            case GTE:
                return isBranchCond ? "\tifge " : "\tifge " + createAuxBranchStatement();
        }
        return "";
    }

    public static String createBinaryOpInstruction(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable, boolean isBranchCond) {
        OperationType operationType = instruction.getOperation().getOpType();
        Element leftOperand = instruction.getLeftOperand();
        Element rightOperand = instruction.getRightOperand();
        String statementList = "";

        switch (operationType) {
            case ADD: case SUB: case MUL: case DIV:
                statementList += getLoadInstruction(leftOperand, varTable);
                statementList += getLoadInstruction(rightOperand, varTable);
                statementList += createArithmeticInstruction(operationType);
                break;
            case AND: case ANDB: case OR: case ORB:
                statementList += getLoadInstruction(leftOperand, varTable);
                statementList += getLoadInstruction(rightOperand, varTable);
                statementList += createLogicalInstruction(operationType);
                break;
            case LTH: case LTE: case GTH: case GTE:
                if (leftOperand instanceof LiteralElement && parseInt(((LiteralElement)leftOperand).getLiteral()) == 0) {
                    statementList += getLoadInstruction(rightOperand, varTable);
                    statementList += createZeroComparisonInstruction(operationType, isBranchCond);
                } else if (rightOperand instanceof LiteralElement && parseInt(((LiteralElement)rightOperand).getLiteral()) == 0) {
                    statementList += getLoadInstruction(leftOperand, varTable);
                    statementList += createZeroComparisonInstruction(operationType, isBranchCond);
                } else {
                    statementList += getLoadInstruction(leftOperand, varTable);
                    statementList += getLoadInstruction(rightOperand, varTable);
                    statementList += createComparisonInstruction(operationType, isBranchCond);
                }
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

    public static boolean checkTempAssign(AssignInstruction instruction) {
        Operand lhs = ((Operand)((AssignInstruction)instruction).getDest());
        Instruction rhsInstruction = ((AssignInstruction)instruction).getRhs();
        if (!(rhsInstruction instanceof SingleOpInstruction))
            return false;
        return ((SingleOpInstruction) rhsInstruction).getSingleOperand() instanceof Operand;
    }

    public static String createAssignStatement(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        Element assignElement = instruction.getDest();
        String statementList = "";

        if (checkTempAssign(instruction)) {
            Element rhsElement = ((SingleOpInstruction)instruction.getRhs()).getSingleOperand();
            String iincVarEquivalent = iincVars.get(((Operand)assignElement).getName());
            if (iincVarEquivalent != null && iincVarEquivalent.equals(((Operand)rhsElement).getName()))
                return "";
        }

        if (instruction.getRhs() instanceof BinaryOpInstruction) {
            statementList = checkInc((BinaryOpInstruction)instruction.getRhs(), assignElement, varTable);
            if (!statementList.equals(""))
                return statementList;
        }

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
