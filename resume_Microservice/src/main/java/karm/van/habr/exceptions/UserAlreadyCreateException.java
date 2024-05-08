package karm.van.habr.exceptions;

public class UserAlreadyCreateException extends Exception{

    public UserAlreadyCreateException(){}

    public UserAlreadyCreateException(String message){
        super(message);
    }
}
