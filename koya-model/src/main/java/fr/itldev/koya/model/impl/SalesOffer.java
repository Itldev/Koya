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

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import fr.itldev.koya.model.json.util.NodeRefDeserializer;

/**
 * Template attribute exists to determinate Offer available for Sale and Offers
 * already attributed to a company.
 * 
 */
public final class SalesOffer {

	private String template = "";
	private Boolean multiSpaces = Boolean.FALSE;
	private Boolean active = Boolean.TRUE;
	private Long quotaMb = Long.valueOf(0);
	private Integer limitDossiers = Integer.valueOf(0);
	private Integer limitSpaces = Integer.valueOf(0);
	private String expiration = "";
	
	
	protected NodeRef nodeRef;

	protected String name;
	protected String title;

	/*
	 * ======== Attributes Getters/Setters
	 */

	@JsonDeserialize(using = NodeRefDeserializer.class)
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		if (title == null || title.isEmpty()) {
			return name;
		}
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public SalesOffer() {
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public Boolean getMultiSpaces() {
		return multiSpaces;
	}

	public void setMultiSpaces(Boolean multiSpaces) {
		this.multiSpaces = multiSpaces;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getQuotaMb() {
		return quotaMb;
	}

	public void setQuotaMb(Long quotaMb) {
		this.quotaMb = quotaMb;
	}

	public Integer getLimitDossiers() {
		return limitDossiers;
	}

	public void setLimitDossiers(Integer limitDossiers) {
		this.limitDossiers = limitDossiers;
	}

	public Integer getLimitSpaces() {
		return limitSpaces;
	}

	public void setLimitSpaces(Integer limitSpaces) {
		this.limitSpaces = limitSpaces;
	}

	public String getExpiration() {
		return expiration;
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}	
}
