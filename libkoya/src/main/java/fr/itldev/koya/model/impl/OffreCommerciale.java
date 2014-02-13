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
import fr.itldev.koya.model.ElementSecurise;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * L'attribut template permet de différencier les offres disponibles à la vente
 * et celles attribuées à une société.
 *
 */
public final class OffreCommerciale extends ElementSecurise implements Activable{

    /**
     * Propriétés minimales obligatoires
     */
    private static List<String> propsObligatoires = asList("template", "nodeRef", "nom", "active");

    private Map<String, String> donnees;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public Map<String, String> getDonnees() {
        return donnees;
    }

    public void setDonnees(Map<String, String> donnees) {
        this.donnees = donnees;
    }

    @Override
    @JsonIgnore
    public String getNom() {
        return donnees.get("nom");
    }

    @JsonIgnore
    public Boolean getTemplate() {
        return Boolean.getBoolean(donnees.get("template"));
    }

    @JsonIgnore
    public Boolean getMultiEspaces() {
        return Boolean.getBoolean(donnees.get("multiEspaces"));
    }

    @JsonIgnore
    @Override
    public Boolean getActive() {
        return Boolean.getBoolean(donnees.get("active"));
    }

    @JsonIgnore
    public Long getQuotaMo() {
        return Long.valueOf(donnees.get("quotaMo"));
    }

    @JsonIgnore
    public Integer getLimitDossiers() {
        return Integer.valueOf(donnees.get("limitDossiers"));
    }

    @JsonIgnore
    public Integer getLimitEspaces() {
        return Integer.valueOf(donnees.get("limitEspaces"));
    }

    @JsonIgnore
    public String getExpiration() {
        //TODO travailler le format ...
        return donnees.get("expiration");
    }

    // </editor-fold>
    private OffreCommerciale() {
    }

    public OffreCommerciale(Map<String, String> donnees) throws AlfrescoServiceException {
        for (String prop : propsObligatoires) {
            if (!donnees.keySet().contains(prop)) {
                throw new AlfrescoServiceException("L'offre commerciale n'est pas valide car elle ne contiens pas la propriété obligatoire : '" + prop + "'");
            }
        }
        this.setNodeRef(donnees.get("nodeRef"));
        this.donnees = donnees;
    }

    @Override
    public void setActive(Boolean active) {
        //TODO activation
    }

}
