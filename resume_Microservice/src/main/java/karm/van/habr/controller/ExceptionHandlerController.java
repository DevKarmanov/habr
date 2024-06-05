package karm.van.habr.controller;

import karm.van.habr.exceptions.UserBlockedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException() {
        return "{\"error\":\"Максимальный размер файла 5 мб\"}";
    }

}
