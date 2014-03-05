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
import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.SubSpace;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

public final class Dossier extends SubSpace implements Container, Activable {

    private Boolean active = Boolean.TRUE;
    @JsonIgnore
    private Space parentSpace;

    @JsonIgnore
    private List<Content> children = new ArrayList<>();

    private Long byteSize;

    private Date lastModifiedDate;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @Override
    public Boolean getActive() {
        return active;
    }

    @Override
    public void setActive(Boolean active) {
        this.active = active;
    }

    public Space getParentSpace() {
        return parentSpace;
    }

    public void setParentSpace(Space parentSpace) {
        this.parentSpace = parentSpace;
        setParentNodeRef(parentSpace.getNodeRef());
    }

    @Override
    public List<Content> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<? extends SecuredItem> children) {
        this.children = (List<Content>) children;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="constructeurs">
    public Dossier(String name, Space parentSpace) {
        setName(name);
        setParentSpace(parentSpace);
    }

    public Dossier() {
    }

    // </editor-fold>
    @Override
    public Long getByteSize() {
        return byteSize;
    }

    @Override
    public void setByteSize(Long byteSize) {
        this.byteSize = byteSize;
    }

}