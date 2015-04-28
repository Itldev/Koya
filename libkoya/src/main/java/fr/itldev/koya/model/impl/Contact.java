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
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.itldev.koya.model.KoyaNode;

/**
 *
 *
 */
public class Contact extends KoyaNode {

    private User user;
    private List<ContactItem> contactItems = new ArrayList<>();
  
    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    
    // @JsonDeserialize(using = NodeRefDeserializer.class)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<ContactItem> getContactItems() {
        return contactItems;
    }

    public void setContactItems(List<ContactItem> contactItems) {
        this.contactItems = contactItems;
    }
   
    // </editor-fold >

    public Contact(NodeRef nodeRef) {
        super(nodeRef);
    }

    private Contact() {
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
        Contact other = (Contact) obj;
        if (nodeRef == null) {
            if (other.nodeRef != null)
                return false;
        } else if (!nodeRef.equals(other.nodeRef))
            return false;
        return true;
    }

    public static Contact newInstance(User user) {
        Contact c = new Contact();
        c.setUser(user);
        return c;
    }

}
