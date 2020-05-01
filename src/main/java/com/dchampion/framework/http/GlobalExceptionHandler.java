package com.dchampion.framework.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * A global exception handler for all REST methods of {@code RestController}-annotated
 * classes in the application context.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger =
        LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String errMsg =
        "Something went wrong; please contact site administrator";

    /**
     * Catches and handles any exception not explicitly caught/handled in the super
     * class's {@link #handleException(Exception, WebRequest)} method.
     *
     * @param exception the target exception
     *
     * @return the {@link ResponseEntity} for consumption by the client.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handle(Exception exception) {
        // Put something useful in the log...
        logger.warn(exception.getMessage(), exception);

        // but don't reveal too much to the client.
        return handleExceptionInternal(exception, null,
            new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    /**
     * This override completely replaces the super class's version, and adds a generic error
     * message to the body of the response.
     *
     * @param exception the exception
	 * @param body the body for the response
	 * @param headers the headers for the response
	 * @param status the response status
	 * @param request the current request
     *
     * @return a {@link ResponseEntity} whose body contains a generic error message for the
     * client.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (headers == null) {
            headers = new HttpHeaders();
        }
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        return ResponseEntity.status(status).headers(headers).body(errMsg);
    }
}
