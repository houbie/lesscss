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

package com.github.houbie.lesscss

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.github.houbie.lesscss.engine.LessCompilationEngine
import com.github.houbie.lesscss.engine.RhinoLessCompilationEngine
import com.github.houbie.lesscss.engine.ScriptEngineLessCompilationEngine
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import org.slf4j.LoggerFactory
import spock.lang.IgnoreIf
import spock.lang.Specification

import javax.script.ScriptEngineManager

import static com.github.houbie.lesscss.engine.LessCompilationEngineFactory.*

class BootstrapSpec extends Specification {
    static File source = new File('src/test/resources/less/bootstrap/bootstrap.less')
    static File destination = new File('build/tmp/bootstrap.css')
    static String expected = new File('src/test/resources/less/bootstrap/bootstrap.css').text

    def "compile twitter bootstrap less"() {
        LessCompilerImpl compiler = new LessCompilerImpl(new RhinoLessCompilationEngine())
        compiler.compile(source, destination,
                new Options(relativeUrls: false), new FileSystemResourceReader(source.getParentFile()), null)

        expect:
        destination.text == expected
    }

    @IgnoreIf({ !new ScriptEngineManager().getEngineByName('nashorn') })
    def "compile twitter bootstrap less with nashorn"() {
        LessCompilerImpl compiler = new LessCompilerImpl(create('nashorn'))
        compiler.compile(source, destination,
                new Options(relativeUrls: false), new FileSystemResourceReader(source.getParentFile()), null)

        expect:
        destination.text == expected
    }

    def "compile twitter bootstrap less with commandline lessc"() {
        LessCompilerImpl compiler = new LessCompilerImpl(create('commandline'))
        compiler.compile(source, destination,
                new Options(relativeUrls: false), new FileSystemResourceReader(source.getParentFile()), null)

        expect:
        destination.text == expected
    }

    def "bootstrap with customized variables"() {
        File destination = new File('build/tmp/custom-bootstrap.css')
        destination.delete()

        when:
        Lessc.main('--include-path src/test/resources/less/bootstrap/customized:src/test/resources/less/bootstrap bootstrap.less build/tmp/custom-bootstrap.css'.split(' '))

        then:
        destination.text == new File('src/test/resources/less/bootstrap/customized/custom-bootstrap.css').text
    }

    @IgnoreIf({ System.getProperty("perfTest") == null })
    def "compare performance"() {
        long start = System.currentTimeMillis()

        when:
        LessCompilationEngine engine = create(engineName)
        LessCompilerImpl compiler = new LessCompilerImpl(engine)
        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).level = Level.WARN
        LoggerFactory.getLogger(ScriptEngineLessCompilationEngine).level = Level.WARN
        LoggerFactory.getLogger(FileSystemResourceReader).level = Level.WARN

        then:
        (1..15).each {
            if (it > 1) {
                start = System.currentTimeMillis()
            }
            compiler.compile(source, destination,
                    new Options(relativeUrls: false), new FileSystemResourceReader(source.getParentFile()), null)
            long time = System.currentTimeMillis() - start
            println("$engineName\t$it\t$time")
        }
        destination.text == expected

        where:
        engineName << [RHINO, COMMAND_LINE, NASHORN]
    }
}
