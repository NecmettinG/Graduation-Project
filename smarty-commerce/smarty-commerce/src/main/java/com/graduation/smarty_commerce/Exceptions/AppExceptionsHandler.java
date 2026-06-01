package com.graduation.smarty_commerce.Exceptions;

import com.graduation.smarty_commerce.ui.Model.Response.ErrorMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Date;

@ControllerAdvice
public class AppExceptionsHandler {


    @ExceptionHandler(value = {UserServiceException.class})
    public ResponseEntity<Object> handleUserServiceException(UserServiceException ex, WebRequest request)
    {

        ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());


        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {OrderServiceException.class})
    public ResponseEntity<Object> handleOrderServiceException(OrderServiceException ex, WebRequest request)
    {
        ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {CartServiceException.class})
    public ResponseEntity<Object> handleCartServiceException(CartServiceException ex, WebRequest request)
    {
        ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*
    OPTIMISTIC LOCKING:
    Instead of throwing a scary unformatted 500 Server Error to your front end, it instantly responds with a safe, manageable 409 Conflict-
    error reading: "The resource was modified by another transaction. Please refresh and try again."
    */
    @ExceptionHandler(value = {ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<Object> handleOptimisticLockingFailureException(ObjectOptimisticLockingFailureException ex, WebRequest request)
    {
        ErrorMessage errorMessage = new ErrorMessage(new Date(), "The resource was modified by another transaction. Please refresh and try again.");
        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleOtherExceptions(Exception ex, WebRequest request)
    {
        ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());

        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {NoResourceFoundException.class})
    public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request)
    {
        ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());

        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

}
