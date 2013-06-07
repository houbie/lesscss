package com.github.houbie.lesscss.scriptengine

import com.github.houbie.lesscss.LessCompiler
import spock.lang.Specification
import spock.lang.Unroll

import javax.script.*

class ScriptEngineSpec extends Specification {

    @Unroll
    def "evaluate multiple scripts with the same engine with #engineName"() {
        ScriptEngineManager factory = new ScriptEngineManager()

        when:
        ScriptEngine scriptEngine = factory.getEngineByName(engineName)
        scriptEngine.eval(new StringReader('function f1(){return "result1"} var v1="value1";'))
        scriptEngine.eval(new StringReader('function f2(){return "result2"} var v2="value2";'))

        then:
        scriptEngine.invokeFunction("f1") == 'result1'
        scriptEngine.invokeFunction("f2") == 'result2'
        scriptEngine.get('v1') == 'value1'
        scriptEngine.get('v2') == 'value2'

        where:
        engineName << [LessCompiler.NASHORN, LessCompiler.RHINO]
    }
}
