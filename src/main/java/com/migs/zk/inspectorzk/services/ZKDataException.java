package com.migs.zk.inspectorzk.services;

/**
 * Created by: mig.c on 1/10/18.
 */
public class ZKDataException extends Exception {

    public ZKDataException(String message) {
        super(message);
    }

    public ZKDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
