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
import com.github.houbie.lesscss.LessParseException;
import com.github.houbie.lesscss.Options;
import com.github.houbie.lesscss.engine.LessCompilationEngine;
import com.github.houbie.lesscss.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static com.github.houbie.lesscss.LessCompiler.CompilationDetails;

/**
 * A CompilationTask can be used to lazy compile one or more LESS files that are set with @see setCompilationUnits
 * The compilation will only be executed if one of the (imported) sources is newer than the resulting CSS.
 * A CompilationTask caches information about imported files by default in ${user.home}\.lesscss
 * This class also provides a daemon that periodically checks for changes in the (imported) LESS sources.
 * This class is typically used in build scripts (ANT, Gradle...).
 *
 * @author Ivo Houbrechts
 */
public class CompilationTask {
    private static final Logger logger = LoggerFactory.getLogger(CompilationTask.class);

    private File cacheDir;
    private LessCompiler lessCompiler;
    private Set<CompilationUnit> compilationUnits = new HashSet<>();
    private CompilationListener compilationListener;
    private long customJavaScriptHashCode;

    protected Thread daemon;
    private boolean stopDaemon;

    /**
     * Default constructor, uses ${user.home}/.lesscss as cache directory.
     *
     * @param engine javascript LessEngine
     * @throws IOException When a resource cannot be read/written
     */
    public CompilationTask(LessCompilationEngine engine) throws IOException {
        this(engine, (Reader) null);
    }


    /**
     * @param engine   javascript LessEngine
     * @param cacheDir The directory where import information will be cached.
     * @throws IOException When a resource cannot be read/written
     */
    public CompilationTask(LessCompilationEngine engine, File cacheDir) throws IOException {
        this(engine, (Reader) null, cacheDir);
    }

    /**
     * @param engine           javascript LessEngine
     * @param customJavaScript File containing custom JavaScript functions (@see LessCompilerImpl)
     * @param cacheDir         The directory where import information will be cached.
     * @throws IOException When a resource cannot be read/written
     */
    public CompilationTask(LessCompilationEngine engine, File customJavaScript, File cacheDir) throws IOException {
        this(engine, new FileReader(customJavaScript), cacheDir);
    }

    /**
     * @param engine                 javascript LessEngine
     * @param customJavaScriptReader Reader for reading custom JavaScript functions (@see LessCompilerImpl)
     * @throws IOException When a resource cannot be read/written
     */
    public CompilationTask(LessCompilationEngine engine, Reader customJavaScriptReader) throws IOException {
        this(engine, customJavaScriptReader, null);
    }

    /**
     * @param engine                 javascript LessEngine
     * @param customJavaScriptReader Reader for reading custom JavaScript functions (@see LessCompilerImpl)
     * @param cacheDir               The directory where import information will be cached.
     * @throws IOException When a resource cannot be read/written
     */
    public CompilationTask(LessCompilationEngine engine, Reader customJavaScriptReader, File cacheDir) throws IOException {
        if (customJavaScriptReader != null) {
            String customJavaScript = IOUtils.read(customJavaScriptReader);
            lessCompiler = new LessCompilerImpl(engine, customJavaScript);
            customJavaScriptHashCode = customJavaScript.hashCode();
        } else {
            lessCompiler = new LessCompilerImpl(engine);
        }

        this.cacheDir = (cacheDir == null) ? new File(new File(System.getProperty("user.home")), ".lesscss") : cacheDir;
    }

    /**
     * Execute the lazy compilation.
     *
     * @return the compilation units that were dirty and got compiled
     * @throws IOException When a resource cannot be read/written
     */
    public Collection<CompilationUnit> execute() throws IOException {
        List<CompilationUnit> compiledUnits = new ArrayList<>();
        logger.debug("CompilationTask: execute");
        long start = System.currentTimeMillis();
        for (CompilationUnit unit : compilationUnits) {
            if (compileIfDirty(unit)) {
                compiledUnits.add(unit);
            }
        }
        logger.debug("execute finished in {} millis", System.currentTimeMillis() - start);
        return compiledUnits;
    }

    /**
     * Start a daemon thread that will execute this CompilationTask periodically.
     *
     * @param interval execution interval in milliseconds
     */
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
                        try {
                            Collection<CompilationUnit> units = execute();
                            if (!units.isEmpty() && compilationListener != null) {
                                compilationListener.notifySuccessfulCompilation(units);
                            }
                        } catch (LessParseException e) {
                            System.out.println(e.getMessage());
                        }
                        Thread.sleep(interval);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                daemon = null;
            }
        }, "LessCompilationDaemon");
        daemon.setDaemon(true);
        daemon.start();
    }

    /**
     * Stop the daemon thread.
     */
    public void stopDaemon() {
        stopDaemon = true;
    }

    /**
     * Compile a CompilationUnit if dirty.
     *
     * @param unit CompilationUnit
     * @return true if the CompilationUnit was dirty
     * @throws IOException
     */
    private boolean compileIfDirty(CompilationUnit unit) throws IOException {
        //if the unit is dirty, we need to compile anyway, so no need to refresh
        CompilationUnit unitToCompile = (unit.isDirty()) ? unit : refreshCompilationUnit(unit);

        if (unitToCompile.isDirty()) {
            logger.debug("compiling less: {}", unit);
            long start = System.currentTimeMillis();

            try {
                CompilationDetails compilationResult = lessCompiler.compileWithDetails(unit.getSourceAsString(), unit.getResourceReader(), unit.getOptions(), unit.getSourceLocation());
                if (unit.getDestination() != null) {
                    IOUtils.writeFile(compilationResult.getResult(), unit.getDestination(), unit.getEncoding());
                }
                updateImportsAndCache(unitToCompile, compilationResult.getImports());
                logger.info("compilation of less {} finished in {} millis", unit, System.currentTimeMillis() - start);
                return true;
            } catch (LessParseException e) {
                unit.setExceptionTimestamp(System.currentTimeMillis());
                cache(unit);
                throw e;
            }
        }
        return false;
    }

    public CompilationUnit refreshCompilationUnit(CompilationUnit unit) throws IOException {
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
        CompilationDetails compilationDetails = lessCompiler.compileWithDetails(unit.getSourceAsString(), unit.getResourceReader(), options, unit.getSourceLocation());
        return compilationDetails.getImports();
    }

    private File getCacheFile(CompilationUnit unit) {
        File dir = new File(cacheDir, Long.toHexString(customJavaScriptHashCode));
        return new File(dir, Long.toHexString(unit.hashCode()));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void cache(CompilationUnit unit) {
        try {
            File file = getCacheFile(unit);
            file.getParentFile().mkdirs();
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

    public CompilationListener getCompilationListener() {
        return compilationListener;
    }

    public void setCompilationListener(CompilationListener compilationListener) {
        this.compilationListener = compilationListener;
    }
}
