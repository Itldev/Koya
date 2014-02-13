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

import fr.itldev.koya.services.impl.util.ItlAlfrescoServiceWrapperDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Classe de modele pour le retour des services alfresco
 *
 */
@JsonDeserialize(using = ItlAlfrescoServiceWrapperDeserializer.class)
public class ItlAlfrescoServiceWrapper {

    public static final String STATUS_NOK = "NOK";
    public static final String STATUS_OK = "OK";

    private String status;
    private String message;
    private List items = new ArrayList();
    private Integer nbitems;
    private String typeItems;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        this.items = items;
    }

    public Integer getNbitems() {
        return nbitems;
    }

    public void setNbitems(Integer nbitems) {
        this.nbitems = nbitems;
    }

    public String getTypeItems() {
        return typeItems;
    }

    public void setTypeItems(String typeItems) {
        this.typeItems = typeItems;
    }

    // </editor-fold>
    public void addItem(Object item) {
        CollectionUtils.addIgnoreNull(this.items, item);
    }

    public void addItems(List items) {
        for (Object o : items) {
            addItem(o);
        }
    }

    public void setStatusOK() {
        this.status = STATUS_OK;

        //Suppression des items null
        this.nbitems = this.items.size();
        //TODO vérifier que tous les items sont du meme type !!! sinon exception
        if (this.nbitems > 0) {
            this.typeItems = items.get(0).getClass().getCanonicalName();
        }
    }

    public void setStatusFail(String failMessage) {
        this.status = STATUS_NOK;
        this.message = failMessage;
    }

    @JsonIgnore
    public String getAsJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}
