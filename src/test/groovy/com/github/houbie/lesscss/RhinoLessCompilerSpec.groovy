package com.github.houbie.lesscss

import javax.script.ScriptEngineManager

class RhinoLessCompilerSpec extends AbstractLessCompilerSpec {

    def setupSpec() {
        compiler = createCompiler(new ScriptEngineManager().getEngineByName(LessCompiler.RHINO))
    }
}
