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

        if(Boolean.parseBoolean(config.get("optimize"))){
            System.out.println("Applying optimizations...");
            //TODO: apply optimizations (constant propagation and constant folding)
        }

        Optimization optimization = new Optimization();
        OllirResult ollirResult = optimization.toOllir(semanticsResult);

        JasminGenerator jasminGenerator = new JasminGenerator();
        JasminResult jasminResult = jasminGenerator.toJasmin(ollirResult);
        System.out.println(jasminResult.getJasminCode());

        TestUtils.runJasmin(jasminResult.getJasminCode());
    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length < 1) {
            throw new RuntimeException("Usage: ./jmm <file_path> [-o]");
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        if (Arrays.asList(args).contains("-o"))
            config.put("optimize", "true");
        else
            config.put("optimize", "false");

        return config;
    }
}
