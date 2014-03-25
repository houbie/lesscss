package com.github.houbie.lesscss.engine;

import com.github.houbie.lesscss.LessParseException;
import com.github.houbie.lesscss.Options;
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader;
import com.github.houbie.lesscss.resourcereader.ResourceReader;
import com.github.houbie.lesscss.resourcereader.TrackingResourceReader;
import com.github.houbie.lesscss.utils.IOUtils;
import com.github.houbie.lesscss.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.houbie.lesscss.utils.StringUtils.isEmpty;

/**
 * LessCompilationEngine that calls a locally installed lessc via the command line
 */
public class CommandLineLesscCompilationEngine implements LessCompilationEngine {

    public static final String LESSC = "lessc";

    private static final Logger logger = LoggerFactory.getLogger(CommandLineLesscCompilationEngine.class);


    private String executable;
    private String sourceMapFilename;

    public CommandLineLesscCompilationEngine() {
        this(null);
    }

    public CommandLineLesscCompilationEngine(String executable) {
        this.executable = executable != null ? executable : LESSC;
    }

    @Override
    public void initialize(Reader customJavaScriptReader) {
        if (customJavaScriptReader != null) {
            throw new UnsupportedOperationException("Custom javascript is not supported by the command line lessc");
        }
    }

    @Override
    public String compile(String less, CompilationOptions compilationOptions, ResourceReader resourceReader) {
        FileSystemResourceReader fileSystemResourceReader = getFileSystemResourceReader(resourceReader);
        sourceMapFilename = compilationOptions.getSourceMapFilename();

        try {
            if (!compilationOptions.getOptions().isDependenciesOnly() && resourceReader != null) {
                //hack to force reading of imported less files to make sure they can be cached
                forceReadImports(less, compilationOptions, fileSystemResourceReader, resourceReader);
            }
            String[] command = buildCommand(compilationOptions, fileSystemResourceReader, compilationOptions.getOptions().isDependenciesOnly());
            return executeCommandline(less, command);
        } catch (LessParseException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    FileSystemResourceReader getFileSystemResourceReader(ResourceReader resourceReader) {
        if (resourceReader != null) {
            if (resourceReader instanceof TrackingResourceReader) {
                resourceReader = ((TrackingResourceReader) resourceReader).getDelegate();
            }

            if (resourceReader != null) {
                if (!(resourceReader instanceof FileSystemResourceReader)) {
                    throw new UnsupportedOperationException("The command line lessc only accepts a com.github.houbie.lesscss.resourcereader.FileSystemResourceReader");
                }
                return (FileSystemResourceReader) resourceReader;
            }
        }
        return null;
    }

    private void forceReadImports(String less, CompilationOptions compilationOptions, FileSystemResourceReader resourceReader, ResourceReader originalResourceReader) throws IOException, InterruptedException {
        String[] command = buildCommand(compilationOptions, resourceReader, true);
        String[] imports = executeCommandline(less, command).split("\\s+");
        for (int i = 1; i < imports.length; i++) {//first line contains source filename
            originalResourceReader.read(getRelativePath(imports[i], resourceReader));
        }
    }

    private String getRelativePath(String path, FileSystemResourceReader fileSystemResourceReader) {
        String result = new File(path).getAbsolutePath();
        for (File basePath : fileSystemResourceReader.getBaseDirs()) {
            String base = basePath.getAbsolutePath() + '/';
            if (result.startsWith(base)) {
                return path.substring(base.length());
            }
        }
        return result;
    }

    private String executeCommandline(String less, String[] command) throws IOException, InterruptedException {
        String result, errors;
        logger.info("Executing commandline {}", Arrays.deepToString(command));
        Process process = Runtime.getRuntime().exec(command);
        pipe(less, process);
        result = IOUtils.read(process.getInputStream());
        errors = IOUtils.read(process.getErrorStream());
        process.waitFor();
        if (process.exitValue() == 0) {
            return result;
        }
        throw new LessParseException(errors);
    }

    private void pipe(String source, Process process) throws IOException {
        IOUtils.write(source, process.getOutputStream(), "UTF-8");
    }

    protected String[] buildCommand(CompilationOptions compilationOptions, FileSystemResourceReader resourceReader, boolean dependeciesOnly) {
        Options options = compilationOptions.getOptions();
        List<String> cmd = new ArrayList<String>();
        cmd.add(executable);
        cmd.add("-"); // read less from stdin
        if (dependeciesOnly) {
            cmd.add("-M");
            cmd.add("dummy.css"); //destination is required by lessc -M option
        }
        cmd.add("--no-color");

        if (resourceReader != null) {
            addIncludePaths(cmd, resourceReader);
        }
        if (!options.isIeCompat()) cmd.add("--no-ie-compat");
        if (!options.isJavascriptEnabled()) cmd.add("--no-js");
        if (options.isLint()) cmd.add("-l");
        if (options.isSilent()) cmd.add("-s");
        if (options.isStrictImports()) cmd.add("--strict-imports");
        if (options.isCompress()) cmd.add("-x");
        if (options.isMinify()) cmd.add("--clean-css");
        if (options.isSourceMap()) {
            String sourceMap = "--source-map";
            if (!isEmpty(compilationOptions.getSourceMapFilename())) {
                sourceMap += '=' + compilationOptions.getSourceMapFilename();
            }
            cmd.add(sourceMap);
        }
        if (!isEmpty(options.getSourceMapRootpath()))
            cmd.add("--source-map-rootpath=" + options.getSourceMapRootpath());
        if (!isEmpty(options.getSourceMapBasepath()))
            cmd.add("--source-map-basepath=" + options.getSourceMapBasepath());
        if (options.isSourceMapLessInline()) cmd.add("--source-map-less-inline");
        if (options.isSourceMapMapInline()) cmd.add("--source-map-map-inline");
        if (!isEmpty(options.getSourceMapURL())) cmd.add("--source-map-url=" + options.getSourceMapURL());
        if (!isEmpty(options.getRootpath())) cmd.add("--rootpath=" + options.getRootpath());
        if (options.isRelativeUrls()) cmd.add("-ru");
        cmd.add("-sm=" + (options.isStrictMath() ? "on" : "off"));
        cmd.add("-su=" + (options.isStrictUnits() ? "on" : "off"));
        for (Map.Entry<String, String> globalVar : options.getGlobalVars().entrySet()) {
            cmd.add("--global-var=" + globalVar.getKey() + '=' + globalVar.getValue());
        }
        if (!options.getModifyVars().isEmpty()) {
            cmd.add("--modify-var=_dummy_var_=0"); //first modify-var seems to be ignored
            for (Map.Entry<String, String> modifyVar : options.getModifyVars().entrySet()) {
                cmd.add("--modify-var=" + modifyVar.getKey() + '=' + modifyVar.getValue());
            }
        }
        cmd.add("-O" + options.getOptimizationLevel());
        if (options.getDumpLineNumbers() != Options.LineNumbersOutput.NONE) {
            cmd.add("--line-numbers=" + options.getDumpLineNumbers().getOptionString());
        }

        return cmd.toArray(new String[cmd.size()]);
    }

    private void addIncludePaths(List<String> cmd, FileSystemResourceReader resourceReader) {
        File[] baseDirs = resourceReader.getBaseDirs();
        if (baseDirs.length > 0) {
            String includePath = "--include-path=";
            int index = 0;
            for (File dir : baseDirs) {
                includePath += dir.getAbsolutePath();
                if (++index < baseDirs.length) {
                    includePath += File.pathSeparator;
                }
            }
            cmd.add(includePath);
        }
    }


    @Override
    public String getSourceMap() {
        if (!StringUtils.isEmpty(sourceMapFilename)) {
            File sourceMap = new File(sourceMapFilename);
            if (sourceMap.canRead()) {
                try {
                    return IOUtils.read(sourceMap);
                } catch (IOException e) {
                    logger.error("Error while reading source map " + sourceMapFilename, e);
                }
            }
        }
        return null;
    }

    public String getExecutable() {
        return executable;
    }
}
