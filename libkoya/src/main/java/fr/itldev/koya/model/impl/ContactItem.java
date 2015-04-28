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

package fr.itldev.koya.model.impl;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.itldev.koya.model.KoyaNode;

/**
 * Basic contact item : can be a telephone number, fax number or mail address
 * 
 */
public class ContactItem extends KoyaNode {

	public static final Integer TYPE_TEL = 1;
	public static final Integer TYPE_FAX = 2;
	public static final Integer TYPE_MAIL = 3;
	public static final Integer TYPE_MOBILE = 4;

	private Integer type;
	private String value;

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ContactItem() {
		super();
	}

	public ContactItem(NodeRef n) {
		super(n);
	}

	private ContactItem(Integer type, String value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContactItem other = (ContactItem) obj;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		return true;
	}

	public static ContactItem newInstance(Integer type, String value) {
		return new ContactItem(type, value);
	}	
}
