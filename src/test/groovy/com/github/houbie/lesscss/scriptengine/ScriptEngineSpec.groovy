/*
 * Copyright (c) 2013 Houbrechts IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.houbie.lesscss.scriptengine

import com.github.houbie.lesscss.LessCompilerImpl
import spock.lang.Specification
import spock.lang.Unroll

import javax.script.*

class ScriptEngineSpec extends Specification {

    @Unroll
    def "evaluate multiple scripts with the same engine"() {
        ScriptEngineManager factory = new ScriptEngineManager()

        when:
        ScriptEngine scriptEngine = factory.getEngineByName(LessCompilerImpl.RHINO)
        scriptEngine.eval(new StringReader('function f1(){return "result1"} var v1="value1";'))
        scriptEngine.eval(new StringReader('function f2(){return "result2"} var v2="value2";'))

        then:
        scriptEngine.invokeFunction("f1") == 'result1'
        scriptEngine.invokeFunction("f2") == 'result2'
        scriptEngine.get('v1') == 'value1'
        scriptEngine.get('v2') == 'value2'
    }
}
