package pt.up.fe.comp2023;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.jasmin.JasminGenerator;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.ollir.Optimization;
import pt.up.fe.comp2023.semantic.Analysis;
import pt.up.fe.comp2023.semantic.MySymbolTable;
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

        // Instantiate Analysis
        Analysis analysis = new Analysis();

        // Semantic Analysis Stage
        JmmSemanticsResult semanticsResult = analysis.semanticAnalysis(parserResult);

        // Output Semantic Errors
        for (Report report : analysis.getReports()) {
            System.out.println(report.toString());
        }

        // Check if there are semantic errors
        TestUtils.noErrors(semanticsResult.getReports());

        Optimization optimization = new Optimization();

        // Apply Constant Propagation and Constant Folding optimizations
        if (Boolean.parseBoolean(config.get("optimize"))) {
            System.out.println("Applying optimizations...");

            optimization.optimize(semanticsResult);

            // Output AST after optimizations
            System.out.println(semanticsResult.getRootNode().toTree());
        }

        OllirResult ollirResult = optimization.toOllir(semanticsResult);

        // Optimize register allocation
        if (Integer.parseInt(config.get("registerAllocation")) >= 0)
            optimization.optimize(ollirResult);

        JasminGenerator jasminGenerator = new JasminGenerator();
        JasminResult jasminResult = jasminGenerator.toJasmin(ollirResult);
        System.out.println(jasminResult.getJasminCode());

        TestUtils.runJasmin(jasminResult.getJasminCode());
    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length < 1)
            throw new RuntimeException("Usage: ./jmm <file_path> [-o] [-p <n>]");

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("debug", "false");
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");

        for (int i = 2; i < args.length; i++) {
            if(args[i].equals("-o"))
                config.put("optimize", "true");

            else if(args[i].equals("-p")) {
                if(i + 1 >= args.length)
                    throw new RuntimeException("Missing argument for -r option.");
                else {
                    try {
                        int n = Integer.parseInt(args[i + 1]);
                        config.put("registerAllocation", Integer.toString(n));
                        i++;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid argument for -r option: " + args[i + 1]);
                    }
                }
            }
        }
        return config;
    }
}
