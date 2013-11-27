package com.github.houbie.lesscss.engine

import spock.lang.IgnoreIf
import spock.lang.Specification


class LessCompilationEngineFactoryTest extends Specification {
    def "test create default engine"() {
        expect:
        LessCompilationEngineFactory.create() instanceof RhinoLessCompilationEngine
    }

    @IgnoreIf({ LessCompilationEngineFactory.jav8JarName == null })
    def "test create jav8 engine"() {
        expect:
        LessCompilationEngineFactory.create('jav8', new File('build/tmp')).scriptEngine.class.simpleName == 'V8ScriptEngine'
    }
}
