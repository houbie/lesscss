/*
 * Copyright (c) 2013 Houbrechts IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.houbie.lesscss;


import com.github.houbie.lesscss.builder.CompilationTask;
import com.github.houbie.lesscss.builder.CompilationUnit;
import com.github.houbie.lesscss.engine.LessCompilationEngine;
import com.github.houbie.lesscss.engine.LessCompilationEngineFactory;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Commandline interface for the LESS compiler
 *
 * @author Ivo Houbrechts
 */
public class Lessc {
    public static final String LESS_VERSION = "1.7.0";

    protected static InputStream systemIn = System.in;

    public static void main(String[] args) throws Exception {
        LesscCommandLineParser lesscCommandLineParser = new LesscCommandLineParser(getVersionInfo());
        try {
            boolean done = lesscCommandLineParser.parse(args);

            if (!done) {
                execute(lesscCommandLineParser);
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            LesscCommandLineParser.printHelp();
        }
    }


    private static void execute(LesscCommandLineParser cmd) throws Exception {
        try {
            if (cmd.getOptions().isDependenciesOnly()) {
                printDependencies(cmd);
            } else if (cmd.getDestination() == null) {
                printCompilationResult(cmd);
            } else {
                compileToDestination(cmd);
            }
        } catch (Exception e) {
            if (cmd.isVerbose()) {
                throw e;
            } else {
                System.err.println(e.getMessage());
            }
        }
    }

    private static void printDependencies(LesscCommandLineParser cmd) throws IOException {
        LessCompiler.CompilationDetails compilationDetails = compileWithDetails(cmd);
        System.out.println("Dependencies for " + cmd.getSourceLocation() + ":");
        for (String importPath : compilationDetails.getImports()) {
            System.out.println(importPath);
        }
    }

    private static void printCompilationResult(LesscCommandLineParser cmd) throws IOException {
        LessCompiler.CompilationDetails compilationDetails = compileWithDetails(cmd);
        System.out.println(compilationDetails.getResult());
    }

    private static LessCompiler.CompilationDetails compileWithDetails(LesscCommandLineParser cmd) throws IOException {
        LessCompilationEngine engine = LessCompilationEngineFactory.create(cmd.getEngine());
        LessCompiler lessCompiler = new LessCompilerImpl(engine, cmd.getCustomJsReader());
        return lessCompiler.compileWithDetails(cmd.getResourceReader().read(cmd.getSourceLocation()), cmd.getResourceReader(), cmd.getOptions(), cmd.getSourceLocation());
    }

    private static void compileToDestination(LesscCommandLineParser cmd) throws IOException {
        LessCompilationEngine engine = LessCompilationEngineFactory.create(cmd.getEngine());
        CompilationTask compilationTask = new CompilationTask(engine, cmd.getCustomJsReader(), cmd.getCacheDir());
        CompilationUnit compilationUnit = new CompilationUnit(cmd.getSourceLocation(), cmd.getDestination(), cmd.getOptions(), cmd.getResourceReader(), cmd.getSourceMapFile());
        compilationTask.getCompilationUnits().add(compilationUnit);
        if (cmd.isDaemon()) {
            runDaemon(compilationTask);
        } else {
            compilationTask.execute();
        }
    }

    private static void runDaemon(CompilationTask compilationTask) throws IOException {
        compilationTask.startDaemon(500);
        System.out.println("Compiler daemon running, press q to quit...");
        while (true) {
            if (systemIn.read() == 'q') {
                compilationTask.stopDaemon();
                return;
            }
        }
    }

    private static String getVersionInfo() {
        String version = LessCompiler.class.getPackage().getImplementationVersion();
        return ("LessCompiler version " + version + ", using Less version " + LESS_VERSION);
    }

}