package com.github.houbie.lesscss

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class NashornLessCompilerTest extends AbstractLessCompilerTest {
    ScriptEngine getScriptEngine() {
        return new ScriptEngineManager().getEngineByName(LessCompiler.RHINO)
    }
}
