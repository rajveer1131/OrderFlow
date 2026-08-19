package com.example.OrderFlow.Common.Exception;

public class InvalidOrderStateException extends RuntimeException{
    public InvalidOrderStateException(String message){
        super(message);
    }
}
