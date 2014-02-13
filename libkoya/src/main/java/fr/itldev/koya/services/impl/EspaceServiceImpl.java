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

import fr.itldev.koya.model.impl.Espace;
import fr.itldev.koya.model.impl.Societe;
import fr.itldev.koya.model.impl.Utilisateur;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.EspaceService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EspaceServiceImpl extends AlfrescoRestService implements EspaceService {

    private static final String REST_POST_ADDESPACE = "/s/fr/itldev/koya/espace/add";
    private static final String REST_POST_CHANGERSTATUTACTIF_ESPACE = "/s/fr/itldev/koya/global/toggleactive";
    private static final String REST_POST_LISTESPACE = "/s/fr/itldev/koya/espace/list";

    @Override
    public Espace creerNouveau(Utilisateur user, Espace espace) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_ADDESPACE, espace, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK) && ret.getNbitems() == 1) {
            return (Espace) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    @Override
    public void activer(Utilisateur user, Espace espace) throws AlfrescoServiceException {
        if (!espace.getActive()) {
            espace.setActive(Boolean.TRUE);
            changerStatutActivite(user, espace);
        }
    }

    @Override
    public void desactiver(Utilisateur user, Espace espace) throws AlfrescoServiceException {
        if (espace.getActive()) {
            espace.setActive(Boolean.FALSE);
            changerStatutActivite(user, espace);
        }
    }

    @Override
    public List<Espace> listEspaces(Utilisateur user, Societe societe) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_LISTESPACE, societe, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    /**
     * Méthode qui assure la réorganisation de la liste plate des espaces afin
     * d'offrir une vision arborescente.
     *
     * @param user
     * @param societe
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public List<Espace> listEspacesArbo(Utilisateur user, Societe societe) throws AlfrescoServiceException {

        List<Espace> lstArbo = new ArrayList<Espace>();

        Map<String, Espace> mapNr = new HashMap<String, Espace>();
        for (Espace e : listEspaces(user, societe)) {
            mapNr.put(e.getNodeRef(), e);
        }

        for (Espace esp : mapNr.values()) {

            if (esp.getParentNodeRef().equals(societe.getNodeRef())) {
                esp.setParent(societe);
                lstArbo.add(esp);
            } else {
                esp.setParent(mapNr.get(esp.getParentNodeRef()));
                mapNr.get(esp.getParentNodeRef()).getFils().add(esp);

            }
        }

        return lstArbo;
    }

    private void changerStatutActivite(Utilisateur user, Espace espace) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_CHANGERSTATUTACTIF_ESPACE, espace, ItlAlfrescoServiceWrapper.class);
        if (!ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

}
