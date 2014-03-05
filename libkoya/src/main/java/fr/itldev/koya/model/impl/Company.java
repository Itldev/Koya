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

import fr.itldev.koya.model.Activable;
import fr.itldev.koya.model.Container;
import fr.itldev.koya.model.SecuredItem;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.cmr.site.SiteInfo;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * Title is a displayable label . It may be compound of spaces or spécial
 * characters
 *
 * Name (inherited attribute) is calculated from title.
 *
 */
public final class Company extends SecuredItem implements Container, Activable {

    private String title;//displayed title
    private Boolean active = Boolean.TRUE;

    private String currentSaleOfferNodeRef;
    @JsonIgnore
    private SalesOffer currentSaleOffer;
    @JsonIgnore
    private List<SalesOffer> saleOffersHistory;
    @JsonIgnore
    private List<Space> children = new ArrayList<>();

    

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Boolean getActive() {
        return active;
    }

    @Override
    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCurrentSaleOfferNodeRef() {
        return currentSaleOfferNodeRef;
    }

    public void setCurrentSaleOfferNodeRef(String currentSaleOfferNodeRef) {
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

    @Override
    public List<? extends SecuredItem> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<? extends SecuredItem> children) {
        this.children = (List<Space>) children;
    }

    // </editor-fold>
    public Company() {
    }

    public Company(String titre, SalesOffer offre) {
        this.title = titre;
        this.currentSaleOffer = offre;
    }

    public Company(SiteInfo siteInfo) {
        this.setName(siteInfo.getShortName());
        this.setTitle(siteInfo.getTitle());
        this.setNodeRefasObject(siteInfo.getNodeRef());
        //TODO path, parentNoderef
    }

    @Override
    public String toString() {
        return "Company {" + "name=" + getName() + ", title=" + title + ", nodeRef=" + getNodeRef() + '}';
    }
   
}
