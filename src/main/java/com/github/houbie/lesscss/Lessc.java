package com.github.houbie.lesscss;


import com.github.houbie.lesscss.utils.IOUtils;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

public class Lessc {
    public static final String VERSION = "0.9";//TODO get from meta-inf
    public static final String LESS_VERSION = "1.3.3";

    public static void main(String[] args) throws ParseException, IOException {
        LesscCommandLineParser lesscCommandLineParser = new LesscCommandLineParser(getVersionInfo());
        boolean done = lesscCommandLineParser.parse(args);

        if (!done) {
            execute(lesscCommandLineParser);
        }
    }


    private static void execute(LesscCommandLineParser cmd) throws IOException {
        LessCompiler lessCompiler = new LessCompiler(cmd.getCustomJsReader());
        if (cmd.getOptions().isDependenciesOnly()) {
            LessCompiler.CompilationDetails compilationDetails = lessCompiler.compileWithDetails(IOUtils.read(cmd.getSource()), cmd.getIncludePathsReader(), cmd.getOptions(), cmd.getEncoding());
            StringBuilder dependencies = new StringBuilder();
            dependencies.append(cmd.getDestination()).append(": ");
            for (String importPath : compilationDetails.getImports()) {
                dependencies.append(importPath).append(' ');
            }
            System.out.println(dependencies);
        } else if (cmd.getDestination() != null) {
            lessCompiler.compile(cmd.getSource(), cmd.getDestination(), cmd.getOptions(), cmd.getIncludePathsReader(), cmd.getEncoding());
        } else {
            System.out.print(lessCompiler.compile(IOUtils.read(cmd.getSource()), cmd.getIncludePathsReader(), cmd.getOptions(), cmd.getEncoding()));
        }
    }

    private static String getVersionInfo() {
        return ("LessCompiler version " + VERSION + ", using Less version " + LESS_VERSION);
    }

}