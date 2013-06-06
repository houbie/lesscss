package com.github.houbie.lesscss

import spock.lang.Specification
import spock.lang.Unroll

import javax.script.ScriptEngine

import static com.github.houbie.lesscss.Options.LineNumbersOutput.*

abstract class AbstractLessCompilerTest extends Specification {
    static File LESS_JS_CSS_DIR = new File('src/test/resources/less.js-tests/css')

    LessCompiler compiler

    LessCompiler createCompiler(String customJavaScript = null) {
        def reader = (customJavaScript) ? new StringReader(customJavaScript) : null
        return new LessCompiler(reader, getScriptEngine())
    }

    abstract ScriptEngine getScriptEngine();

    def "compile empty less"() {
        expect:
        createCompiler().compile('') == ''
    }

    def "compile css"() {
        def css = 'div {\n  color: black;\n}\n'

        expect:
        createCompiler().compile(css) == css
    }

    def "compile file with imports"() {
        def file = new File('src/test/resources/less/import.less')
        def result = createCompiler().compileWithDetails(file.text, new FileSystemResourceReader(file.parentFile), new Options(), file.name)

        expect:
        result.result == new File('src/test/resources/less/import.css').text
        result.imports == ['import1/imported1.less', 'import1/import2/imported2.less', 'import1/commonImported.less', 'import1/import2/commonImported.less', 'imported0.less']
    }

    def "compile file with errors"() {
        when:
        createCompiler().compile(new File('src/test/resources/less/broken.less'))

        then:
        def e = thrown(Exception)
        e.message == 'less parse exception: type:Parse,message:missing closing `}`,filename:broken.less,index:13,line:1,callLine:undefined,callExtract:undefined,stack:undefined,column:13,extract:,#broken less {,,'
    }

    def "compile file with missing imports"() {
        when:
        createCompiler().compile(new File('src/test/resources/less/brokenImport.less'))

        then:
        def e = thrown(Exception)
        e.message == 'less parse exception: type:Syntax,message:less compiler error: import "doesNotExist.less" could not be resolved,filename:brokenImport.less,index:undefined,line:null,callLine:undefined,callExtract:undefined,stack:undefined,column:-1,extract:,,  color: red;,'
    }

    def "compile twitter bootstrap"() {
        def result = createCompiler().compile(new File('src/test/resources/less/bootstrap/bootstrap.less'))

        expect:
        result == new File('src/test/resources/less/bootstrap/bootstrap.css').text
    }

    @Unroll
    def "less.js compatibility tests for #lessFile"() {
        LessCompiler compiler = createCompiler(new File('src/test/resources/less.js-tests/functions.js').text)
        expect:
        compiler.compile(lessFile, new Options(strictImports: true)) == getCss(lessFile).text

        where:
        lessFile << new File('src/test/resources/less.js-tests/less').listFiles().findAll { it.name.endsWith('.less') }
    }

    @Unroll
    def "less.js debug compatibility test #dumpLineNumbers"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/debug/linenumbers.less')
        when:
        compiler = createCompiler()

        then:
        compiler.compile(lessFile, new Options(dumpLineNumbers: dumpLineNumbers)) == getCss(lessFile, 'debug/', '-' + dumpLineNumbers.optionString).text
                .replace('{pathimport}', 'import/').replace('{path}', '')
                .replace('{pathimportesc}', 'import\\/').replace('{pathesc}', '')

        where:
        dumpLineNumbers << [COMMENTS, MEDIA_QUERY, ALL]
    }

    def "less.js static urls compatibility test"() {
        def lessFile = new File('src/test/resources/less.js-tests/less/static-urls/urls.less')
        def compiler = createCompiler()

        expect:
        compiler.compile(lessFile, new Options(relativeUrls: false, rootPath: 'folder (1)/')) == getCss(lessFile, 'static-urls/').text
    }

    def getCss(File lessFile, prefix = '', suffix = '') {
        return new File(LESS_JS_CSS_DIR, prefix + lessFile.getName().replace('.less', suffix + '.css'))
    }
}
