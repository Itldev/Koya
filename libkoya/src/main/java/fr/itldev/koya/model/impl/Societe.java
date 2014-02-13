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
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.cmr.site.SiteInfo;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Le titre est le 'libellé affichable' de la société.Il peut etre composé
 * d'espaces et caractères spéciaux.
 *
 * le nom (attribut hérité) est déterminé en fonction du titre normalisé et
 * accolé d'un chiffre en cas de doublon. (effectué sur le serveur alfresco)
 *
 *
 *
 *
 *
 */
public final class Societe extends ElementSecurise implements Conteneur, Activable {

    private String titre;//nom Affichable : accents/espaces OK
    private Boolean active = Boolean.TRUE;

    private String offreEnCoursNodeRef;
    @JsonIgnore
    private OffreCommerciale offreEnCours;
    @JsonIgnore
    private List<OffreCommerciale> historique;
    @JsonIgnore
    private List<Espace> fils = new ArrayList<Espace>();

    private Long tailleOctets;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    @Override
    public Boolean getActive() {
        return active;
    }

    @Override
    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getOffreEnCoursNodeRef() {
        return offreEnCoursNodeRef;
    }

    public void setOffreEnCoursNodeRef(String offreEnCoursNodeRef) {
        this.offreEnCoursNodeRef = offreEnCoursNodeRef;
    }

    public OffreCommerciale getOffreEnCours() {
        return offreEnCours;
    }

    public void setOffreEnCours(OffreCommerciale offreEnCours) {
        this.offreEnCours = offreEnCours;
        if (offreEnCours != null) {
            setOffreEnCoursNodeRef(offreEnCours.getNodeRef());
        }
    }

    public List<OffreCommerciale> getHistorique() {
        return historique;
    }

    public void setHistorique(List<OffreCommerciale> historique) {
        this.historique = historique;
    }

    @Override
    public List<? extends ElementSecurise> getFils() {
        return fils;
    }

    @Override
    public void setFils(List<? extends ElementSecurise> fils) {
        this.fils = (List<Espace>) fils;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Constructeurs">
    public Societe() {
    }

    public Societe(String titre, OffreCommerciale offre) {
        this.titre = titre;
        this.offreEnCours = offre;
    }

    public Societe(SiteInfo siteInfo) {
        this.setNom(siteInfo.getShortName());
        this.setTitre(siteInfo.getTitle());
        this.setNodeRefasObject(siteInfo.getNodeRef());
        //TODO path, parentNoderef
    }

    // </editor-fold>
    @Override
    public String toString() {
        return "Societe{" + "nom=" + getNom() + ", titre=" + titre + ", nodeRef=" + getNodeRef() + '}';
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
