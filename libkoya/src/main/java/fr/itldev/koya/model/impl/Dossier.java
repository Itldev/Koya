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
import fr.itldev.koya.model.Contenu;
import fr.itldev.koya.model.ElementSecurise;
import fr.itldev.koya.model.SousEspace;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

public final class Dossier extends SousEspace implements Conteneur, Activable {

    private Boolean active = Boolean.TRUE;
    @JsonIgnore
    private Espace espaceParent;

    @JsonIgnore
    private List<Contenu> fils = new ArrayList<Contenu>();

    private Long tailleOctets;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @Override
    public Boolean getActive() {
        return active;
    }

    @Override
    public void setActive(Boolean active) {
        this.active = active;
    }

    public Espace getEspaceParent() {
        return espaceParent;
    }

    public void setEspaceParent(Espace espaceParent) {
        this.espaceParent = espaceParent;
        setParentNodeRef(espaceParent.getNodeRef());
    }

    @Override
    public List<Contenu> getFils() {
        return fils;
    }

    @Override
    public void setFils(List<? extends ElementSecurise> fils) {
        this.fils = (List<Contenu>) fils;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="constructeurs">
    public Dossier(String nom, Espace espaceParent) {
        setNom(nom);
        setEspaceParent(espaceParent);
    }

    public Dossier() {
    }

    // </editor-fold>
    @Override
    public Long getTailleOctets() {
        return tailleOctets;
    }

    @Override
    public void setTailleOctets(Long tailleOctets) {
        this.tailleOctets = tailleOctets;
    }

}
