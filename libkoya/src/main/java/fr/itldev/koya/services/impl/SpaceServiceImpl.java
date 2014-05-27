/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.SpaceService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public class SpaceServiceImpl extends AlfrescoRestService implements SpaceService {

    private static final String REST_POST_ADDSPACE = "/s/fr/itldev/koya/space/add/{parentNodeRef}";
    private static final String REST_POST_TOGGLEACTIVE = "/s/fr/itldev/koya/global/toggleactive";
    private static final String REST_POST_LISTSPACE = "/s/fr/itldev/koya/space/list";
    private static final String REST_POST_LISTSPACE_DEPTH_OPTION = "/s/fr/itldev/koya/space/list?maxdepth={maxdepth}";
    private static final String REST_POST_MOVESPACE = "/s/fr/itldev/koya/space/move/{newParentNodeRef}";

    /**
     * Create a new space
     *
     * @param user
     * @param space
     * @param parent
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public Space create(User user, Space space, SecuredItem parent) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_ADDSPACE, space, ItlAlfrescoServiceWrapper.class, parent.getNodeRef());
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
            changeActivityStatus(user, space);
        }
    }

    @Override
    public void disable(User user, Space space) throws AlfrescoServiceException {
        if (space.getActive()) {
            space.setActive(Boolean.FALSE);
            changeActivityStatus(user, space);
        }
    }

    @Override
    public List<Space> list(User user, Company company, Integer... depth) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret;
        if (depth.length > 0) {
            ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_LISTSPACE_DEPTH_OPTION, company, ItlAlfrescoServiceWrapper.class, depth[0]);
        } else {
            ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_LISTSPACE, company, ItlAlfrescoServiceWrapper.class);
        }

        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    private void changeActivityStatus(User user, Space space) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_TOGGLEACTIVE, space, ItlAlfrescoServiceWrapper.class);
        if (!ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    @Override
    public Space move(User user, Space toMove, Space destination) throws AlfrescoServiceException {
        return movePrivate(user, toMove, destination.getNodeRef());
    }

    @Override
    public Space move(User user, Space toMove, Company destination) throws AlfrescoServiceException {
        return movePrivate(user, toMove, destination.getNodeRef());
    }

    private Space movePrivate(User user, Space toMove, String newParentNodeRef) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_MOVESPACE, toMove, ItlAlfrescoServiceWrapper.class, newParentNodeRef);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return (Space) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

}
