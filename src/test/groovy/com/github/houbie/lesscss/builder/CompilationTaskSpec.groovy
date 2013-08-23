package com.github.houbie.lesscss.builder

import com.github.houbie.lesscss.LessCompiler
import com.github.houbie.lesscss.Options
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import spock.lang.Specification

class CompilationTaskSpec extends Specification {
    File workDir = new File('build/tmp/compilationTask')
    File cacheDir = new File("build/tmp/testCacheDir")

    File importSource = new File(workDir, 'import.less')
    File imported0Source = new File(workDir, 'imported0.less')
    File importResult = new File('src/test/resources/less/import.css')
    File importDestination = new File(workDir, 'import.css')
    CompilationUnit importUnit = new CompilationUnit(importSource, importDestination, new Options(), new FileSystemResourceReader(workDir, new File('src/test/resources/less')))

    File basicSource = new File(workDir, 'basic.less')
    File basicResult = new File('src/test/resources/less/basic.css')
    File basicDestination = new File(workDir, 'basic.css')
    CompilationUnit basicUnit = new CompilationUnit(basicSource, basicDestination, new Options(), new FileSystemResourceReader(new File('src/test/resources/less')))

    CompilationTask compilationTask = new CompilationTask(cacheDir: cacheDir, compilationUnits: [importUnit, basicUnit])


    def setup() {
        cleanDir(cacheDir)
        cleanDir(workDir)
        initSources()
    }

    def 'execute with empty cache and destination'() {
        when:
        compilationTask.execute()

        then:
        basicDestination.text == basicResult.text
        basicUnit.imports == []
        !basicUnit.isDirty()

        importDestination.text == importResult.text
        importUnit.imports == ['import1/imported1.less', 'import1/import2/imported2.less', 'import1/commonImported.less', 'import1/import2/commonImported.less', 'imported0.less']
        !importUnit.isDirty()

        compilationTask.readFromCache(basicUnit) == basicUnit
        compilationTask.readFromCache(importUnit) == importUnit
    }

    def 'execute with filled cache and destination'() {
        setup:
        compilationTask.execute() //fill cache

        when:
        compilationTask.lessCompiler = Mock(LessCompiler)
        compilationTask.execute()

        then:
        //everything is up to date -> compiler should not be invoked
        0 * compilationTask.lessCompiler._
    }

    def 'recompile when source changed'() {
        setup:
        compilationTask.execute() //fill cache

        when:
        sleep(1000)
        basicSource << 'a{color: @black;}'
        compilationTask.execute()

        then:
        basicDestination.text == basicResult.text + 'a {\n  color: #000000;\n}\n'
        basicUnit.imports == []
        !basicUnit.isDirty()
    }

    def 'recompile when imported source changed'() {
        setup:
        compilationTask.execute() //fill cache

        when:
        sleep(1000)
        imported0Source << '@import "basic";'
        compilationTask.execute()

        then:
        importDestination.text == importResult.text + 'p {\n  color: #000000;\n  width: add(1, 1);\n}\n'
        importUnit.imports == ['import1/imported1.less', 'import1/import2/imported2.less', 'import1/commonImported.less', 'import1/import2/commonImported.less', 'imported0.less', 'basic.less']
        !importUnit.isDirty()
    }

    def 'compile with custom js'() {
        setup:
        basicDestination.delete()

        when:
        compilationTask = new CompilationTask(new File('src/test/resources/less.js-tests/functions.js'), cacheDir)
        compilationTask.compilationUnits = [basicUnit]
        compilationTask.execute()

        then:
        basicDestination.text == basicResult.text.replace('add(1, 1)', '2')
        basicUnit.imports == []
        !basicUnit.isDirty()
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
