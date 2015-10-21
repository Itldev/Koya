/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.model.impl;

import java.util.Objects;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.web.client.RestTemplate;

import fr.itldev.koya.model.KoyaNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class User extends KoyaNode {

	@JsonProperty("userName")
	private String userName;
	@JsonProperty("firstName")
	private String firstName;
	@JsonProperty("lastName")
	private String name;
	@JsonProperty("enabled")
	private Boolean enabled;
	@JsonProperty("email")
	private String email;
	@JsonProperty("emailFeedDisabled")
	private Boolean emailFeedDisabled;
	@JsonIgnore
	private String password;
	@JsonProperty("civilTitle")
	private String civilTitle;

	private String ticketAlfresco;
	@JsonIgnore
	private Preferences preferences;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		if (firstName == null) {
			return "";
		}
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public String getName() {
		if (name == null) {
			return "";
		}
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public Boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getEmail() {
		if (email == null) {
			return "";
		}
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getEmailFeedDisabled() {
		if (emailFeedDisabled == null) {
			emailFeedDisabled = false;
		}
		/*
		 * TODO check real value at user building
		 */
		return emailFeedDisabled;
	}

	public void setEmailFeedDisabled(Boolean emailFeedDisabled) {
		this.emailFeedDisabled = emailFeedDisabled;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCivilTitle() {
		return civilTitle;
	}

	public void setCivilTitle(String civilTitle) {
		this.civilTitle = civilTitle;
	}

	public String getTicketAlfresco() {
		return ticketAlfresco;
	}

	public void setTicketAlfresco(String ticketAlfresco) {
		this.ticketAlfresco = ticketAlfresco;
	}

	public Preferences getPreferences() {
		return preferences;
	}

	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	@Override
	public String toString() {
		return userName + "(" + email + ")";
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + Objects.hashCode(this.userName);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final User other = (User) obj;
		if (!Objects.equals(this.userName, other.userName)) {
			return false;
		}
		return true;
	}

}
