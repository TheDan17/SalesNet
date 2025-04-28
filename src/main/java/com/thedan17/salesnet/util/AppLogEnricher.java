package com.thedan17.salesnet.util;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Level;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/** Аспект для логирования всех методов и исключений пакета проекта. */
@Aspect
@Component
public class AppLogEnricher {
  private static final Map<String, String> packageShortage = new HashMap<>();
  @Autowired @Lazy
  AppLoggerCore logger;

  // private static final Logger logger = LoggerFactory.getLogger(AppLogEnricher.class);

  /** Конструктор, задающий вручную сокращения для пакетов. */
  public AppLogEnricher(AppLoggerCore logger) {
    this.logger = logger;
    packageShortage.put("com.thedan17.salesnet", "SALESNET");
    packageShortage.put("org.springframework", "SPRINGFW");
  }

  /**
   * Создание сокращённой структуры.
   *
   * <p>Заключается в создании строки, указывающей полный путь метода, но с заменой некоторых
   * пакетов на сокращения, указанные в {@link AppLogEnricher#packageShortage}
   */
  private String makeShortSignature(Signature signature) {
    MethodSignature methodSignature = (MethodSignature) signature;
    String className = methodSignature.getDeclaringTypeName();
    String methodName = methodSignature.getName();
    return makeShortPackages(className, methodName);
  }

  private String makeShortPackages(String className, String methodName) {
    for (var pair : packageShortage.entrySet()) {
      className = className.replace(pair.getKey(), pair.getValue());
    }
    return className + '.' + methodName;
  }

  /** Создаёт свою собственное строковое представление target. */
  private String makeShortTarget(Object target) {
    if (target.getClass().toString().contains("com.thedan17.salesnet")) {
      String className = target.getClass().getSimpleName();
      int identityHashCode = System.identityHashCode(target);
      return className + "@" + Integer.toHexString(identityHashCode);
    }
    return target.toString();
  }

  /** Перехват выполнения всех методов проекта, для логирования. */
  @Around("execution(* com.thedan17.salesnet..*.*(..))")
  public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    if (joinPoint.getSignature().getDeclaringType().equals(AppLoggerCore.class)) {
      return joinPoint.proceed(); // не логируем AppLoggerCore
    }

    long startTimePoint = System.currentTimeMillis();
    logger.trace(
        "start of method \"{}\"  [args:{}, target:{}]",
        makeShortSignature(joinPoint.getSignature()),
        joinPoint.getArgs(),
        makeShortTarget(joinPoint.getTarget()));
    Object result = joinPoint.proceed();

    long endTimePoint = System.currentTimeMillis();
    logger.trace(
        "end of method \"{}\"  [took:{}ms, target:{}]",
        makeShortSignature(joinPoint.getSignature()),
        endTimePoint - startTimePoint,
        makeShortTarget(joinPoint.getTarget()));

    return result;
  }

  /** Получение исключения, возникшего в проекте, для логирования. */
  public void logException(HandlerMethod handlerMethod, Throwable ex) {
    logger.error(
        "error in {} method: {}  [error_type:{}, target:{}]",
        makeShortPackages(
            handlerMethod.getBeanType().getName(), handlerMethod.getMethod().getName()),
        ex.getMessage(),
        ex.getClass().getSimpleName(),
        makeShortTarget(handlerMethod.getBean()));
  }

  /**
   * Метод логирования обработанных исключений, которые сообщаются через метод {@link
   * org.springframework.context.ApplicationEventPublisher#publishEvent(Object)}.
   */
  public void onExceptionHandled(Pair<Exception, HandlerMethod> event) {
    logger.warn(
        "error in {} method handled [target:{}]",
        makeShortPackages(
            event.getSecond().getBeanType().getName(), event.getSecond().getMethod().getName()),
        makeShortTarget(event.getSecond().getBean()));
  }

  public void logEnriched(Level level, String msg) {
    logger.log(level, "message from '{}' : {}", "", msg);
  }
}
