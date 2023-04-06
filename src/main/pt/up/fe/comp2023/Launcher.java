package pt.up.fe.comp2023;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        // Read contents of input file
        String code = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        // Output AST
        System.out.println(parserResult.getRootNode().toTree());

        // Generate Symbol Table
        MySymbolTable symbolTable = new MySymbolTable(parserResult.getRootNode());

        // Output Symbol Table
        System.out.println(symbolTable.print());

        String ollirCode = "Test {\n" +
            "\n" +
            "\t.construct Test().V {\n" +
            "\t\tinvokespecial(this, \"<init>\").V;\n" +
            "\t}\n" +
            "\n" +
            "\t.method public static main(args.array.String).V {\n" +
            "\t\tret.V;\n" +
            "\t}\n" +
            "\n" +
            "\t\n" +
            "\t.method public foo().i32 {\n" +
            "\t\ta.i32 :=.i32 1.i32;\n" +
            "\t\tb.i32 :=.i32 2.i32;\n" +
            "\n" +
            "\t\tc.i32 :=.i32 a.i32 +.i32 b.i32;\n" +
            "\n" +
            "\t\tret.i32 c.i32;\n" +
            "\t}\n" +
            "}";
        OllirResult ollirResult = new OllirResult(ollirCode, new HashMap<>());

        JasminGenerator jasminGenerator = new JasminGenerator();
        JasminResult jasminResult = jasminGenerator.toJasmin(ollirResult);
        System.out.println(jasminResult.getJasminCode());

        // ... add remaining stages
    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        return config;
    }
}
