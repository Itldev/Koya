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

import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.SpaceService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceServiceImpl extends AlfrescoRestService implements SpaceService {

    private static final String REST_POST_ADDSPACE = "/s/fr/itldev/koya/space/add";
    private static final String REST_POST_TOGGLEACTIVE = "/s/fr/itldev/koya/global/toggleactive";
    private static final String REST_POST_LISTSPACE = "/s/fr/itldev/koya/space/list";

    @Override
    public Space create(User user, Space space) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_ADDSPACE, space, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK) && ret.getNbitems() == 1) {
            return (Space) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    @Override
    public void enable(User user, Space space) throws AlfrescoServiceException {
        if (!space.getActive()) {
            space.setActive(Boolean.TRUE);
            changerStatutActivite(user, space);
        }
    }

    @Override
    public void disable(User user, Space space) throws AlfrescoServiceException {
        if (space.getActive()) {
            space.setActive(Boolean.FALSE);
            changerStatutActivite(user, space);
        }
    }

    @Override
    public List<Space> list(User user, Company societe) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_LISTSPACE, societe, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    /**
     * Méthode qui assure la réorganisation de la liste plate des spaces afin
     * d'offrir une vision arborescente.
     *
     * @param user
     * @param societe
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public List<Space> listAsTree(User user, Company societe) throws AlfrescoServiceException {

        List<Space> lstArbo = new ArrayList<>();

        Map<String, Space> mapNr = new HashMap<>();
        for (Space e : list(user, societe)) {
            mapNr.put(e.getNodeRef(), e);
        }

        for (Space esp : mapNr.values()) {

            if (esp.getParentNodeRef().equals(societe.getNodeRef())) {
                esp.setParent(societe);
                lstArbo.add(esp);
            } else {
                esp.setParent(mapNr.get(esp.getParentNodeRef()));
                mapNr.get(esp.getParentNodeRef()).getChildren().add(esp);

            }
        }

        return lstArbo;
    }

    private void changerStatutActivite(User user, Space space) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_TOGGLEACTIVE, space, ItlAlfrescoServiceWrapper.class);
        if (!ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

}
