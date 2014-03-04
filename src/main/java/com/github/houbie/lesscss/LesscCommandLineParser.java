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


import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader;
import com.github.houbie.lesscss.resourcereader.ResourceReader;
import com.github.houbie.lesscss.utils.LogbackConfigurator;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Commandline parser based on commons cli.
 *
 * @author Ivo Houbrechts
 */
public class LesscCommandLineParser {
    static final String HELP_OPTION = "help";
    static final String INCLUDE_PATH_OPTION = "include-path";
    static final String DEPENDS_OPTION = "depends";
    static final String NO_IE_COMPAT_OPTION = "no-ie-compat";
    static final String NO_JS_OPTION = "no-js";
    static final String LINT_OPTION = "lint";
    static final String SILENT_OPTION = "silent";
    static final String STRICT_IMPORTS_OPTION = "strict-imports";
    static final String VERSION_OPTION = "version";
    static final String COMPRESS_OPTION = "compress";
    static final String SOURCE_MAP_OPTION = "source-map";
    static final String SOURCE_MAP__ROOTPATH_OPTION = "source-map-rootpath";
    static final String SOURCE_MAP__BASEPATH_OPTION = "source-map-basepath";
    static final String SOURCE_MAP_LESS_INLINE_OPTION = "source-map-less-inline";
    static final String SOURCE_MAP_MAP_INLINE_OPTION = "source-map-map-inline";
    static final String SOURCE_MAP_URL_OPTION = "source-map-url";
    static final String ROOT_PATH_OPTION = "rootpath";
    static final String RELATIVE_URLS_OPTION = "relative-urls";
    static final String STRICT_MATH_OPTION = "strict-math";
    static final String STRICT_UNITS_OPTION = "strict-units";
    static final String GLOBAL_VAR_OPTION = "global-var";
    static final String MODIFY_VAR_OPTION = "modify-var";

    static final String CUSTOM_JS_OPTION = "custom-js";
    static final String ENCODING_OPTION = "encoding";
    static final String CACHE_DIR_OPTION = "cache-dir";
    static final String DAEMON_OPTION = "daemon";
    static final String ENGINE_OPTION = "engine";
    static final String VERBOSE_OPTION = "verbose";
    static final String YUI_COMPRESS_OPTION = "yui-compress";

    //deprecated options
    static final String OPTIMIZATION_LEVEL_OPTION = "optimization";
    static final String LINE_NUMBERS_OPTION = "line-numbers";


    static final String MAIN_COMMAND = "lessc";
    static final String HELP_HEADER = "[option option=parameter ...] <source> [destination]";
    static final String HELP_FOOTER = "";

    private final Pattern nameValuePattern = Pattern.compile("(\"|')?(\\w+)=(\\w+)(\"'|')?");

    private final String version;

    private Options options;
    private String sourceLocation;
    private File sourceMapFile;
    private File destination;
    private ResourceReader resourceReader;
    private String encoding;
    private Reader customJsReader;
    private boolean verbose;
    private File cacheDir;
    private boolean daemon;
    private String engine;

    public LesscCommandLineParser(String version) {
        this.version = version;
    }

    public boolean parse(String[] args) throws ParseException, IOException {
        org.apache.commons.cli.Options cliOptions = createOptions();
        CommandLine commandLine = parseCommandLine(args, cliOptions);

        if (printHelp(commandLine, cliOptions) || printVersion(commandLine)) {
            return true;
        }

        process(commandLine);
        return false;
    }

