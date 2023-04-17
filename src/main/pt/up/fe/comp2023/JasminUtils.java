package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;

import java.util.ArrayList;
import java.util.HashMap;

public class JasminUtils {

    public static String getTypeDescriptor(Type type, boolean isDeclaration) {
        ElementType elementType = type.getTypeOfElement();
        if (elementType.equals(ElementType.INT32))
            return "I";
        if (elementType.equals(ElementType.BOOLEAN))
            return "Z";
        if (elementType.equals(ElementType.VOID))
            return "V";
        if (elementType.equals(ElementType.STRING))
            return (isDeclaration) ? "Ljava/lang/String;" : "Ljava/lang/String";
        if (elementType.equals(ElementType.OBJECTREF))
            return (isDeclaration) ? "L" + ((ClassType)type).getName() + ";" : ((ClassType)type).getName();
        if (elementType.equals(ElementType.CLASS) ||
            elementType.equals(ElementType.THIS))
            return ((ClassType)type).getName();
        if (elementType.equals(ElementType.ARRAYREF))
            return "[".repeat(((ArrayType)type).getNumDimensions())
                    + getTypeDescriptor(((ArrayType)type).getElementType(), isDeclaration);
        return "";
    }

    public static String createClassDirective(ClassUnit classUnit) {
        String classDirective = ".class ";
        if (classUnit.getClassAccessModifier() != AccessModifiers.DEFAULT)
            classDirective += classUnit.getClassAccessModifier().toString().toLowerCase() + " ";
        if (classUnit.isFinalClass())
            classDirective += "final ";
        if (classUnit.getPackage() != null)
            classDirective += classUnit.getPackage() + '/';
        classDirective += classUnit.getClassName();

        return classDirective + '\n';
    }

    public static String createSuperDirective(ClassUnit classUnit) {
        String superClassDirective = ".super ";
        if (classUnit.getSuperClass() != null)
            superClassDirective += classUnit.getSuperClass();
        else
            superClassDirective += "java/lang/Object";

        return  superClassDirective + '\n';
    }

    public static String createFieldDirective(Field field) {
        String fieldDirective = ".field ";
        if (field.getFieldAccessModifier() != AccessModifiers.DEFAULT)
            fieldDirective += field.getFieldAccessModifier().toString().toLowerCase() + " ";
        if (field.isStaticField())
            fieldDirective += "static ";
        if (field.isFinalField())
            fieldDirective += "final ";
        fieldDirective += field.getFieldName() + " ";
        fieldDirective += getTypeDescriptor(field.getFieldType(), true);
        if (field.isInitialized())
            fieldDirective += " = " + field.getInitialValue();

        return fieldDirective + '\n';
    }

    public static String createMethodSignature(String methodName, ArrayList<Element> listOfParameters, Type returnType, boolean isDeclaration) {
        String methodSignature = "";
        methodSignature += methodName + "(";
        for (Element parameter: listOfParameters)
            methodSignature += getTypeDescriptor(parameter.getType(), isDeclaration);
        methodSignature += ")" + getTypeDescriptor(returnType, isDeclaration) + '\n';
        return methodSignature;
    }

    public static String createMethodDeclaration(Method method) {
        String methodDirective = "";
        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT)
            methodDirective += method.getMethodAccessModifier().toString().toLowerCase() + " ";
        if (method.isStaticMethod())
            methodDirective += "static ";
        if (method.isFinalMethod())
            methodDirective += "final ";
        methodDirective += createMethodSignature(
                method.getMethodName(),
                method.getParams(),
                method.getReturnType(),
                true
        );
        return methodDirective;
    }

    public static String handleInstruction(Instruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        switch (instruction.getInstType()) {
            case ASSIGN:
                statementList += JVMInstructionUtils.createAssignStatement(
                        (AssignInstruction)instruction,
                        varTable
                );
                break;
            case CALL:
                statementList += JVMInstructionUtils.createCallStatement(
                        (CallInstruction)instruction,
                        varTable
                );
                break;
            case GOTO:
                break;
            case BRANCH:
                break;
            case RETURN:
                statementList += JVMInstructionUtils.createReturnStatement(
                        (ReturnInstruction)instruction,
                        varTable
                );
                break;
            case GETFIELD:
                statementList += JVMInstructionUtils.createGetfieldStatement(
                        (GetFieldInstruction)instruction,
                        varTable
                );
                break;
            case PUTFIELD:
                statementList += JVMInstructionUtils.createPutfieldStatement(
                        (PutFieldInstruction)instruction,
                        varTable
                );
                break;
            case UNARYOPER:
                break;
            case BINARYOPER:
                break;
            case NOPER:
                statementList += JVMInstructionUtils.createInstructionRhs(
                        (SingleOpInstruction)instruction,
                        varTable
                );
                break;
        }
        return statementList;
    }

    public static String handleMethodStatements(Method method) {
        String statementList = "";
        for (Instruction instruction: method.getInstructions())
            statementList += handleInstruction(instruction, method.getVarTable());
        return statementList;
    }

    public static String createConstructMethod(String superClassName) {
        String methodDirective = ".method public <init>()V\n";
        methodDirective += "\taload_0\n";
        methodDirective += "\tinvokespecial ";
        if (superClassName != null)
            methodDirective += superClassName;
        else
            methodDirective += "java/lang/Object";
        methodDirective += "/<init>()V\n";
        methodDirective += "\treturn\n";
        return methodDirective + ".end method\n\n";
    }

    public static String createMethodDirective(Method method) {
        String methodDirective = ".method ";
        methodDirective += createMethodDeclaration(method);
        methodDirective += "\t.limit stack 99\n";
        methodDirective += "\t.limit locals 99\n";
        methodDirective += handleMethodStatements(method);
        return methodDirective + ".end method\n\n";
    }
}
