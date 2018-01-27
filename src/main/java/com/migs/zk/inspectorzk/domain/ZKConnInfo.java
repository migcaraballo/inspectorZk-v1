package com.migs.zk.inspectorzk.domain;

/**
 * Created by migc on 1/9/18.
 */
public class ZKConnInfo {

	private String host;
	private int port;
	private String usr;
	private String pw;

	public ZKConnInfo(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/******************************************************************************************************************/

	public String getFullHostString(){
		return String.format("%s:%d", host, port);
	}

	/******************************************************************************************************************/

	public void setUsr(String usr) {
		this.usr = usr;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsr() {
		return usr;
	}

	public String getPw() {
		return pw;
	}
}
