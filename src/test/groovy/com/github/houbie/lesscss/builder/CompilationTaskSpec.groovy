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

package com.github.houbie.lesscss.builder

import com.github.houbie.lesscss.LessCompiler
import com.github.houbie.lesscss.Options
import com.github.houbie.lesscss.engine.CommandLineLesscCompilationEngine
import com.github.houbie.lesscss.engine.RhinoLessCompilationEngine
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import spock.lang.Specification

import static com.github.houbie.lesscss.Options.LineNumbersOutput.COMMENTS

class CompilationTaskSpec extends Specification {
    File workDir = new File('build/tmp/compilationTask')
    File cacheDir = new File("build/tmp/testCacheDir")

    File importSource = new File(workDir, 'import.less')
    File imported0Source = new File(workDir, 'imported0.less')
    File importResult = new File('src/test/resources/less/import.css')
    File importDestination = new File(workDir, 'import.css')
    CompilationUnit importUnit = new CompilationUnit(importSource.canonicalPath, importDestination, new Options(), new FileSystemResourceReader(workDir, new File('src/test/resources/less')))

    File basicSource = new File(workDir, 'basic.less')
    File basicResult = new File('src/test/resources/less/basic.css')
    File basicDestination = new File(workDir, 'basic.css')
    CompilationUnit basicUnit = new CompilationUnit(basicSource.canonicalPath, basicDestination, new Options(), new FileSystemResourceReader(new File('src/test/resources/less')))

    CompilationTask compilationTask


    def setup() {
        cleanDir(cacheDir)
        cleanDir(workDir)
        initSources()
        compilationTask = new CompilationTask(new RhinoLessCompilationEngine())
        compilationTask.cacheDir = cacheDir
        compilationTask.compilationUnits = [importUnit, basicUnit]
    }

    def 'execute with empty cache and destination'() {
        when:
        def compiledLocation = compilationTask.execute()*.sourceLocation

        then:
        basicDestination.text == basicResult.text
        basicUnit.imports == []
        !basicUnit.isDirty()

        importDestination.text == importResult.text
        importUnit.imports == ['import1/imported1.less', 'import1/import2/imported2.less', 'import1/commonImported.less', 'import1/import2/commonImported.less', 'imported0.less']
        !importUnit.isDirty()

        compilationTask.readFromCache(basicUnit) == basicUnit
        compilationTask.readFromCache(importUnit) == importUnit

        compiledLocation.sort() == [importUnit.sourceLocation, basicUnit.sourceLocation].sort()
    }

    def 'execute with filled cache and destination'() {
        setup:
        //fill cache
        compilationTask.execute()

        //create new objects to test that the cache file based on the hash code does not change
        compilationTask = new CompilationTask(new RhinoLessCompilationEngine())
        compilationTask.cacheDir = cacheDir
        CompilationUnit importUnit = new CompilationUnit(importSource.canonicalPath, importDestination, new Options(), new FileSystemResourceReader(workDir, new File('src/test/resources/less')))
        CompilationUnit basicUnit = new CompilationUnit(basicSource.canonicalPath, basicDestination, new Options(), new FileSystemResourceReader(new File('src/test/resources/less')))
        compilationTask.compilationUnits = [importUnit, basicUnit]
        compilationTask.lessCompiler = Mock(LessCompiler)

        when:
        def compiledUnits = compilationTask.execute()

        then:
        //everything is up to date -> compiler should not be invoked
        0 * compilationTask.lessCompiler._
        compiledUnits == []
    }

    def 'recompile when source changed'() {
        setup:
        compilationTask.execute() //fill cache

        when:
        sleep(1200)
        basicSource << 'a{color: @black;}'
        def compiledLocations = compilationTask.execute()*.sourceLocation

        then:
        basicDestination.text == basicResult.text + 'a {\n  color: #000000;\n}\n'
        basicUnit.imports == []
        !basicUnit.isDirty()
        compiledLocations == [basicUnit.sourceLocation]
    }

    def 'recompile when imported source changed'() {
        setup:
        compilationTask.execute() //fill cache

        when:
        sleep(1200)
        imported0Source << '@import "basic";'
        def compiledLocations = compilationTask.execute()*.sourceLocation

        then:
        importDestination.text == importResult.text + 'p {\n  color: #000000;\n  width: add(1, 1);\n}\n'
        importUnit.imports == ['import1/imported1.less', 'import1/import2/imported2.less', 'import1/commonImported.less', 'import1/import2/commonImported.less', 'imported0.less', 'basic.less']
        !importUnit.isDirty()
        compiledLocations == [importUnit.sourceLocation]
    }

