package com.thedan17.salesnet.util;

import com.thedan17.salesnet.exception.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;

/** Перехватчик исключений, предполагаются в основном те, что созданы для проекта. */
@ControllerAdvice
public class GlobalExceptionHandler {
  @Autowired AppLogEnricher appLogEnricher;
  @Autowired AppLoggerCore appLoggerCore;

  /** Конструктор. */
  public GlobalExceptionHandler(AppLogEnricher appLogEnricher, AppLoggerCore appLoggerCore) {
    this.appLogEnricher = appLogEnricher;
    this.appLoggerCore = appLoggerCore;
  }

  /** Логирование перехваченной ошибки и формирование ответа на запрос. */
  private ResponseEntity<ProblemDetail> handleExceptionDefault(
        Exception exception, HandlerMethod handlerMethod, HttpStatus httpStatus) {
    this.appLogEnricher.onExceptionHandled(Pair.of(exception, handlerMethod));
    this.appLoggerCore.error("Exception occurred: " + exception.getMessage());
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, exception.getMessage());
    problemDetail.setProperty("timestamp", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    return ResponseEntity.of(problemDetail).build();
  }

  /** Перехват исключений плохого запроса по вине запроса. */
  @ExceptionHandler({InvalidSearchParameterException.class,
                     InvalidRequestBodyException.class,
                     SuchElementExistException.class})
  public ResponseEntity<ProblemDetail> handleInvalidRequestData(
          Exception e, HandlerMethod handlerMethod) {
    return handleExceptionDefault(e, handlerMethod, HttpStatus.BAD_REQUEST);
  }

  /** Перехват исключения ненайденных данных по вине запроса. */
  @ExceptionHandler({LogNotExistException.class,
                     ContentNotFoundException.class})
  public ResponseEntity<ProblemDetail> handleInvalidContentId(
          Exception e, HandlerMethod handlerMethod) {
    return handleExceptionDefault(e, handlerMethod, HttpStatus.NOT_FOUND);
  }

  /** Перехват исключения игнорирования запроса. */
  @ExceptionHandler(RequestIgnoreNeededException.class)
  public ResponseEntity<ProblemDetail> handleIgnoreException(
          Exception e, HandlerMethod handlerMethod) {
    return handleExceptionDefault(e, handlerMethod, HttpStatus.ACCEPTED);
  }

  /** Удержание всех остальных исключений. Предназначено для production. */
  //@ExceptionHandler(Exception.class)
  public void handleUnhandledException(Exception e, HandlerMethod handlerMethod) {
    this.appLogEnricher.logException(handlerMethod, e);
  }
}
