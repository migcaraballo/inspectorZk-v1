package com.migs.zk.inspectorzk.services;

/**
 * Created by migc on 1/9/18.
 */
public class ZKConnectionException extends Exception {

	public ZKConnectionException(Throwable cause) {
		super("Problem Encountered while connecting to ZooKeeper.", cause);
	}

	public ZKConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
