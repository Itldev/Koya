/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

package fr.itldev.koya.model.json;

import org.alfresco.service.cmr.invitation.NominatedInvitation;

/**
 * 
 * Wrapper for alfresco user invitation
 * 
 */
public class KoyaInvite {

	private String companyName;
	private String email;
	private String roleName;

	private String inviteId;
	private String ticket;

	private String userName;

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getInviteId() {
		return inviteId;
	}

	public void setInviteId(String inviteId) {
		this.inviteId = inviteId;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public KoyaInvite() {
	}

	public KoyaInvite(NominatedInvitation i) {
		super();

		this.roleName = i.getRoleName();
		this.inviteId = i.getInviteId();
		this.ticket = i.getTicket();
		this.email = i.getInviteeEmail();
		this.companyName = i.getResourceName();
		this.userName = i.getInviteeUserName();
	}

}
