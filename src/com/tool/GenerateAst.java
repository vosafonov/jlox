package com.tool;

import java.io.IOException;

public class GenerateAst {
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(1);
        }
        String outputDir = args[0];
    }
}
