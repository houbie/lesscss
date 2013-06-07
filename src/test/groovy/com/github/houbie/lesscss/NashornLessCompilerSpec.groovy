package com.github.houbie.lesscss

import javax.script.ScriptEngineManager

class NashornLessCompilerSpec extends AbstractLessCompilerSpec {
    def setupSpec() {
        compiler = createCompiler(new ScriptEngineManager().getEngineByName(LessCompiler.NASHORN))
    }
}
