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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.services.impl.util.ContactItemListDeserializer;
import fr.itldev.koya.services.impl.util.ContactListDeserializer;
import fr.itldev.koya.services.impl.util.NodeRefDeserializer;

/**
 *
 *
 *
 */
public class CompanyProperties extends KoyaNode {

    
    private String address;
    private String address2;
    private String zipCode;
    private String city;
    private String mailHeaderText;
    private String description;
    private String legalInformations;
    
    private NodeRef logoNodeRef;
    
    private List<Contact> contacts = new ArrayList<>();
    private List<ContactItem> contactItems = new ArrayList<>();
    private GeoPos geoPos;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
   
    
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

    @JsonDeserialize(using = NodeRefDeserializer.class)
    public NodeRef getLogoNodeRef() {
        return logoNodeRef;
    }

    public void setLogoNodeRef(NodeRef logoNodeRef) {
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

    @JsonProperty("contacts")
    @JsonDeserialize(using = ContactListDeserializer.class)
    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @JsonProperty("contactItems")
    @JsonDeserialize(using = ContactItemListDeserializer.class)
    public List<ContactItem> getContactItems() {
        return contactItems;
    }

    public void setContactItems(List<ContactItem> contactItems) {
        this.contactItems = contactItems;
    }

    // </editor-fold >

    public CompanyProperties(){
        super();
    }
    
    public CompanyProperties(String name, NodeRef nodeRef) {
        super(nodeRef,name);
    }

    // complete json serialisation
    //TODO improve serialisation for mails
    @Deprecated
    public HashMap<String, String> toHashMap() {
        HashMap<String, String> p = new HashMap<>();
        p.put("shortName", name);
        p.put("title", title);

        p.put("address", address);
        p.put("address2", address2);
        p.put("zipCode", zipCode);
        p.put("city", city);
        p.put("mailHeaderText", mailHeaderText);

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

   

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();        
        try {
            return mapper.writeValueAsString(this);
        } catch ( IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return"";
    }

}
