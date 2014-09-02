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

import fr.itldev.koya.model.interfaces.Activable;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Template attribute exists to determinate Offer available for Sale and Offers
 * already attributed to a company.
 *
 */
public final class SalesOffer extends SecuredItem implements Activable {

    private static List<String> mandatoryProperties = asList("template", "nodeRef", "name", "active");

    private Map<String, String> data;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public Map<String, String> getDonnees() {
        return data;
    }

    public void setDonnees(Map<String, String> donnees) {
        this.data = donnees;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return data.get("name");
    }

    @JsonIgnore
    public Boolean getTemplate() {
        return Boolean.getBoolean(data.get("template"));
    }

    @JsonIgnore
    public Boolean getMultiSpaces() {
        return Boolean.getBoolean(data.get("multiSpaces"));
    }

    @JsonIgnore
    @Override
    public Boolean getActive() {
        return Boolean.getBoolean(data.get("active"));
    }

    @JsonIgnore
    public Long getQuotaMb() {
        return Long.valueOf(data.get("quotaMb"));
    }

    @JsonIgnore
    public Integer getLimitDossiers() {
        return Integer.valueOf(data.get("limitDossiers"));
    }

    @JsonIgnore
    public Integer getLimitSpaces() {
        return Integer.valueOf(data.get("limitSpaces"));
    }

    @JsonIgnore
    public String getExpiration() {
        return data.get("expiration");
    }

    // </editor-fold>
    private SalesOffer() {
    }

    public SalesOffer(Map<String, String> data) throws AlfrescoServiceException {
        for (String prop : mandatoryProperties) {
            if (!data.keySet().contains(prop)) {
                throw new AlfrescoServiceException(
                        "Invalid Sale Offer - Mandatory property not found : '" + prop + "'", KoyaErrorCodes.SALES_OFFER_LACK_MANDATORY_PROPERTY);
            }
        }
        this.setNodeRef(data.get("nodeRef"));
        this.data = data;
    }

    @Override
    public void setActive(Boolean active) {
        //TODO activation
    }
    
     @Override
    public String getType() {
        return "salesoffer";
    }

}
