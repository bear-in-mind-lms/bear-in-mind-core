package com.kwezal.bearinmind.core.config;

import static com.kwezal.bearinmind.core.exceptions.ErrorCode.REQUEST_ARGUMENT_INVALID;
import static com.kwezal.bearinmind.core.utils.LoggingUtils.*;
import static java.util.Objects.nonNull;

import com.kwezal.bearinmind.core.exceptions.*;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    // NOT FOUND

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException e) {
        log.info(resourceNotFound(e.getClassName(), e.getProperties()));

        return new ErrorResponse(e.getErrorCode(), e.getErrorArguments());
    }

    // BAD REQUEST

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpClientErrorException.class)
    public ErrorResponse handleHttpClientErrorException(HttpClientErrorException e) {
        log.error(GENERIC_EXCEPTION_MESSAGE, e.getClass().getSimpleName(), e.getMessage());

        return new ErrorResponse(ErrorCode.CONNECTION_ERROR, null);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidRequestDataException.class)
    public ErrorResponse handleInvalidRequestDataException(InvalidRequestDataException e) {
        log.info(invalidRequestDataException(e.getClassName(), e.getProperties()));

        return new ErrorResponse(e.getErrorCode(), e.getErrorArguments());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        { HttpMessageNotReadableException.class, IllegalArgumentException.class, ConstraintViolationException.class }
    )
    public void handleIllegalArgumentException(Exception e) {
        log.error(GENERIC_EXCEPTION_MESSAGE, e.getClass().getSimpleName(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        final var parameter = e.getParameter();
        final var parameterName = nonNull(parameter.getParameterName()) ? parameter.getParameterName() : "";
        final var method = parameter.getMethod();
        final var methodName = nonNull(method) ? method.getName() : "";

        final var fieldsErrorMessages = FieldsAndErrorMessages.fromBindException(e);
        final var fields = fieldsErrorMessages.fields;
        final var errorMessages = fieldsErrorMessages.errorMessages;

        log.info(methodArgumentNotValidException(), methodName, parameterName, fields, errorMessages);

        return new ErrorResponse(REQUEST_ARGUMENT_INVALID, fields);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ErrorResponse handleBindException(BindException e) {
        final var fieldsErrorMessages = FieldsAndErrorMessages.fromBindException(e);
        final var fields = fieldsErrorMessages.fields;
        final var errorMessages = fieldsErrorMessages.errorMessages;

        log.info(bindException(), e.getObjectName(), fields, errorMessages);

        return new ErrorResponse(REQUEST_ARGUMENT_INVALID, fields);
    }

    private record FieldsAndErrorMessages(List<String> fields, List<String> errorMessages) {
        private static FieldsAndErrorMessages fromBindException(BindException e) {
            final var fields = new ArrayList<String>();
            final var errorMessages = new ArrayList<String>();

            for (final var error : e.getAllErrors()) {
                errorMessages.add(error.getDefaultMessage());
                if (error instanceof FieldError fieldError) {
                    fields.add(fieldError.getField());
                }
            }

            return new FieldsAndErrorMessages(fields, errorMessages);
        }
    }

    // UNAUTHORIZED

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthorizationException.class)
    public ErrorResponse handleAuthorizationException(AuthorizationException e) {
        log.info(authorizationException(e.getClassName(), e.getProperties()));

        return new ErrorResponse(e.getErrorCode(), e.getErrorArguments());
    }

    // FORBIDDEN

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public ErrorResponse handleForbiddenException(ForbiddenException e) {
        log.info(authorizationException(e.getClassName(), e.getProperties()));

        return new ErrorResponse(e.getErrorCode(), e.getErrorArguments());
    }

    // INTERNAL SERVER ERROR

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ AccessDeniedException.class, SecurityException.class })
    public void handleSecurityExceptions(Exception e) {
        log.error(GENERIC_EXCEPTION_MESSAGE, e.getClass().getSimpleName(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(
        {
            EntityNotFoundException.class,
            IndexOutOfBoundsException.class,
            NoSuchElementException.class,
            NonUniqueResultException.class,
            NullPointerException.class,
            RollbackException.class,
        }
    )
    public void handleInternalServerErrors(Exception e) {
        log.error(GENERIC_EXCEPTION_MESSAGE, e.getClass().getSimpleName(), e.getMessage());
    }
}
