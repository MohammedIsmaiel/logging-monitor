package com.basatatech.loggingmonitor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, Model model) {
        return createErrorModelAndView("Resource Not Found", ex.getMessage(), model);
    }

    @ExceptionHandler(NoAvailableLogsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleNoAvailableLogsException(NoAvailableLogsException ex, Model model) {
        return createErrorModelAndView("No Available Logs", ex.getMessage(), model);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        return createErrorModelAndView("Internal Server Error", ex.getMessage(), model);
    }

    private String createErrorModelAndView(String errorTitle, String errorMessage, Model model) {
        model.addAttribute(errorTitle + "errorMessage", errorMessage);
        return "error";
    }
}
