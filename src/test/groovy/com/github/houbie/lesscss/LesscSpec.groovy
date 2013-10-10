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

class LesscSpec extends OutputCapturingSpec {
    static String expectedResult = new File('src/test/resources/less/basic.css').text

    File destination = new File('build/tmp/lessc.css')

    def setup() {
        destination.delete()
    }

    def 'compile to destination'() {
        setup:
        Lessc.main('src/test/resources/less/basic.less build/tmp/lessc.css'.split(' '))

        expect:
        destination.text == expectedResult
        sysOutCapture.toString() == ''
    }

    def 'run daemon'() {
        setup:
        File source = new File('src/test/resources/less/basic.less')
        File destination = new File('build/tmp/lessc.css')
        int i = 0
        Lessc.systemIn = [read: { ->
            sleep(500)
            if (destination.text == expectedResult || ++i > 10) {
                return 'q'
            }
            return 0
        }] as InputStream;

        Lessc.main("--daemon $source $destination".split(' '))

        expect:
        destination.text == expectedResult
        sysOutCapture.toString().startsWith(
                'Compiler daemon running, press q to quit...\n' +
                        "compilation of less CompilationUnit{source=$source.absolutePath, destination=$destination.absolutePath} finished in ")
    }

    def 'run daemon with broken less'() {
        setup:
        String expectedOutput = 'Compiler daemon running, press q to quit...\n' +
                'less parse exception: missing closing `}`\n' +
                'in broken.less at line 1\n' +
                'extract\n' +
                '#broken less {\n'
        int i = 0
        Lessc.systemIn = [read: { ->
            sleep(1000)
            if (sysOutCapture.toString() == expectedOutput || ++i > 2) {
                return 'q'
            }
            return 0
        }] as InputStream;

        Lessc.main('--daemon src/test/resources/less/broken.less build/tmp/lessc.css'.split(' '))

        expect:
        sysOutCapture.toString() == expectedOutput
    }

    def 'compile to destination with --verbose'() {
        setup:
        Lessc.main('--verbose src/test/resources/less/basic.less build/tmp/lessc.css'.split(' '))

        expect:
        destination.text == expectedResult
        sysOutCapture.toString() == 'start less compilation\n' +
                'prepareScriptEngine\n' +
                'Using implementation version: Rhino 1.7 release 4 2012 06 18\n' +
                'finished less compilation\n'
    }

    def 'compile to stdout with --verbose'() {
        setup:
        Lessc.main('--verbose src/test/resources/less/basic.less'.split(' '))

        expect:
        sysOutCapture.toString() == expectedResult + '\n'
    }

    def 'print dependencies to stdout'() {
        setup:
        Lessc.main('--depends src/test/resources/less/import.less'.split(' '))

        expect:
        sysOutCapture.toString() == 'Dependencies for src/test/resources/less/import.less:\n' +
                'import1/imported1.less\n' +
                'import1/import2/imported2.less\n' +
                'import1/commonImported.less\n' +
                'import1/import2/commonImported.less\n' +
                'imported0.less\n'
    }

    def 'compile with errors'() {
        setup:
        Lessc.main('src/test/resources/less/broken.less'.split(' '))

        expect:
        sysOutCapture.toString() == ''
        sysErrCapture.toString() == 'less parse exception: missing closing `}`\n' +
                'in broken.less at line 1\n' +
                'extract\n' +
                '#broken less {\n'
    }
}
