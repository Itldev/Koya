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

package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.ElementSecurise;
import fr.itldev.koya.model.impl.Utilisateur;
import fr.itldev.koya.services.AlfrescoService;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.client.RestTemplate;

public class AlfrescoRestService implements AlfrescoService {

    protected static final String DESERIALISATION_ERROR = "Erreur de déserialisation de l'objet";
    protected static final String REST_DEL_NODE = "/s/api/node/{store_type}/{store_id}/{id}/tree";

    private String alfrescoServerUrl;

    private RestTemplate template;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getAlfrescoServerUrl() {
        return alfrescoServerUrl;
    }

    public void setAlfrescoServerUrl(String alfrescoServerUrl) {
        this.alfrescoServerUrl = alfrescoServerUrl;
    }

    public RestTemplate getTemplate() {
        return template;
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }

    // </editor-fold>
    /**
     * Supprime l'element passé en argument
     *
     * TODO protection pour la suppression de tous les sous elements ?
     *
     * @param user
     * @param element
     */
    @Override
    public void supprimer(Utilisateur user, ElementSecurise element) {
        user.getRestTemplate().delete(alfrescoServerUrl + REST_DEL_NODE, explodeNodeRef(element.getNodeRef()));
    }

    /*
     *  ================ Methodes Utiles ==================
     */
    /**
     * Extrait les elements d'un nodeRef passé sous forme brute
     *
     * @param nodeRef
     * @return
     */
    protected Map<String, String> explodeNodeRef(String nodeRef) {

        Map exploded = new HashMap<String, String>();

        Pattern p = Pattern.compile("(\\w+):\\/\\/(\\w+)\\/(.*)");
        Matcher m = p.matcher(nodeRef);

        if (m.find()) {
            exploded.put("store_type", m.group(1));
            exploded.put("store_id", m.group(2));
            exploded.put("id", m.group(3));

        }

        return exploded;
    }

    /**
     * Méthode qui Convertis un taille de données fournie en octets sous forme
     * lisible.
     *
     * @param bytes
     * @param si
     * @return
     */
    protected String getHumanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " o";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %so", bytes / Math.pow(unit, exp), pre);
    }

}
