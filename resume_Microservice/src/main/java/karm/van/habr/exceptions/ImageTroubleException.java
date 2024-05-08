package karm.van.habr.exceptions;

import karm.van.habr.service.ImageCompressionService;

public class ImageTroubleException extends Exception{

    public ImageTroubleException(String message){
        super(message);
    }

    public ImageTroubleException() {

    }
}
