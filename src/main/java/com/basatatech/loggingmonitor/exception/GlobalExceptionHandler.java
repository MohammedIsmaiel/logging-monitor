package com.basatatech.loggingmonitor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleResourceNotFoundException(ResourceNotFoundException ex) {
        return createErrorModelAndView("Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(NoAvailableLogsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleNoAvailableLogsException(NoAvailableLogsException ex) {
        return createErrorModelAndView("No Available Logs", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(Exception ex) {
        return createErrorModelAndView("Internal Server Error", ex.getMessage());
    }

    private ModelAndView createErrorModelAndView(String errorTitle, String errorMessage) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("errorTitle", errorTitle);
        modelAndView.addObject("errorMessage", errorMessage);
        return modelAndView;
    }
}
