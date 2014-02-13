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
import fr.itldev.koya.model.Conteneur;
import fr.itldev.koya.model.ElementSecurise;
import fr.itldev.koya.model.SousEspace;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

public final class Espace extends SousEspace implements Conteneur, Activable {

    private Boolean active = Boolean.TRUE;
    /*---------------------*/
    /**
     * La définition de l'element parent (espace ou Société) permet d'effectuer
     * rapidement une représentation arborescente dans l'interface client.
     */
    @JsonIgnore
    private ElementSecurise parent;
    @JsonIgnore
    private List<SousEspace> fils = new ArrayList<SousEspace>();
    private Long tailleOctets;

    @Override
    public List<SousEspace> getFils() {
        return fils;
    }

    @Override
    public void setFils(List<? extends ElementSecurise> fils) {
        this.fils = (List<SousEspace>) fils;
    }

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @Override
    public Boolean getActive() {
        return active;
    }

    @Override
    public void setActive(Boolean active) {
        this.active = active;
    }

    @JsonIgnore
    public void setParent(Espace e) {
        this.parent = e;
        setParentNodeRef(e.getNodeRef());
    }

    @JsonIgnore
    public void setParent(Societe s) {
        this.parent = s;
        setParentNodeRef(s.getNodeRef());
    }

    public ElementSecurise getParent() {
        return parent;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructeurs">
    public Espace() {
    }

    public Espace(String nom, Societe parent) {
        setNom(nom);
        setParent(parent);
    }

    public Espace(String nom, Espace parent) {
        setNom(nom);
        setParent(parent);
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Espace [" + getNom() + ", noderef=" + getNodeRef() + " , active =" + active + ", parentNoderef= " + getParentNodeRef() + "]";
    }

    @Override
    public Long getTailleOctets() {
        return tailleOctets;
    }

    @Override
    public void setTailleOctets(Long tailleOctets) {
        this.tailleOctets = tailleOctets;
    }
    
    

}
