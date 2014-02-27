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

import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.DossierService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public class DossierServiceImpl extends AlfrescoRestService implements DossierService {

    private static final String REST_POST_ADDDOSSIER = "/s/fr/itldev/koya/dossier/add";
    private static final String REST_POST_LISTCHILD = "/s/fr/itldev/koya/dossier/list?filter={filter}";

    @Override
    public Dossier create(User user, Dossier dossier) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_ADDDOSSIER, dossier, ItlAlfrescoServiceWrapper.class);

        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK) && ret.getNbitems() == 1) {
            return (Dossier) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    @Override
    public Dossier edit(User user, Dossier dossier) throws AlfrescoServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Liste tous les dossiers d'un espace
     *
     * @param user
     * @param space
     * @throws AlfrescoServiceException
     */
    @Override
    public List<Dossier> list(User user, Space space, String... filter) throws AlfrescoServiceException {
        String filtre = "";
        if (filter.length == 1) {
            filtre = filter[0];
        }
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_LISTCHILD, space, ItlAlfrescoServiceWrapper.class, filtre);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

}