    @SuppressWarnings("static-access")
    private static org.apache.commons.cli.Options createOptions() {
        org.apache.commons.cli.Options result = new org.apache.commons.cli.Options();

        result.addOption("h", HELP_OPTION, false, "Print help (this message) and exit.");
        result.addOption(OptionBuilder.withLongOpt(INCLUDE_PATH_OPTION).hasArg().withDescription("Set include paths. Separated by `:'. Use `;' on Windows.").create());
        result.addOption("M", DEPENDS_OPTION, false, "Output a makefile import dependency list to stdout.");
        result.addOption(OptionBuilder.withLongOpt(NO_IE_COMPAT_OPTION).withDescription("Disable IE compatibility checks.").create());
        result.addOption(OptionBuilder.withLongOpt(NO_JS_OPTION).withDescription("Disable JavaScript in less files").create());
        result.addOption("l", LINT_OPTION, false, "Syntax check only (lint).");
        result.addOption("s", SILENT_OPTION, false, "Suppress output of error messages.");
        result.addOption(OptionBuilder.withLongOpt(STRICT_IMPORTS_OPTION).withDescription("Force evaluation of imports.").create());
        result.addOption("v", VERSION_OPTION, false, "Print version number and exit.");
        result.addOption("x", COMPRESS_OPTION, false, "Compress output by removing some whitespaces.");
        result.addOption(OptionBuilder.withLongOpt(SOURCE_MAP_OPTION).hasOptionalArg().withDescription("--source-map[=FILENAME] Outputs a v3 sourcemap to the filename (or output filename.map)").create());
        result.addOption(OptionBuilder.withLongOpt(SOURCE_MAP__ROOTPATH_OPTION).hasArg().withDescription("adds this path onto the sourcemap filename and less file paths").create());
        result.addOption(OptionBuilder.withLongOpt(SOURCE_MAP__BASEPATH_OPTION).hasArg().withDescription("Sets sourcemap base path, defaults to current working directory.").create());
        result.addOption(OptionBuilder.withLongOpt(SOURCE_MAP_LESS_INLINE_OPTION).withDescription("puts the less files into the map instead of referencing them").create());
        result.addOption(OptionBuilder.withLongOpt(SOURCE_MAP_MAP_INLINE_OPTION).withDescription("puts the map (and any less files) into the output css file").create());
        result.addOption(OptionBuilder.withLongOpt(SOURCE_MAP_URL_OPTION).hasArg().withDescription("the complete url and filename put in the less file").create());
        result.addOption("rp", ROOT_PATH_OPTION, true, "Set rootpath for url rewriting in relative imports and urls. Works with or without the relative-urls option.");
        result.addOption("ru", RELATIVE_URLS_OPTION, false, "re-write relative urls to the base less file.");
        result.addOption("sm", STRICT_MATH_OPTION, false, "Turn on or off strict math, where in strict mode, math requires brackets. This option may default to on and then be removed in the future.");
        result.addOption("su", STRICT_UNITS_OPTION, false, "Allow mixed units, e.g. 1px+1em or 1px*1px which have units that cannot be represented.");
        result.addOption(OptionBuilder.withLongOpt(GLOBAL_VAR_OPTION).hasArg().withDescription("--global-var='VAR=VALUE' Defines a variable that can be referenced by the file.").create());
        result.addOption(OptionBuilder.withLongOpt(MODIFY_VAR_OPTION).hasArg().withDescription("--modify-var='VAR=VALUE' Modifies a variable already declared in the file.").create());
        result.addOption(OptionBuilder.withLongOpt(VERBOSE_OPTION).withDescription("Be verbose.").create());
        result.addOption(OptionBuilder.withLongOpt(YUI_COMPRESS_OPTION).withDescription("Compress output using YUI cssmin.").create());

        //deprecated options
        result.addOption(OptionBuilder.hasArg().withLongOpt(OPTIMIZATION_LEVEL_OPTION).withType(Number.class).withDescription("[deprecated] -O0, -O1, -O2; Set the parser's optimization level. The lower the number, the less nodes it will create in the tree. This could matter for debugging, or if you want to access the individual nodes in the tree.").create('O'));
        result.addOption(OptionBuilder.withLongOpt(LINE_NUMBERS_OPTION).hasArg().withDescription("[deprecated] --line-numbers=TYPE Outputs filename and line numbers. TYPE can be either 'comments', which will output the debug info within comments, 'mediaquery' that will output the information within a fake media query which is compatible with the SASS format, and 'all' which will do both.").create());

        //additional lesscss options
        result.addOption("js", CUSTOM_JS_OPTION, true, "File with custom JavaScript functions.");
        result.addOption("e", ENCODING_OPTION, true, "Character encoding.");
        result.addOption(OptionBuilder.withLongOpt(CACHE_DIR_OPTION).hasArg().withDescription("Cache directory.").create());
        result.addOption(OptionBuilder.withLongOpt(DAEMON_OPTION).withDescription("Start compiler daemon.").create());
        result.addOption(OptionBuilder.hasArg().withLongOpt(ENGINE_OPTION).withDescription("JavaScript engine, either 'rhino' (default), 'nashorn' (requires JDK8) or 'jav8' (only on supported operating systems).").create());

        return result;
    }

