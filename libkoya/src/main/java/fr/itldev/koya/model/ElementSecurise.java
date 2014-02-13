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

package fr.itldev.koya.model;

import java.io.IOException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class ElementSecurise {

    private String nodeRef;
    private String path;
    private String nom;
    private String parentNodeRef;
    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">

    public String getNodeRef() {
        return nodeRef;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    public void setNodeRefasObject(NodeRef nodeRef) {
        this.nodeRef = nodeRef.toString();
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getParentNodeRef() {
        return parentNodeRef;
    }

    public void setParentNodeRef(String parentNodeRef) {
        this.parentNodeRef = parentNodeRef;
    }

    public void setParentNodeRefasObject(NodeRef parentNodeRef) {
        this.parentNodeRef = parentNodeRef.toString();
    }

    // </editor-fold>
    public ElementSecurise() {
    }

    public ElementSecurise(String nodeRef, String path, String nom, String parentNodeRef) {
        this.nodeRef = nodeRef;
        this.path = path;
        this.nom = nom;
        this.parentNodeRef = parentNodeRef;
    }

    @JsonIgnore
    public String getCompleteFtpPath() {
        return (path + "/" + nom).replace("Espace racine", "Alfresco");
    }

    @JsonIgnore
    public String getFtpPath() {
        return (path + "/").replace("Espace racine", "Alfresco");
    }

    @JsonIgnore
    public NodeRef getNodeRefasObject() {
        return new NodeRef(this.nodeRef);
    }

    @JsonIgnore
    public NodeRef getParentNodeRefasObject() {
        return new NodeRef(this.parentNodeRef);
    }

    private static final Integer HASHCONST1 = 3;
    private static final Integer HASHCONST2 = 47;

    @Override
    public int hashCode() {
        int hash = HASHCONST1;
        hash = HASHCONST2 * hash + (getNodeRef() != null ? getNodeRef().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ElementSecurise other = (ElementSecurise) obj;
        if ((getNodeRef() == null) ? (other.getNodeRef() != null) : !this.getNodeRef().equals(other.getNodeRef())) {
            return false;
        }
        return true;
    }

    @JsonIgnore
    public String getAsJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}
