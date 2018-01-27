package com.migs.zk.inspectorzk.services;

/**
 * Created by: mig.c on 1/10/18.
 */
public class ZKAuthException extends Exception {

    public ZKAuthException(String message) {
        super(message);
    }

    public ZKAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