    private CommandLine parseCommandLine(String[] args, org.apache.commons.cli.Options cliOptions) throws ParseException {
        CommandLineParser parser = new GnuParser();
        return parser.parse(cliOptions, args);
    }

    private boolean printVersion(CommandLine cmd) {
        if (cmd.hasOption(VERSION_OPTION)) {
            System.out.println(version);
            return true;
        }
        return false;
    }

    private boolean printHelp(CommandLine cmd, org.apache.commons.cli.Options options) {
        if (cmd.hasOption(HELP_OPTION)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(MAIN_COMMAND, HELP_HEADER, options, HELP_FOOTER);
            return true;
        }
        return false;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(MAIN_COMMAND, HELP_HEADER, createOptions(), HELP_FOOTER);
    }

    private void process(CommandLine cmd) throws ParseException, IOException {
        setSourceLocation(cmd);
        setDestinationFile(cmd);
        setVerbosity(cmd);
        setOptions(cmd);
        setEncoding(cmd);
        setResourceReader(cmd);
        setCustomJsReader(cmd);
        setCacheDir(cmd);
        setDaemon(cmd);
        setEngine(cmd);
        checkSourceLocation();
    }

    private void setSourceLocation(CommandLine cmd) throws ParseException {
        if (cmd.getArgs().length < 1) {
            throw new ParseException("<source> is not specified");
        }
        sourceLocation = cmd.getArgs()[0];
    }

    private void setDestinationFile(CommandLine cmd) {
        if (cmd.getArgs().length > 1) {
            destination = new File(cmd.getArgs()[1]);
        }
    }

    private void setVerbosity(CommandLine cmd) {
        verbose = cmd.hasOption(VERBOSE_OPTION);
        //don't log anything if the result has to go to stdout
        LogbackConfigurator.configure(verbose && destination != null, cmd.hasOption(DAEMON_OPTION));
    }

    private void setOptions(CommandLine cmd) throws ParseException {
        options = new Options();

        options.setDependenciesOnly(cmd.hasOption(DEPENDS_OPTION));
        options.setIeCompat(!cmd.hasOption(NO_IE_COMPAT_OPTION));
        options.setJavascriptEnabled(!cmd.hasOption(NO_JS_OPTION));
        options.setLint(cmd.hasOption(LINT_OPTION));
        options.setSilent(cmd.hasOption(SILENT_OPTION));
        options.setStrictImports(cmd.hasOption(STRICT_IMPORTS_OPTION));
        options.setCompress(cmd.hasOption(COMPRESS_OPTION));
        options.setSourceMap(cmd.hasOption(SOURCE_MAP_OPTION));
        options.setSourceMapRootpath(cmd.getOptionValue(SOURCE_MAP__ROOTPATH_OPTION));
        options.setSourceMapBasepath(cmd.getOptionValue(SOURCE_MAP__BASEPATH_OPTION));
        options.setSourceMapLessInline(cmd.hasOption(SOURCE_MAP_LESS_INLINE_OPTION));
        options.setSourceMapMapInline(cmd.hasOption(SOURCE_MAP_MAP_INLINE_OPTION));
        options.setSourceMapUrl(cmd.getOptionValue(SOURCE_MAP_URL_OPTION));
        if (options.isSourceMap() &&
                !options.isSourceMapMapInline()) {
            String sourceMapFileName = cmd.getOptionValue(SOURCE_MAP_OPTION);
            if (sourceMapFileName == null) {
                if (destination == null) {
                    throw new ParseException("The sourcemap option only has an optional filename if the css filename is given");
                }
                sourceMapFile = new File(getDestination().getParentFile(), getDestination().getName() + ".map");
            } else {
                sourceMapFile = new File(sourceMapFileName);
            }
        }

        options.setRootpath(cmd.getOptionValue(ROOT_PATH_OPTION));
        options.setRelativeUrls(cmd.hasOption(RELATIVE_URLS_OPTION));
        options.setStrictMath(cmd.hasOption(STRICT_MATH_OPTION));
        options.setStrictUnits(cmd.hasOption(STRICT_UNITS_OPTION));
        options.setGlobalVars(getValuesMap(cmd.getOptionValues(GLOBAL_VAR_OPTION)));
        options.setModifyVars(getValuesMap(cmd.getOptionValues(MODIFY_VAR_OPTION)));
        options.setMinify(cmd.hasOption(YUI_COMPRESS_OPTION));

        //deprecated options
        if (cmd.hasOption(OPTIMIZATION_LEVEL_OPTION)) {
            options.setOptimizationLevel(((Long) cmd.getParsedOptionValue(OPTIMIZATION_LEVEL_OPTION)).intValue());
        }
        if (cmd.hasOption(LINE_NUMBERS_OPTION)) {
            options.setDumpLineNumbers(Options.LineNumbersOutput.fromOptionString(cmd.getOptionValue(LINE_NUMBERS_OPTION)));
        }
    }

