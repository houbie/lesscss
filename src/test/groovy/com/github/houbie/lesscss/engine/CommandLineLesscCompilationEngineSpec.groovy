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

package com.github.houbie.lesscss.engine

import com.github.houbie.lesscss.LessCompilerImpl
import com.github.houbie.lesscss.Options
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import spock.lang.Specification

import static com.github.houbie.lesscss.engine.LessCompilationEngineFactory.COMMAND_LINE

class CommandLineLesscCompilationEngineSpec extends Specification {
    def 'build command'() {
        def engine = new CommandLineLesscCompilationEngine(System.getProperty('lesscExecutable', 'lessc'))
        def resourceReader = new FileSystemResourceReader(new File('include1'), new File('../include2'))

        expect:
        engine.buildCommand(compilationOptions, resourceReader, false).join(' ') == commandLine

        where:
        compilationOptions                                                            | commandLine
        new CompilationOptions(new Options(), 'source.less', 'destination.css', null) | 'lessc - --no-color --include-path=/Users/ivo/java/lesscss/include1:/Users/ivo/java/lesscss/../include2 -sm=off -su=off -O1'
        new CompilationOptions(new Options(
                compress: true,
                optimizationLevel: 2,
                strictImports: true,
                rootpath: 'rootpath',
                relativeUrls: true,
                dumpLineNumbers: Options.LineNumbersOutput.ALL,
                dependenciesOnly: true,
                strictMath: true,
                strictUnits: true,
                ieCompat: false,
                javascriptEnabled: false,
                lint: true,
                silent: true,
                sourceMap: true,
                sourceMapRootpath: 'sourceMapRootpath',
                sourceMapBasepath: 'sourceMapBasepath',
                sourceMapLessInline: 'sourceMapLessInline',
                sourceMapMapInline: 'sourceMapMapInline',
                sourceMapURL: 'sourceMapUrl',
                globalVars: [globalVars: 'globalVarValue'],
                modifyVars: [modifyVars: 'modifyVarValue'],
                minify: true
        ), 'source.less', 'destination.css', 'sourceMap.map')                         | 'lessc - --no-color --include-path=/Users/ivo/java/lesscss/include1:/Users/ivo/java/lesscss/../include2 --no-ie-compat --no-js -l -s --strict-imports -x --clean-css --source-map=sourceMap.map --source-map-rootpath=sourceMapRootpath --source-map-basepath=sourceMapBasepath --source-map-less-inline --source-map-map-inline --source-map-url=sourceMapUrl --rootpath=rootpath -ru -sm=on -su=on --global-var=globalVars=globalVarValue --modify-var=_dummy_var_=0 --modify-var=modifyVars=modifyVarValue -O2 --line-numbers=all'

    }

    def 'compile basic less'() {
        def engine = new CommandLineLesscCompilationEngine(System.getProperty('lesscExecutable', 'lessc'))
        def compiler = new LessCompilerImpl(engine)
        def lessFile = new File('src/test/resources/less/import.less')
        def compilationDetails = compiler.compileWithDetails(lessFile.text, new FileSystemResourceReader(lessFile.parentFile), new Options(), lessFile.name)

        expect:
        compilationDetails.result == new File('src/test/resources/less/import.css').text
        compilationDetails.imports*.replace('\\', '/').sort() == ['import1/commonImported.less', 'import1/import2/commonImported.less', 'import1/import2/imported2.less', 'import1/imported1.less', 'imported0.less']
    }
}
