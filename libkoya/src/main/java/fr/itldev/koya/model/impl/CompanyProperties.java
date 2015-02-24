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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 *
 *
 */
public class CompanyProperties {

	private String name;
	private String title;
	private String address;
	private String address2;
	private String zipCode;
	private String city;
	private String mailHeaderText;


	private String logoNodeRef;
	private String description;
	private String legalInformations;
	private List<Contact> contacts = new ArrayList<>();
	private List<ContactItem> contactItems = new ArrayList<>();

	private GeoPos geoPos;

	// <editor-fold defaultstate="collapsed" desc="Getters/Setters">

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}		

	public String getMailHeaderText() {
		return mailHeaderText;
	}

	public void setMailHeaderText(String mailHeaderText) {
		this.mailHeaderText = mailHeaderText;
	}

	public String getLogoNodeRef() {
		return logoNodeRef;
	}

	public void setLogoNodeRef(String logoNodeRef) {
		this.logoNodeRef = logoNodeRef;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLegalInformations() {
		return legalInformations;
	}

	public void setLegalInformations(String legalInformations) {
		this.legalInformations = legalInformations;
	}

	public GeoPos getGeoPos() {
		return geoPos;
	}

	public void setGeoPos(GeoPos geoPos) {
		this.geoPos = geoPos;
	}

	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	public List<ContactItem> getContactItems() {
		return contactItems;
	}

	public void setContactItems(List<ContactItem> contactItems) {
		this.contactItems = contactItems;
	}

	// </editor-fold >
	public CompanyProperties() {
	}

	public CompanyProperties(String name) {
		this.name = name;
	}

	public HashMap<String, String> toHashMap() {
		HashMap<String, String> p = new HashMap<>();
		p.put("shortName", name);
		p.put("title", title);

		p.put("address", address);
		p.put("address2", address2);
		p.put("zipCode", zipCode);
		p.put("city", city);
		p.put("mailHeaderText",mailHeaderText);

		int indexfax = 1;
		int indextel = 1;
		int indexmail = 1;
		for (ContactItem ci : this.contactItems) {
			if (ci.getType() == ContactItem.TYPE_FAX) {
				p.put("fax" + indexfax, ci.getValue());
			}
			if (ci.getType() == ContactItem.TYPE_MAIL) {
				p.put("mail" + indexmail, ci.getValue());
			}
			if (ci.getType() == ContactItem.TYPE_TEL) {
				p.put("tel" + indextel, ci.getValue());
			}
		}

		return p;
	}
}
