package com.github.houbie.lesscss

import spock.lang.Specification
import spock.lang.Unroll

abstract class AbstractLessCompilerTest extends Specification {
    static File LESS_JS_CSS_DIR = new File('src/test/resources/less.js-tests/css')

    LessCompiler compiler

    def "compile empty less"() {
        expect:
        compiler.compile('') == ''
    }

    def "compile css"() {
        def css = 'div {\n  color: black;\n}\n'

        expect:
        compiler.compile(css) == css
    }

    def "compile file with imports"() {
        def file = new File('src/test/resources/less/import.less')
        def result = compiler.compileWithDetails(file.text, new FileSystemResourceReader(file.parentFile))

        expect:
        result.result == new File('src/test/resources/less/import.css').text
        result.imports == ['import1/imported1.less', 'import1/import2/imported2.less', 'import1/commonImported.less', 'import1/import2/commonImported.less', 'imported0.less']
    }

    def "compile file with errors"() {
        when:
        compiler.compile(new File('src/test/resources/less/broken.less'))

        then:
        def e = thrown(Exception)
        e.message == 'less parse exception: type:Parse,message:missing closing `}`,filename:null,index:13,line:1,callLine:undefined,callExtract:undefined,stack:undefined,column:13,extract:,#broken less {,,'
    }

    def "compile file with missing imports"() {
        when:
        compiler.compile(new File('src/test/resources/less/brokenImport.less'))

        then:
        def e = thrown(Exception)
        e.message == 'less parse exception: type:Syntax,message:less compiler error: import "doesNotExist.less" could not be resolved,filename:null,index:undefined,line:null,callLine:undefined,callExtract:undefined,stack:undefined,column:-1,extract:,,  color: red;,'
    }

    def "compile twitter bootstrap"() {
        def result = compiler.compile(new File('src/test/resources/less/bootstrap/bootstrap.less'))

        expect:
        result == new File('src/test/resources/less/bootstrap/bootstrap.css').text
    }

    @Unroll
    def "less.js compatibility tests for #lessFile"() {
        expect:
        compiler.compile(lessFile,) == getCss(lessFile).text

        where:
        lessFile << new File('src/test/resources/less.js-tests/less').listFiles().findAll { it.name.endsWith('.less') }
    }

    def getCss(File lessFile) {
        return new File(LESS_JS_CSS_DIR, lessFile.getName().replace('.less', '.css'))
    }
}
