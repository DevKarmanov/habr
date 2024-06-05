package karm.van.habr.exceptions;

public class UserBlockedException extends Exception{
    public UserBlockedException(String message){
        super(message);
    }

    public UserBlockedException(){
        super();
    }
}
