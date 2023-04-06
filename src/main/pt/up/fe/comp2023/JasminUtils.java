package pt.up.fe.comp2023;

import org.specs.comp.ollir.AccessModifiers;
import org.specs.comp.ollir.ClassUnit;

public class JasminUtils {
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
}
