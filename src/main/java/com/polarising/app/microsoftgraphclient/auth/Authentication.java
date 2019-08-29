package com.polarising.app.microsoftgraphclient.auth;

import com.polarising.app.microsoftgraphclient.Constants;

public class Authentication {

	private String grant_type;
	private String userName;
	private String password;
	private String client_id;
	private String client_secret;
	private String scope;

	public Authentication(String username, String password) {
		super();
		this.userName = username;
		this.password = password;
		this.client_id = Constants.CLIENT_ID;
		this.client_secret = Constants.CLIENT_SECRET;
		this.scope = "./default";
		this.grant_type = "password";
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getClient_secret() {
		return client_secret;
	}

	public void setClient_secret(String client_secret) {
		this.client_secret = client_secret;
	}

	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getScope() {
		return scope;
	}

	public String getGrant_type() {
		return grant_type;
	}

}
