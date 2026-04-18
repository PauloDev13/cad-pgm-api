package br.gov.rn.natal.cadpgmapi.exception;

public class BusinessException extends RuntimeException{
    public BusinessException(String message) {
        super(message);
    }
}