package com.github.houbie.lesscss

import javax.script.ScriptEngineManager

class NashornLessCompilerTest extends AbstractLessCompilerTest {
    void setup() {
        compiler = new LessCompiler(new ScriptEngineManager().getEngineByName(LessCompiler.NASHORN), new File('src/test/resources/less.js-tests/functions.js').text)
    }
}
