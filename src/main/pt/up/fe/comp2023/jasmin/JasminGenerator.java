package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Field;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;

public class JasminGenerator implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        String jasminCode = generateJasminCode(ollirResult.getOllirClass());
        return new JasminResult(ollirResult, jasminCode, new ArrayList<Report>());
    }

    private String generateJasminCode(ClassUnit classUnit) {
        return createHeader(classUnit) + '\n'
                + createFieldDefinitions(classUnit) + '\n'
                + createMethodDefinitions(classUnit);
    }

    private String createHeader(ClassUnit classUnit) {
        return JasminUtils.createClassDirective(classUnit)
                + JasminUtils.createSuperDirective(classUnit);
    }

    private String createFieldDefinitions(ClassUnit classUnit) {
        String fieldDefinitions = "";
        for (Field field: classUnit.getFields())
            fieldDefinitions += JasminUtils.createFieldDirective(field);
        return fieldDefinitions;
    }

    private String createMethodDefinitions(ClassUnit classUnit) {
        String methodDefinitions = "";
        for (Method method: classUnit.getMethods())
            if (method.isConstructMethod())
                methodDefinitions += JasminUtils.createConstructMethod(classUnit.getSuperClass());
            else
                methodDefinitions += JasminUtils.createMethodDirective(method);
        return methodDefinitions;
    }
}
