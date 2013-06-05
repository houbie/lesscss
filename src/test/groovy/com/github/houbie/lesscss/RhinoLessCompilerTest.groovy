package com.github.houbie.lesscss

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class RhinoLessCompilerTest extends AbstractLessCompilerTest {

    ScriptEngine getScriptEngine() {
        return new ScriptEngineManager().getEngineByName(LessCompiler.RHINO)
    }

//    def "less.js compatibility"() {
//        def lessFile = new File('src/test/resources/less.js-tests/less/urls.less')
//        expect:
//        compiler.compile(lessFile,) == getCss(lessFile).text
//    }

}
