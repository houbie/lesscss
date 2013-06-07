package com.github.houbie.lesscss

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class PerformanceComparison {
    static File source = new File('src/test/resources/less/bootstrap/bootstrap.less')
    static File destination = new File('build/tmp/bootstrap.css')

    static LessCompiler createCompiler(ScriptEngine scriptEngine) {
        return new LessCompiler(null, scriptEngine)
    }

    static void main(String[] args) {
        long start = System.currentTimeMillis()

        def compiler = createCompiler(new ScriptEngineManager().getEngineByName(LessCompiler.NASHORN))

        (1..2).each {//3th run fails
            if (it > 1) {
                start = System.currentTimeMillis()
            }
            compiler.compile(source, destination,
                    new Options(optimizationLevel: 3, relativeUrls: false), new FileSystemResourceReader(source.getParentFile()), null)
            long time = System.currentTimeMillis() - start
            println("NASHORN\t$it\t$time")
        }


        start = System.currentTimeMillis()

        compiler = createCompiler(new ScriptEngineManager().getEngineByName(LessCompiler.RHINO))

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