    def 'recompile when imported source changed with commandline engine'() {
        setup:
        compilationTask = new CompilationTask(new CommandLineLesscCompilationEngine(System.getProperty('lesscExecutable', 'lessc')))
        compilationTask.cacheDir = cacheDir
        compilationTask.compilationUnits = [importUnit, basicUnit]
        compilationTask.execute() //fill cache

        when:
        sleep(1200)
        imported0Source << '@import "basic";'
        def compiledLocations = compilationTask.execute()*.sourceLocation

        then:
        importDestination.text == importResult.text + 'p {\n  color: #000000;\n  width: add(1, 1);\n}\n'
        importUnit.imports*.replace('\\', '/').sort() == ['basic.less', 'import1/commonImported.less', 'import1/import2/commonImported.less', 'import1/import2/imported2.less', 'import1/imported1.less', 'imported0.less']
        !importUnit.isDirty()
        compiledLocations == [importUnit.sourceLocation]
    }

    def 'recompile when options changed'() {
        setup:
        compilationTask.execute() //fill cache

        when:
        compilationTask.compilationUnits.each { it.options.dumpLineNumbers = COMMENTS }
        def compiledLocations = compilationTask.execute()*.sourceLocation

        then:
        compiledLocations.sort() == [importUnit.sourceLocation, basicUnit.sourceLocation].sort()
    }

    def 'compile with custom js'() {
        setup:
        basicDestination.delete()

        when:
        compilationTask = new CompilationTask(new RhinoLessCompilationEngine(), new File('src/test/resources/less.js-tests/functions.js'), cacheDir)
        compilationTask.compilationUnits = [basicUnit]
        compilationTask.execute()

        then:
        basicDestination.text == basicResult.text.replace('add(1, 1)', '2')
        basicUnit.imports == []
        !basicUnit.isDirty()
    }

    def 'start daemon'() {
        setup:
        def compiledLocations = []
        compilationTask.compilationListener = [notifySuccessfulCompilation: {
            compiledLocations << it.sourceLocation
        }] as CompilationListener
        compilationTask.startDaemon(100)

        when:
        sleep(1200)

        then:
        importDestination.text == importResult.text
        compiledLocations[0].sort() == [importUnit.sourceLocation, basicUnit.sourceLocation].sort()

        when:
        imported0Source << '@import "basic";'
        sleep(1200)

        then:
        importDestination.text == importResult.text + 'p {\n  color: #000000;\n  width: add(1, 1);\n}\n'
        compiledLocations[1] == [importUnit.sourceLocation]
    }

    def 'run daemon with error in less'() {
        setup:
        def compiledLocations = []
        compilationTask.compilationListener = [notifySuccessfulCompilation: {
            compiledLocations << it.sourceLocation
        }] as CompilationListener
        compilationTask.startDaemon(100)

        when:
        sleep(1200)

        then:
        importDestination.text == importResult.text
        compiledLocations[0].sort() == [importUnit.sourceLocation, basicUnit.sourceLocation].sort()

        when:
        imported0Source << '@import "basic'
        sleep(1200)

        then:
        importDestination.text == importResult.text
        compiledLocations.size() == 1

        when:
        imported0Source << '";'
        sleep(1200)

        then:
        importDestination.text == importResult.text + 'p {\n  color: #000000;\n  width: add(1, 1);\n}\n'
        compiledLocations[1] == [importUnit.sourceLocation]
    }

    def 'fail to start daemon twice'() {
        setup:
        compilationTask.startDaemon(100)

        when:
        compilationTask.startDaemon(100)

        then:
        thrown(RuntimeException)
    }

    def 'stop daemon'() {
        setup:
        compilationTask.startDaemon(100)

        when:
        sleep(100)

        then:
        compilationTask.daemon.alive

        when:
        compilationTask.stopDaemon()
        sleep(1000)

        then:
        compilationTask.daemon == null
    }

    private void cleanDir(File dir) {
        dir.deleteDir()
        dir.mkdirs()
    }

    private void initSources() {
        importSource.text = new File('src/test/resources/less/import.less').text
        imported0Source.text = new File('src/test/resources/less/imported0.less').text
        basicSource.text = new File('src/test/resources/less/basic.less').text
    }
}
