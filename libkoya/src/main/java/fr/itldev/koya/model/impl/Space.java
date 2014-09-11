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

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.interfaces.Container;
import fr.itldev.koya.model.interfaces.SubSpace;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public final class Space extends SecuredItem implements Container, SubSpace {

    private Boolean active = Boolean.TRUE;
    /*---------------------*/
    @JsonIgnore
    private SecuredItem parent;

    @JsonProperty("childspaces")
    private List<Space> childSpaces = new ArrayList<>();

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public List<Space> getChildSpaces() {
        return childSpaces;
    }

    public void setChildSpaces(List<Space> childSpaces) {
        this.childSpaces = childSpaces;
    }

    // </editor-fold>
    public Space() {
    }

    public Space(String name) {
        setName(name);
    }

    @Override
    public String toString() {
        return "Space [ name = " + getName() + ", noderef=" + getNodeRef() + " , active =" + active + "]";
    }

    @Override
    public String getType() {
        return "space";
    }

}
