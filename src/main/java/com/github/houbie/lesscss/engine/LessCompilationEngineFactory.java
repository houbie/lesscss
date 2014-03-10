package com.github.houbie.lesscss.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LessCompilationEngine factory that tries to instantiate the requested engine, but falls back to the RhinoLessCompilationEngine
 */
public class LessCompilationEngineFactory {
    public static final String RHINO = "rhino";
    public static final String NASHORN = "nashorn";
    public static final String COMMAND_LINE = "commandline";

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
     * @param type The engine type. "rhino", "nashorn" and "commandline" are supported out of the box.
     * @return create(type, null)
     */
    public static LessCompilationEngine create(String type) {
        return create(type, null);
    }


    /**
     * Create a new engine of the specified type if available, or a default engine.
     *
     * @param type       The engine type. "rhino", "nashorn" and "commandline" are supported out of the box.
     * @param executable The executable in case of commandline engine
     * @return A new RhinoLessCompilationEngine
     */
    public static LessCompilationEngine create(String type, String executable) {
        if (type == null || RHINO.equals(type)) {
            return create();
        }

        if (COMMAND_LINE.equals(type)) {
            return new CommandLineLesscCompilationEngine(executable);
        }
        return new ScriptEngineLessCompilationEngine(type);
    }
}
