package com.jrock.shop.exception;

public class NotEnoughStockException extends RuntimeException {

    // 아래의 것 모두 오버라이딩
    public NotEnoughStockException() {
        super();
    }

    public NotEnoughStockException(String message) {
        super(message);
    }

    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }
}
