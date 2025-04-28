package com.thedan17.salesnet.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AppLoggerCore {
  Logger basicLogger = LoggerFactory.getLogger(AppLoggerCore.class.toString() + "Basic");
  ch.qos.logback.classic.Logger logbackLogger;

  public AppLoggerCore() {
    try {
      initialize();
    } catch (Exception e) {
      basicLogger.error(
          "Failed to initialize {}, occurred exception '{}':{}",
          AppLoggerCore.class,
          e.getClass(),
          e.getMessage());
    }
  }

  private void initialize() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    logbackLogger = loggerContext.getLogger(AppLoggerCore.class);
    logbackLogger.setAdditive(false);

    String logDir = "logs";
    Path logDirPath = Paths.get(logDir);
    if (!Files.exists(logDirPath)) {
      try {
        Files.createDirectories(logDirPath);
      } catch (IOException e) {
        basicLogger.error("AppLoggerCore couldn't be initialized, exception '{}'", e.toString());
        throw new UncheckedIOException("Fatal: Logging app component initialization failed because path not reachable", e);
      }
    }
    RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
    rollingFileAppender.setContext(loggerContext);
    rollingFileAppender.setImmediateFlush(true);

    // Настраиваем политику ротации по дате
    TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
    rollingPolicy.setFileNamePattern(logDir + "/log-%d{yyyy-MM-dd}.log");
    rollingPolicy.setParent(rollingFileAppender);
    rollingPolicy.setContext(loggerContext);
    rollingPolicy.setMaxHistory(90);
    rollingPolicy.start();

    rollingFileAppender.setRollingPolicy(rollingPolicy);

    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
    consoleAppender.setContext(loggerContext);

    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(loggerContext);
    encoder.setPattern("APP [%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] %-5level %logger{36} - %msg%n");
    encoder.start();
    rollingFileAppender.setEncoder(encoder);
    rollingFileAppender.start();
    rollingPolicy.start();
    consoleAppender.setEncoder(encoder);
    consoleAppender.start();

    logbackLogger.addAppender(rollingFileAppender);
    logbackLogger.addAppender(consoleAppender);
    logbackLogger.setLevel(Level.TRACE);
    basicLogger.debug("{} initialized.", AppLoggerCore.class);
  }

  public void log(Level level, String message, Object... args) {
    switch (level.levelInt) {
      case Level.TRACE_INT -> logbackLogger.trace(message, args);
      case Level.DEBUG_INT -> logbackLogger.debug(message, args);
      case Level.INFO_INT -> logbackLogger.info(message, args);
      case Level.WARN_INT -> logbackLogger.warn(message, args);
      case Level.ERROR_INT -> logbackLogger.error(message, args);
      default -> logbackLogger.info(message, args); // fallback
    }
  }

  public void trace(String format, Object... args) {
    logbackLogger.trace(format, args);
  }

  public void debug(String format, Object... args) {
    logbackLogger.debug(format, args);
  }

  public void info(String format, Object... args) {
    logbackLogger.info(format, args);
  }

  public void warn(String format, Object... args) {
    logbackLogger.warn(format, args);
  }

  public void error(String format, Object... args) {
    logbackLogger.error(format, args);
  }
}
