package com.github.houbie.lesscss

import javax.script.ScriptEngineManager

class RhinoLessCompilerTest extends AbstractLessCompilerTest {
    void setup() {
        compiler = new LessCompiler(new ScriptEngineManager().getEngineByName(LessCompiler.RHINO), new File('src/test/resources/less.js-tests/functions.js').text)
    }
}
