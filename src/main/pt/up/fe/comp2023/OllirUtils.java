package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class OllirUtils {

    public static String ollirTypes(Type type){
        String typeS = ""; // array needs to be checked first

        if (type.isArray())
            typeS = ".array";

        if (type.getName().equals("boolean"))
            typeS += ".bool";
        else if (type.getName().equals("int"))
            typeS += ".i32";
        else if (type.getName().equals("void"))
            typeS += ".V";
        else
            typeS += "." + type.getName();

        return typeS;
    }

}
