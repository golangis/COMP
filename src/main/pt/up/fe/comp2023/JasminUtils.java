package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;

import java.util.Locale;

public class JasminUtils {

    // TODO: CLASS, THIS ?
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
            return "L" + ((ClassType)type).getName();
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
}