    private Map<String, String> getValuesMap(String[] args) throws ParseException {
        Map<String, String> values = new HashMap<String, String>();
        if (args != null) {
            for (String arg : args) {
                Matcher matcher = nameValuePattern.matcher(arg);
                if (matcher.matches()) {
                    values.put(matcher.group(2), matcher.group(3));
                }
            }
        }
        return values;
    }


    private void setEncoding(CommandLine cmd) {
        encoding = cmd.getOptionValue(ENCODING_OPTION);
    }

    private void setResourceReader(CommandLine cmd) throws ParseException {
        if (cmd.hasOption(INCLUDE_PATH_OPTION)) {
            String[] paths = cmd.getOptionValue(INCLUDE_PATH_OPTION).split("[:|;]");
            File[] files = new File[paths.length];
            for (int i = 0; i < paths.length; i++) {
                files[i] = new File(paths[i]);
            }
            resourceReader = new FileSystemResourceReader(encoding, files);
        } else {
            File source = new File(sourceLocation);
            if (!source.exists()) {
                throw new ParseException(sourceLocation + " can not be found. Check the location or use --" + INCLUDE_PATH_OPTION);
            }
            resourceReader = new FileSystemResourceReader(encoding, source.getParentFile());
        }
    }

    private void setCustomJsReader(CommandLine cmd) {
        if (cmd.hasOption(CUSTOM_JS_OPTION)) {
            try {
                customJsReader = new FileReader(new File(cmd.getOptionValue(CUSTOM_JS_OPTION)));
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Custom JavaScript file " + cmd.getOptionValue(CUSTOM_JS_OPTION) + " could not be read", e);
            }
        }
    }

    private void setCacheDir(CommandLine cmd) {
        if (cmd.hasOption(CACHE_DIR_OPTION)) {
            cacheDir = new File(cmd.getOptionValue(CACHE_DIR_OPTION));
        }
    }

    private void setDaemon(CommandLine cmd) throws ParseException {
        daemon = cmd.hasOption(DAEMON_OPTION);
        if (daemon && destination == null) {
            throw new ParseException("option --daemon requires an output path to be specified");
        }
        if (daemon && options.isDependenciesOnly()) {
            throw new ParseException("option --daemon cannot be used together with -M or --depends option");
        }
    }

    private void setEngine(CommandLine cmd) {
        engine = cmd.getOptionValue(ENGINE_OPTION);
    }

    private void checkSourceLocation() throws ParseException, IOException {
        if (!resourceReader.canRead(sourceLocation)) {
            throw new ParseException(this.sourceLocation + " can not be read");
        }

    }

    public Options getOptions() {
        return options;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public File getSourceMapFile() {
        return sourceMapFile;
    }

    public File getDestination() {
        return destination;
    }

    public ResourceReader getResourceReader() {
        return resourceReader;
    }

    public String getEncoding() {
        return encoding;
    }

    public Reader getCustomJsReader() {
        return customJsReader;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public String getEngine() {
        return engine;
    }
}