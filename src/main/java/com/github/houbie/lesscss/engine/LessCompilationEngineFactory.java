package com.github.houbie.lesscss.engine;

import com.github.houbie.lesscss.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * LessCompilationEngine factory that tries to instantiate the requested engine, but falls back to the RhinoLessCompilationEngine
 */
public class LessCompilationEngineFactory {
    public static final String RHINO = "rhino";
    public static final String NASHORN = "nashorn";
    public static final String JAV8 = "jav8";

    private static Logger logger = LoggerFactory.getLogger(LessCompilationEngineFactory.class);

    /**
     * Create the default engine.
     *
     * @return a instance off {@link com.github.houbie.lesscss.engine.RhinoLessCompilationEngine}
     */
    public static LessCompilationEngine create() {
        logger.info("creating default Rhino less compilation engine");
        return new RhinoLessCompilationEngine();
    }

    /**
     * Create a new engine of the specified type if available, or a default engine.
     *
     * @param type The engine type. "rhino", "nashorn" and "jav8" are supported out of the box.
     * @return create(type, null)
     */
    public static LessCompilationEngine create(String type) {
        return create(type, null);
    }


    /**
     * Create a new engine of the specified type if available, or a default engine.
     *
     * @param type     The engine type. "rhino", "nashorn" and "jav8" are supported out of the box.
     * @param cacheDir The directory where binaries will be cached when type = "jav8". If null, ${user.home}/.lesscss is used.
     * @return A new RhinoLessCompilationEngine
     */
    public static LessCompilationEngine create(String type, File cacheDir) {
        if (type == null || RHINO.equals(type)) {
            return create();
        }

        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(type);
        if (scriptEngine == null) {
            if (JAV8.equals(type)) {
                scriptEngine = createJav8Engine(cacheDir);
            } else {
                logger.warn("The script engine {} could not be loaded", type);
            }
        }
        return (scriptEngine != null) ? new ScriptEngineLessCompilationEngine(scriptEngine) : create();
    }

    private static ScriptEngine createJav8Engine(File cacheDir) {
        //the fact that we arrived here means that jav8 is not on the classpath
        try {
            URL jav8Jar = copyJav8Jar(cacheDir);
            if (jav8Jar != null) {

                ClassLoader classLoader = new URLClassLoader(new URL[]{jav8Jar}, LessCompilationEngineFactory.class.getClassLoader());
                ScriptEngineFactory v8ScriptEngineFactory = (ScriptEngineFactory) classLoader.loadClass("lu.flier.script.V8ScriptEngineFactory").newInstance();
                return v8ScriptEngineFactory.getScriptEngine();
            }
        } catch (Exception e) {
            logger.warn("The jav8 script engine could not be created: {}", e.getMessage());
        }
        return null;
    }

    private static URL copyJav8Jar(File cacheDir) throws IOException {
        String jav8JarName = getJav8JarName();
        if (jav8JarName == null) {
            logger.warn("jav8 is not available for this operating system");
            return null;
        }
        InputStream inputStream = LessCompilationEngineFactory.class.getClassLoader().getResourceAsStream(jav8JarName);
        if (inputStream == null) {
            throw new RuntimeException("jav8 jar not found in the classpath");
        }

        if (cacheDir == null) {
            cacheDir = new File(new File(System.getProperty("user.home")), ".lesscss");
        }
        File jav8Jar = new File(cacheDir, jav8JarName);
        logger.debug("copying {} to {}", jav8JarName, cacheDir);
        cacheDir.getParentFile().mkdirs();
        jav8Jar.createNewFile();
        IOUtils.copyLarge(inputStream, new FileOutputStream(jav8Jar));
        return jav8Jar.toURI().toURL();
    }

    public static String getJav8JarName() {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        if (osName.toLowerCase().contains("mac os x") && osArch.contains("64")) {
            return "jav8-jsr223-Mac OS X-x86_64-0.6.jar";
        }
        if (osName.toLowerCase().contains("windows") && System.getenv("ProgramW6432") != null) {
            return "jav8-jsr223-win-amd64-0.6.jar";
        }
        if (osName.toLowerCase().contains("linux") && osArch.contains("64")) {
            return "jav8-jsr223-win-amd64-0.6.jar";
        }
        return null;
    }
}
