package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;

import java.util.HashMap;

public class JasminUtils {

    public static String getTypeDescriptor(Type type) {
        ElementType elementType = type.getTypeOfElement();
        if (elementType.equals(ElementType.INT32))
            return "I";
        if (elementType.equals(ElementType.BOOLEAN))
            return "Z";
        if (elementType.equals(ElementType.VOID))
            return "V";
        if (elementType.equals(ElementType.STRING))
            return "Ljava/lang/String;";
        if (elementType.equals(ElementType.OBJECTREF))
            return "L" + ((ClassType)type).getName() + ";";
        if (elementType.equals(ElementType.ARRAYREF))
            return "[".repeat(((ArrayType)type).getNumDimensions())
                    + getTypeDescriptor(((ArrayType)type).getElementType());
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
        fieldDirective += getTypeDescriptor(field.getFieldType());
        if (field.isInitialized())
            fieldDirective += " = " + field.getInitialValue();

        return fieldDirective + '\n';
    }

    public static String createMethodSignature(Method method) {
        String methodDirective = "";
        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT)
            methodDirective += method.getMethodAccessModifier().toString().toLowerCase() + " ";
        if (method.isStaticMethod())
            methodDirective += "static ";
        if (method.isFinalMethod())
            methodDirective += "final ";
        if (method.isConstructMethod())
            methodDirective += "<init>(";
        else
            methodDirective += method.getMethodName() + "(";
        for (Element parameter: method.getParams())
            methodDirective += getTypeDescriptor(parameter.getType());
        methodDirective += ")" + getTypeDescriptor(method.getReturnType()) + '\n';

        return methodDirective;
    }

    public static String handleInstruction(Instruction instruction, HashMap<String, Descriptor> varTable) {
        String statementList = "";
        switch (instruction.getInstType()) {
            case ASSIGN:
                break;
            case CALL:
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
                break;
            case PUTFIELD:
                break;
            case UNARYOPER:
                break;
            case BINARYOPER:
                break;
            case NOPER:
                break;
        }
        return statementList;
    }

    public static String handleMethodStatements(Method method) {
        String statementList = "";

        for (Instruction instruction: method.getInstructions())
            handleInstruction(instruction, method.getVarTable());

        if (method.isConstructMethod())
            statementList += "return\n";
        return statementList;
    }

    public static String createMethodDirective(Method method) {
        String methodDirective = ".method ";
        methodDirective += createMethodSignature(method);
        methodDirective += handleMethodStatements(method);
        return methodDirective + ".end method\n\n";
    }
}
