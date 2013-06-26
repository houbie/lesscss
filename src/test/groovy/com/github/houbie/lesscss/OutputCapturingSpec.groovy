package com.github.houbie.lesscss

import spock.lang.Specification

abstract class OutputCapturingSpec extends Specification {
    PrintStream originalSystemOut
    ByteArrayOutputStream sysOutCapture = new ByteArrayOutputStream()
    PrintStream originalSystemErr
    ByteArrayOutputStream sysErrCapture = new ByteArrayOutputStream()

    def setup() {
        originalSystemOut = System.out
        System.out = new PrintStream(sysOutCapture)
        originalSystemErr = System.err
        System.err = new PrintStream(sysErrCapture)
    }

    def cleanup() {
        System.out = originalSystemOut
        System.err = originalSystemErr
    }
}
