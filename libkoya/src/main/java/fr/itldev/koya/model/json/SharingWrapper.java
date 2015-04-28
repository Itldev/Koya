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
package fr.itldev.koya.model.json;

import java.util.ArrayList;
import java.util.List;

import fr.itldev.koya.model.KoyaNode;

/**
 * 
 * This object wraps a list of KoyaNodes to be shared to a list of users mail
 * adresses.Itl is useful to be json serialized.
 * 
 * 
 */
public class SharingWrapper {

	private List<String> sharedNodeRefs = new ArrayList<>();
	private List<String> sharingUsersMails;
	private Boolean resetSharings;
	private String acceptUrl;
	private String rejectUrl;
	private String serverPath;
	private String directAccessUrl;

	public List<String> getSharedNodeRefs() {
		return sharedNodeRefs;
	}

	public void setSharedNodeRefs(List<String> sharedNodeRefs) {
		this.sharedNodeRefs = sharedNodeRefs;
	}

	public List<String> getSharingUsersMails() {
		return sharingUsersMails;
	}

	public void setSharingUsersMails(List<String> sharingUsersMails) {
		this.sharingUsersMails = sharingUsersMails;
	}

	public Boolean isResetSharings() {
		return resetSharings;
	}

	public void setResetSharings(Boolean resetSharings) {
		this.resetSharings = resetSharings;
	}

	public String getAcceptUrl() {
		return acceptUrl;
	}

	public void setAcceptUrl(String acceptUrl) {
		this.acceptUrl = acceptUrl;
	}

	public String getRejectUrl() {
		return rejectUrl;
	}

	public void setRejectUrl(String rejectUrl) {
		this.rejectUrl = rejectUrl;
	}

	public String getServerPath() {
		return serverPath;
	}

	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}

	public String getDirectAccessUrl() {
		return directAccessUrl;
	}

	public void setDirectAccessUrl(String directAccessUrl) {
		this.directAccessUrl = directAccessUrl;
	}

	public SharingWrapper(List<KoyaNode> sharedItems, List<String> usersMails) {
		this(sharedItems, usersMails, Boolean.FALSE, null, null, null, null);
	}

	public SharingWrapper(List<KoyaNode> sharedItems, List<String> usersMails,
			Boolean resetSharings) {
		this(sharedItems, usersMails, resetSharings, null, null, null, null);
	}

	public SharingWrapper(List<KoyaNode> sharedItems, List<String> usersMails,
			String serverPath, String acceptUrl, String rejectUrl,
			String directAccessUrl) {
		this(sharedItems, usersMails, Boolean.FALSE, serverPath, acceptUrl,
				rejectUrl, directAccessUrl);
	}

	public SharingWrapper(List<KoyaNode> sharedItems, List<String> usersMails,
			Boolean resetSharings, String serverPath, String acceptUrl,
			String rejectUrl, String directAccessUrl) {

		for (KoyaNode s : sharedItems) {
			sharedNodeRefs.add(s.getNodeRef().toString());
		}
		sharingUsersMails = usersMails;

		this.resetSharings = resetSharings;
		this.acceptUrl = acceptUrl;
		this.rejectUrl = rejectUrl;
		this.serverPath = serverPath;
		this.directAccessUrl = directAccessUrl;
	}

	public SharingWrapper() {
	}

}
