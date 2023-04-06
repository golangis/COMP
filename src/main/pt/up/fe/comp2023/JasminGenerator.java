package pt.up.fe.comp2023;

import org.specs.comp.ollir.ClassUnit;
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
        return createHeader(classUnit)
                + createFieldDefinitions(classUnit)
                + createMethodDefinitions(classUnit);
    }

    private String createHeader(ClassUnit classUnit) {
        return JasminUtils.createClassDirective(classUnit)
                + JasminUtils.createSuperDirective(classUnit);
    }

    private String createFieldDefinitions(ClassUnit classUnit) {
        return "";
    }

    private String createMethodDefinitions(ClassUnit classUnit) {
        return "";
    }
}
