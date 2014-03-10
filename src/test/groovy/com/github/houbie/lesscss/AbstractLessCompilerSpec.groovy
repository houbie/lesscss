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

import com.github.houbie.lesscss.engine.RhinoLessCompilationEngine
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll

abstract class AbstractLessCompilerSpec extends Specification {

    static LessCompilerImpl compiler

    def setupSpec() {
        Reader reader = new File('src/test/resources/less.js-tests/functions.js').newReader()
        compiler = new LessCompilerImpl(new RhinoLessCompilationEngine(), reader)
    }

    def "compile empty less"() {
        expect:
        compiler.compile('') == ''
    }

    def "compile css"() {
        def css = 'div {\n  color: black;\n}\n'

        expect:
        compiler.compile(css) == css
    }

    def "default constructor"() {
        def compiler = new LessCompilerImpl(new RhinoLessCompilationEngine())

        expect:
        compiler.compile(new File('src/test/resources/less/basic.less')) == new File('src/test/resources/less/basic.css').text
    }

    def "constructor with custom javascript"() {
        def compiler = new LessCompilerImpl(new RhinoLessCompilationEngine(), new File('src/test/resources/less.js-tests/functions.js').text)

        expect:
        compiler.compile(new File('src/test/resources/less/basic.less')) == 'p {\n  color: #000000;\n  width: 2;\n}\n'
    }

    def "compile to file"() {
        compiler.compile(new File('src/test/resources/less/import.less'), new File('build/tmp/import.css'))

        expect:
        new File('build/tmp/import.css').text == new File('src/test/resources/less/import.css').text
    }

    def "compile and minify"() {
        def minified = compiler.compile(new File('src/test/resources/less/minify.less'), new Options(minify: true))

        expect:
        minified == 'p{background-color:#000;margin-left:1px;margin-bottom:1px;margin-top:1px;margin-right:1px}'
    }

    def "less.js compression compatibility test"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/compression/compression.less')

        expect:
        compiler.compile(lessFile, new Options(compress: true)) == getCss(lessFile, 'compression/').text
    }

    @Unroll
    def "less.js global vars compatibility test"() {
        expect:
        compiler.compile(lessFile, new Options(globalVars: getJson(lessFile))) == getCss(lessFile, 'globalVars/').text.replace('/**\n  * Test\n  */\n', '')

        where:
        lessFile << new File('src/test/resources/less.js-tests/less/globalVars').listFiles().findAll {
            it.name.endsWith('.less')
        }
    }

    def "less.js legacy compatibility test"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/legacy/legacy.less')

        expect:
        compiler.compile(lessFile) == getCss(lessFile, 'legacy/').text
    }

    def "less.js static urls compatibility test"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/static-urls/urls.less')

        expect:
        compiler.compile(lessFile, new Options(relativeUrls: false, rootpath: 'folder (1)/')) == getCss(lessFile, 'static-urls/').text
    }

    def getCss(File lessFile, prefix = '', suffix = '') {
        return new File(new File('src/test/resources/less.js-tests/css'), prefix + lessFile.getName().replace('.less', suffix + '.css'))
    }

    def getJson(File lessFile) {
        def file = new File(lessFile.parentFile, lessFile.name.replace('.less', '.json'))
        return new JsonSlurper().parse(file.newReader())
    }

//    def "test a single compatibility test"() {
//        def lessFile = new File('src/test/resources/less.js-tests/less/urls.less')
//
//        expect:
//        compiler.compile(lessFile.text, new FileSystemResourceReader(lessFile.parentFile, lessFile.parentFile.parentFile), new Options(strictMath: true, relativeUrls: true), lessFile.name) == getCss(lessFile).text
//    }
}
