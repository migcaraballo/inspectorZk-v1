package com.migs.zk.inspectorzk.domain;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by migc on 1/13/18.
 */
public class Session {

	private static final String CI_KEY = "ci";

	private static Session instance;
	private Map<String, Object> attributes;

	private SimpleBooleanProperty connected;

	private SimpleStringProperty authUser;

	private Session() {
		attributes = new HashMap<>();
		connected = new SimpleBooleanProperty(false);
		authUser = new SimpleStringProperty("!authed");
	}

	private static synchronized void instantiate(){
		if(instance == null)
			instance = new Session();
	}

	public static Session getSession(){
		if(instance == null)
			instantiate();

		return instance;
	}

	public Object getAttribute(String key){
		return attributes.get(key);
	}

	public void setAttribute(String key, Object o){
		attributes.put(key, o);
	}

	public void removeAttribute(String key){
		attributes.remove(key);
	}

	public ZKConnInfo getCurrentConnectionInfo(){
		return (ZKConnInfo) attributes.get(CI_KEY);
	}

	public SimpleBooleanProperty connectedProperty() {
		return connected;
	}

	public void setConnected(){
		connected.set(true);
	}

	public void setDisconnected(){
		connected.set(false);
	}

	public String getAuthUser() {
		return authUser.get();
	}

	public SimpleStringProperty authUserProperty() {
		return authUser;
	}

	public void setAuthUser(String authUser) {
		this.authUser.set(authUser);
	}
}
