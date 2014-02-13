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

import fr.itldev.koya.model.impl.Repertoire;
import fr.itldev.koya.model.impl.Dossier;
import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class Contenu extends ElementSecurise {

    @JsonIgnore
    private Conteneur parent;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public Conteneur getParent() {
        return parent;
    }

    public final void setParent(Conteneur parent) {
        this.parent = parent;
        this.setParentNodeRef(parent.getNodeRef());
    }

    // </editor-fold>
    public Contenu() {
    }

    public Contenu(String nom, Repertoire pere) {
        this.setNom(nom);
        this.setParent(pere);
    }

    public Contenu(String nom, Dossier pere) {
        this.setNom(nom);
        this.setParent(pere);
    }

    public Contenu(String nodeRef, String path, String nom, String parentNodeRef) {
        super(nodeRef, path, nom, parentNodeRef);
    }

}
