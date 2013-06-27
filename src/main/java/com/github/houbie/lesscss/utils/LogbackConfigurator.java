package com.github.houbie.lesscss.utils;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.github.houbie.lesscss.LessCompiler;
import org.slf4j.LoggerFactory;

public class LogbackConfigurator {

    public static void configure(boolean verbose) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
        ca.setContext(lc);
        ca.setName("less console");
        PatternLayoutEncoder pl = new PatternLayoutEncoder();
        pl.setContext(lc);
        pl.setPattern("%msg%n");
        pl.start();

        ca.setEncoder(pl);
        ca.start();

        //prevent double log output
        lc.getLogger(Logger.ROOT_LOGGER_NAME).detachAndStopAllAppenders();
        Logger compilerLogger = lc.getLogger(LessCompiler.class);
        compilerLogger.detachAndStopAllAppenders();
        compilerLogger.addAppender(ca);
        compilerLogger.setLevel(verbose ? Level.ALL : Level.OFF);
    }
}
