package com.agolovenko.jspring.OSM.DB;

public class NodeServiceException extends Exception {
    public NodeServiceException() {
    }

    public NodeServiceException(String message) {
        super(message);
    }

    public NodeServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeServiceException(Throwable cause) {
        super(cause);
    }

    public NodeServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
