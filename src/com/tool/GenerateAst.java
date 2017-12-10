package com.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(1);
        }

        String outputDir = args[0];
        List<String> types = Arrays.asList(
            "Binary     : Expr left, Token operator, Expr right",
            "Grouping   : Expr expression",
            "Literal    : Object value",
            "Unary      : Token operator, Expr right"
        );
        defineAst(outputDir, "Expr", types);
    }

    private static void defineAst(String outputDir, String basename, List<String> types) throws IOException
    {
        String path = outputDir + "/" + basename + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.lox;");
        writer.println("");
        writer.println("import java.utils.List;");
        writer.println("");
        writer.println("abstract class " + basename + " {");
        writer.println("}");
        writer.close();
    }
}
