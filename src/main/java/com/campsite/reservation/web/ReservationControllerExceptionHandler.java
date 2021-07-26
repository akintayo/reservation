package com.campsite.reservation.web;

import com.campsite.reservation.exception.AccessDeniedException;
import com.campsite.reservation.exception.ErrorDTO;
import com.campsite.reservation.exception.InvalidFieldException;
import com.campsite.reservation.exception.ObjectNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
@Slf4j
public class ReservationControllerExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(InvalidFieldException.class)
    public ErrorDTO handleInvalidField(InvalidFieldException ex) {
        return new ErrorDTO(ex.getMessage(), ex.getErrorCode());
    }

    /**
     * Error handler fpr JSR-303
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorDTO methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        List<ObjectError> errorList = ex.getBindingResult().getAllErrors();
        String errorBuilder = errorList.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining());
        return new ErrorDTO(errorBuilder, "invalid-request");
    }

    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    @ExceptionHandler(ObjectNotFoundException.class)
    public ErrorDTO handleNotFoundException(ObjectNotFoundException ex) {
        return new ErrorDTO(ex.getMessage(), ex.getErrorCode());
    }

    @ResponseStatus(FORBIDDEN)
    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorDTO handleAccessDeniedException(AccessDeniedException ex) {
        return new ErrorDTO(ex.getMessage(), ex.getErrorCode());
    }
}
