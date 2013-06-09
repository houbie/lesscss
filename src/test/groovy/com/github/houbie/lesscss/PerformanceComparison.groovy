package com.github.houbie.lesscss

import ch.qos.logback.classic.Level
import org.slf4j.LoggerFactory

class PerformanceComparison {
    static File source = new File('src/test/resources/less/bootstrap/bootstrap.less')
    static File destination = new File('build/tmp/bootstrap.css')

    static void main(String[] args) {
        long start = System.currentTimeMillis()

        LessCompiler compiler = new LessCompiler()
        LoggerFactory.getLogger(LessCompiler).level = Level.WARN

        then:
        (1..5).each {
            if (it > 1) {
                start = System.currentTimeMillis()
            }
            compiler.compile(source, destination,
                    new Options(optimizationLevel: 3, relativeUrls: false), new FileSystemResourceReader(source.getParentFile()), null)
            long time = System.currentTimeMillis() - start
            println("RHINO\t$it\t$time")
        }
    }
}
