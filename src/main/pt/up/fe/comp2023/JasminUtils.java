package pt.up.fe.comp2023;

import org.specs.comp.ollir.AccessModifiers;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Field;
import org.specs.comp.ollir.Type;

public class JasminUtils {

    public static String getTypeDescriptor(Type type) {
        return "";
    }

    public static String createClassDirective(ClassUnit classUnit) {
        String classDirective = ".class ";
        if (classUnit.getClassAccessModifier() == AccessModifiers.PUBLIC)
            classDirective += "public ";
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
        fieldDirective += field.getFieldAccessModifier().name() + " ";
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
