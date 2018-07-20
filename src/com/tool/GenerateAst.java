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
            "Assign     : Token name, Expr value",
            "Binary     : Expr left, Token operator, Expr right",
            "Call       : Expr callee, Token paren, List<Expr> arguments",
            "Get        : Expr object, Token name",
            "Grouping   : List<Expr> expressions",
            "Literal    : Object value",
            "Logical    : Expr left, Token operator, Expr right",
            "Set        : Expr object, Token name, Expr value",
            "This       : Token keyword",
            "Unary      : Token operator, Expr right",
            "Variable   : Token name"
        );
        defineAst(outputDir, "Expr", types);

        types = Arrays.asList(
            "Block      : List<Stmt> statements",
            "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> methods",
            "Expression : Expr expression",
            "Function   : Token name, List<Token> parameters, List<Stmt> body",
            "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
            "Print      : Expr expression",
            "Return     : Token keyword, Expr value",
            "Var        : Token name, Expr initializer",
            "While      : Expr condition, Stmt body"
        );
        defineAst(outputDir, "Stmt", types);
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException
    {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.lox;");
        writer.println("");
        writer.println("import java.util.List;");
        writer.println("");
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // AST классы
        for (String type : types) {
            String[] parts = type.split(":");
            String className = parts[0].trim();
            String fields = parts[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // accept() метод
        writer.println("");
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types)
    {
        writer.println("    interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            String methodName = "visit" + typeName + baseName;
            String arg = typeName + " " + baseName.toLowerCase();
            writer.println("        R " + methodName + "(" + arg + ");");
        }
        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList)
    {
        writer.println("");
        writer.println("    static class " + className + " extends " + baseName + " {");

        // Конструктор (start)
        writer.println("        " + className + "(" + fieldList + ") {");
        // Инициализация полей
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }
        // Конструктор (end)
        writer.println("        }");

        // Поля
        writer.println("");
        for (String field : fields) {
            writer.println("        final " + field + ";");
        }

        // Реализация метода accept()
        writer.println("");
        writer.println("        <R> R accept(Visitor <R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");
    }
}
