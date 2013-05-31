package com.github.houbie.lesscss.utils

import spock.lang.Specification

class IOUtilsSpec extends Specification {

    def "read a file without encoding"() {
        expect:
        IOUtils.read(new File('src/test/resources/ioutils/plain.txt')) == 'plain text'
    }

    def "read a file with encoding"() {
        expect:
        IOUtils.read(new File('src/test/resources/ioutils/utf16.txt'), 'utf16') == 'utf16 text'
    }

    def "read a url without encoding"() {
        expect:
        IOUtils.read(IOUtils.classLoader.getResource('ioutils/plain.txt')) == 'plain text'
    }

    def "read a url with encoding"() {
        expect:
        IOUtils.read(IOUtils.classLoader.getResource('ioutils/utf16.txt'), 'utf16') == 'utf16 text'
    }

}
