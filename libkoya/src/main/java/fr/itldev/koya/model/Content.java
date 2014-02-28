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
package fr.itldev.koya.model;

import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.services.impl.util.ContentDeserializer;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

@JsonDeserialize(using = ContentDeserializer.class)
public abstract class Content extends SecuredItem {

    @JsonIgnore
    private Container parent;

   

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public Container getParent() {
        return parent;
    }

    public final void setParent(Container parent) {
        this.parent = parent;
        this.setParentNodeRef(parent.getNodeRef());
    }

    // </editor-fold>
    public Content() {
    }

    public Content(String name, Directory parent) {
        this.setName(name);
        this.setParent(parent);
    }

    public Content(String name, Dossier parent) {
        this.setName(name);
        this.setParent(parent);
    }

    public Content(String nodeRef, String path, String name, String parentNodeRef) {
        super(nodeRef, path, name, parentNodeRef);
    }

   
    /**
     * Useful method to deserialize content.
     *
     * @return
     */
    public String getContentType() {
        return this.getClass().getCanonicalName();
    }

}
