/**
 * Copyright 2022 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.comp;

import org.junit.Test;

import static junit.framework.TestCase.fail;

public class GrammarTest {

    private static final String IMPORT = "importDeclaration";
    private static final String MAIN_METHOD = "methodDeclaration";
    private static final String INSTANCE_METHOD = "methodDeclaration";
    private static final String STATEMENT = "statement";
    private static final String EXPRESSION = "expression";

    @Test
    public void testImportSingle() {
        TestUtils.parseVerbose("import bar;", IMPORT);
    }

    @Test
    public void testImportMulti() {
        TestUtils.parseVerbose("import bar.foo.a;", IMPORT);
    }

    @Test
    public void testClass() {
        TestUtils.parseVerbose("class Foo extends Bar {}");
    }

    @Test
    public void testVarDecls() {
        TestUtils.parseVerbose("class Foo {int a; int[] b; int c; boolean d; Bar e;}");
    }

   @Test
    public void testVarDeclString() {
        TestUtils.parseVerbose("String aString;", "varDeclaration");
    }

    @Test
    public void testID1() { TestUtils.parseVerbose("variableName = 1;", STATEMENT); }

    @Test
    public void testID2() { TestUtils.parseVerbose("Variable = 1;", STATEMENT); }

    @Test
    public void testID3() { TestUtils.parseVerbose("variable_name = 1;", STATEMENT); }

    @Test
    public void testID4() { TestUtils.parseVerbose("_ = 1;", STATEMENT); }

    @Test
    public void testID5() { TestUtils.parseVerbose("$ = 1;", STATEMENT); }

    @Test
    public void testID6() { TestUtils.parseVerbose("_variableName = 1;", STATEMENT); }

    @Test
    public void testID7() { TestUtils.parseVerbose("$variableName = 1;", STATEMENT); }

    @Test
    public void testID8() {
        try {
            TestUtils.parseVerbose("1variableName = 1", STATEMENT);
            fail("Should've thrown exception");
        } catch (Exception e) {}
    }

    @Test
    public void testID9() {
        try {
            TestUtils.parseVerbose("variable%name = 1", STATEMENT);
            fail("Should've thrown exception");
        } catch (Exception e) {}
    }

    @Test
    public void testMainMethodEmpty() {
        TestUtils.parseVerbose("static void main(String[] args) {}", MAIN_METHOD);
    }

    @Test
    public void testMainMethodEmpty1() {
        try {
            TestUtils.parseVerbose("static void main(String[] args) {return 0;}", MAIN_METHOD);
            fail("Should've thrown exception");
        } catch (Exception e) {}
    }

    @Test
    public void testInstanceMethodEmpty() {
        TestUtils.parseVerbose("int foo(int anInt, int[] anArray, boolean aBool, String aString) {return a;}", INSTANCE_METHOD);
    }

    @Test
    public void testStmtScope() {
        TestUtils.parseVerbose("{a; b; c;}", STATEMENT);
    }

    @Test
    public void testStmtEmptyScope() {
        TestUtils.parseVerbose("{}", STATEMENT);
    }

    @Test
    public void testStmtIfElse() {
        TestUtils.parseVerbose("if(a){ifStmt1;ifStmt2;}else{elseStmt1;elseStmt2;}", STATEMENT);
    }

    @Test
    public void testStmtIfElseWithoutBrackets() {
        TestUtils.parseVerbose("if(a)ifStmt;else elseStmt;", STATEMENT);
    }

    @Test
    public void testStmtWhile() {
        TestUtils.parseVerbose("while(a){whileStmt1;whileStmt2;}", STATEMENT);
    }

    @Test
    public void testStmtWhileWithoutBrackets() {
        TestUtils.parseVerbose("while(a)whileStmt1;", STATEMENT);
    }

    @Test
    public void testStmtAssign() {
        TestUtils.parseVerbose("a=b;", STATEMENT);
    }

    @Test
    public void testStmtArrayAssign() {
        TestUtils.parseVerbose("anArray[a]=b;", STATEMENT);
    }

    @Test
    public void testExprTrue() {
        TestUtils.parseVerbose("true", EXPRESSION);
    }

    @Test
    public void testExprFalse() {
        TestUtils.parseVerbose("false", EXPRESSION);
    }

    @Test
    public void testExprThis() {
        TestUtils.parseVerbose("this", EXPRESSION);
    }

    @Test
    public void testExprThis1() {
        TestUtils.parseVerbose("this.bar()", EXPRESSION);
    }

    @Test
    public void testExprId() {
        TestUtils.parseVerbose("a", EXPRESSION);
    }

    @Test
    public void testExprIntLiteral() {
        TestUtils.parseVerbose("10", EXPRESSION);
    }

    @Test
    public void testExprIntLiteral1() {
        TestUtils.parseVerbose("0", EXPRESSION);
    }

    @Test
    public void testExprIntLiteral2() {
        TestUtils.parseVerbose("2", EXPRESSION);
    }

    @Test
    public void testExprIntLiteral3() {
        TestUtils.parseVerbose("7010", EXPRESSION);
    }

    @Test
    public void testExprIntLiteral4() {
        try {
            TestUtils.parseVerbose("07", EXPRESSION);
            fail("Should've thrown exception");
        } catch (Exception e) {}
    }

    @Test
    public void testExprParen() {
        TestUtils.parseVerbose("(10)", EXPRESSION);
    }

    @Test
    public void testExprMemberCall() {
        TestUtils.parseVerbose("foo.bar(10, a, true)", EXPRESSION);
    }

    @Test
    public void testExprMemberCallChain() {
        TestUtils.parseVerbose("callee.level1().level2(false, 10).level3(true)", EXPRESSION);
    }

    @Test
    public void testExprLength() {
        TestUtils.parseVerbose("a.length", EXPRESSION);
    }

    @Test
    public void testExprLengthChain() {
        TestUtils.parseVerbose("a.length.length", EXPRESSION);
    }

    @Test
    public void testArrayAccess() {
        TestUtils.parseVerbose("a[10]", EXPRESSION);
    }

    @Test
    public void testArrayAccessChain() {
        TestUtils.parseVerbose("a[10][20]", EXPRESSION);
    }

    @Test
    public void testParenArrayChain() {
        TestUtils.parseVerbose("(a)[10]", EXPRESSION);
    }

    @Test
    public void testCallArrayAccessLengthChain() {
        TestUtils.parseVerbose("callee.foo()[10].length", EXPRESSION);
    }

    @Test
    public void testExprNot() {
        TestUtils.parseVerbose("!true", EXPRESSION);
    }

    @Test
    public void testExprNewArray() {
        TestUtils.parseVerbose("new int[!a]", EXPRESSION);
    }

    @Test
    public void testExprNewClass() {
        TestUtils.parseVerbose("new Foo()", EXPRESSION);
    }

    @Test
    public void testExprMult() {
        TestUtils.parseVerbose("2 * 3", EXPRESSION);
    }

    @Test
    public void testExprDiv() {
        TestUtils.parseVerbose("2 / 3", EXPRESSION);
    }

    @Test
    public void testExprMultChain() {
        TestUtils.parseVerbose("1 * 2 / 3 * 4", EXPRESSION);
    }

    @Test
    public void testExprAdd() {
        TestUtils.parseVerbose("2 + 3", EXPRESSION);
    }

    @Test
    public void testExprSub() {
        TestUtils.parseVerbose("2 - 3", EXPRESSION);
    }

    @Test
    public void testExprAddChain() {
        TestUtils.parseVerbose("1 + 2 - 3 + 4", EXPRESSION);
    }

    @Test
    public void testExprRelational() {
        TestUtils.parseVerbose("1 < 2", EXPRESSION);
    }

    @Test
    public void testExprRelationalChain() {
        TestUtils.parseVerbose("1 < 2 < 3 < 4", EXPRESSION);
    }

    @Test
    public void testExprLogical() {
        TestUtils.parseVerbose("1 && 2", EXPRESSION);
    }

    @Test
    public void testExprLogicalChain() {
        TestUtils.parseVerbose("1 && 2 && 3 && 4", EXPRESSION);
    }

    @Test
    public void testExprChain() {
        TestUtils.parseVerbose("1 && 2 < 3 + 4 - 5 * 6 / 7", EXPRESSION);
    }



    @Test
    public void testComplexClass1() {
        TestUtils.parseVerbose(
            "class Foo {\n" +
            "    public static void main(String[] args) {\n" +
            "        1 < 2 && 2 < 3 + (4 - 5) * 6 / 7;\n" +
            "    }\n" +
            "}");
    }

    @Test
    public void testComplexClass2() {
        TestUtils.parseVerbose(
            "class Foo {\n" +
            "    public static void main(String[] args) {\n" +
            "        boolean a;\n" +
            "        int[] b_array;\n" +
            "        a = 1 < 2 && 2 < 3 + 4 - 5 * 6 / 7;\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testComplexClass3() {
        TestUtils.parseVerbose(
            "class Foo {\n" +
            "    public static void main(String[] args) {\n" +
            "        boolean a;\n" +
            "        int[] b_array;\n" +
            "        int c;\n" +
            "        a = 1 < 2 && 2 < 3 + 4 - 5 * 6 / 7;\n" +
            "        if (!a) {\n" +
            "            c = 1;\n" +
            "        }\n" +
            "        else {\n" +
            "            c = 2;\n" +
            "        }\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testComplexClass4() {
        TestUtils.parseVerbose(
            "class Foo extends FooBar {\n" +
            "    int atr1;\n" +
            "    int atr2;\n" +
            "\n" +
            "    public int bar1() {\n" +
            "        return atr1;\n" +
            "    }\n" +
            "\n" +
            "    public int bar2() {\n" +
            "        return atr2;\n" +
            "    }\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "        this.bar1();\n" +
            "        this.bar2();\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testComplexClassWithComments() {
        TestUtils.parseVerbose(
                "class Bar extends FooBar {\n" +
                        "//This is a single-line Java comment" + "\n" +
                        "//Class fields:" + "\n" +
                        "    int atr1;\n" +
                        "    int atr2;\n" +
                        "\n" +
                        "    public int foo() {\n" +
                        "        return atr1;\n" +
                        "    }\n" +
                        "\n" +
                        "/*" + "\n" +
                        "* The starting point for program execution" +
                        "* @args: Configuration parameters passed into the main function\n" +
                        "*/" + "\n" +
                        "    public static void main(String[] args) {\n" +
                        "        this.foo();\n" +
                        "    }\n" +
                        "}"
        );
    }
}
