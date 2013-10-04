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


import com.github.houbie.lesscss.utils.IOUtils;
import org.apache.commons.cli.ParseException;

/**
 * Commandline interface for the LESS compiler
 *
 * @author Ivo Houbrechts
 */
public class Lessc {
    public static final String VERSION = "0.9";//TODO get from meta-inf
    public static final String LESS_VERSION = "1.4.1";

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
            LessCompiler lessCompiler = new LessCompilerImpl(cmd.getCustomJsReader());
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
                System.out.print(lessCompiler.compile(IOUtils.read(cmd.getSource(), cmd.getEncoding()), cmd.getIncludePathsReader(), cmd.getOptions(), cmd.getSource().getName()));
            }
        } catch (Exception e) {
            if (cmd.isVerbose()) {
                throw e;
            } else {
                System.err.println(e.getMessage());
            }
        }
    }

    private static String getVersionInfo() {
        return ("LessCompiler version " + VERSION + ", using Less version " + LESS_VERSION);
    }

}