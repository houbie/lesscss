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

package com.github.houbie.lesscss.builder;


import com.github.houbie.lesscss.LessCompiler;
import com.github.houbie.lesscss.LessCompilerImpl;
import com.github.houbie.lesscss.Options;
import com.github.houbie.lesscss.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.houbie.lesscss.LessCompiler.CompilationDetails;

public class CompilationTask {
    private static final Logger logger = LoggerFactory.getLogger(CompilationTask.class);

    private File cacheDir;
    private LessCompiler lessCompiler;
    private Set<CompilationUnit> compilationUnits = new HashSet<>();
    private long customJavaScriptHashCode;

    protected Thread daemon;
    private boolean stopDaemon;

    public CompilationTask() throws IOException {
        this((Reader) null);
    }


    public CompilationTask(File cacheDir) throws IOException {
        this((Reader) null, cacheDir);
    }

    public CompilationTask(File customJavaScript, File cacheDir) throws IOException {
        this(new FileReader(customJavaScript), cacheDir);
    }

    public CompilationTask(Reader customJavaScriptReader) throws IOException {
        this(customJavaScriptReader, null);
    }

    public CompilationTask(Reader customJavaScriptReader, File cacheDir) throws IOException {
        if (customJavaScriptReader != null) {
            String customJavaScript = IOUtils.read(customJavaScriptReader);
            lessCompiler = new LessCompilerImpl(customJavaScript);
            customJavaScriptHashCode = customJavaScript.hashCode();
        } else {
            lessCompiler = new LessCompilerImpl();
        }

        this.cacheDir = (cacheDir == null) ? new File(new File(System.getProperty("user.home")), ".lesscss") : cacheDir;
    }

    public void execute() throws IOException {
        logger.debug("CompilationTask: execute");
        long start = System.currentTimeMillis();
        for (CompilationUnit unit : compilationUnits) {
            compileIfDirty(unit);
        }
        logger.info("execute finished in {} millis", System.currentTimeMillis() - start);
    }

    public void startDaemon(final long interval) {
        if (daemon != null) {
            throw new RuntimeException("Trying to start daemon while it is still running");
        }
        stopDaemon = false;
        daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!stopDaemon) {
                        execute();
                        Thread.sleep(interval);
                    }
                    daemon = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "LessCompilationDaemon");
        daemon.setDaemon(true);
        daemon.start();
    }

    public void stopDaemon() {
        stopDaemon = true;
    }

    private void compileIfDirty(CompilationUnit unit) throws IOException {
        //if the unit is dirty, we need to compile anyway, so no need to refresh
        CompilationUnit unitToCompile = (unit.isDirty()) ? unit : refreshCompilationUnit(unit);

        if (unitToCompile.isDirty()) {
            logger.debug("compiling less: {}", unit);
            long start = System.currentTimeMillis();
            CompilationDetails compilationResult = lessCompiler.compileWithDetails(unit.getSourceAsString(), unit.getImportReader(), unit.getOptions(), unit.getSource().getName());
            IOUtils.writeFile(compilationResult.getResult(), unit.getDestination(), unit.getEncoding());
            updateImportsAndCache(unitToCompile, compilationResult.getImports());
            logger.info("compilation of less {} finished in {} millis", unit, System.currentTimeMillis() - start);
        }
    }

    private CompilationUnit refreshCompilationUnit(CompilationUnit unit) throws IOException {
        CompilationUnit cachedUnit = readFromCache(unit);
        if (cachedUnit == null || !unit.isEquivalent(cachedUnit)) {
            updateImportsAndCache(unit, resolveImports(unit));
            return unit;
        }
        return cachedUnit;
    }

    protected CompilationUnit readFromCache(CompilationUnit unit) {
        File cacheFile = getCacheFile(unit);
        if (cacheFile.canRead()) {
            try {
                return (CompilationUnit) new ObjectInputStream(new FileInputStream(cacheFile)).readObject();
            } catch (Exception e) {
                logger.warn("Could not read cached compilationUnit", e);
            }
        }
        return null;
    }

    private void updateImportsAndCache(CompilationUnit unit, List<String> imports) throws IOException {
        unit.setImports(imports);
        cache(unit);
    }

    private List<String> resolveImports(CompilationUnit unit) throws IOException {
        Options options = new Options(unit.getOptions());
        options.setDependenciesOnly(true);
        CompilationDetails compilationDetails = lessCompiler.compileWithDetails(unit.getSourceAsString(), unit.getImportReader(), options, unit.getSource().getName());
        return compilationDetails.getImports();
    }

    private File getCacheFile(CompilationUnit unit) {
        File dir = new File(cacheDir, Long.toHexString(customJavaScriptHashCode));
        return new File(dir, Long.toHexString(unit.hashCode()));
    }

    private void cache(CompilationUnit unit) {
        try {
            File file = getCacheFile(unit);
            file.getParentFile().mkdir();
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(unit);
            os.flush();
            os.close();
        } catch (IOException e) {
            logger.error("Could not cache compilationUnit", e);
        }
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public LessCompiler getLessCompiler() {
        return lessCompiler;
    }

    protected void setLessCompiler(LessCompiler lessCompiler) {
        this.lessCompiler = lessCompiler;
    }

    public Set<CompilationUnit> getCompilationUnits() {
        return compilationUnits;
    }

    public void setCompilationUnits(Set<CompilationUnit> compilationUnits) {
        this.compilationUnits = compilationUnits;
    }
}
