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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.json.util.NodeRefDeserializer;

/**
 *
 * Title is a displayable label . It may be compound of spaces or spécial
 * characters
 *
 * Name (inherited attribute) is calculated from title.
 *
 */
public final class Company extends KoyaNode {

    // private String title;//displayed title
    private NodeRef currentSaleOfferNodeRef;
    @JsonIgnore
    private SalesOffer currentSaleOffer;
    @JsonIgnore
    private List<SalesOffer> saleOffersHistory;
    @JsonIgnore
    private List<Space> children = new ArrayList<>();

    protected String ftpUsername;
    
    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @JsonDeserialize(using = NodeRefDeserializer.class)
    public NodeRef getCurrentSaleOfferNodeRef() {
        return currentSaleOfferNodeRef;
    }

    public void setCurrentSaleOfferNodeRef(NodeRef currentSaleOfferNodeRef) {
        this.currentSaleOfferNodeRef = currentSaleOfferNodeRef;
    }

    public SalesOffer getCurrentSaleOffer() {
        return currentSaleOffer;
    }

    public void setCurrentSaleOffer(SalesOffer currentSaleOffer) {
        this.currentSaleOffer = currentSaleOffer;
        if (currentSaleOffer != null) {
            setCurrentSaleOfferNodeRef(currentSaleOffer.getNodeRef());
        }
    }

    public List<SalesOffer> getSaleOffersHistory() {
        return saleOffersHistory;
    }

    public void setSaleOffersHistory(List<SalesOffer> saleOffersHistory) {
        this.saleOffersHistory = saleOffersHistory;
    }

    public List<? extends KoyaNode> getChildren() {
        return children;
    }

    @SuppressWarnings("unchecked")
	public void setChildren(List<? extends KoyaNode> children) {
        this.children = (List<Space>) children;
    }

    
    public String getFtpUsername() {
		return ftpUsername;
	}

	public void setFtpUsername(String ftpUsername) {
		this.ftpUsername = ftpUsername;
	}

	// </editor-fold>
    private Company() {
    }

    @Override
    public String toString() {
        return "Company {" + "name=" + getName() + ", title=" + getTitle() + ", nodeRef=" + getNodeRef() + '}';
    }


    public static Company newInstance() {
        return new Company();
    }
}
