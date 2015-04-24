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
import fr.itldev.koya.services.SpaceService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import static fr.itldev.koya.services.impl.AlfrescoRestService.fromJSON;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.NotImplementedException;
import org.codehaus.jackson.type.TypeReference;

public class SpaceServiceImpl extends AlfrescoRestService implements SpaceService {

    private static final String REST_POST_ADDSPACE = "/s/fr/itldev/koya/space/add/{parentNodeRef}";
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
        return fromJSON(new TypeReference<Space>() {
        }, user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_ADDSPACE,
                space, String.class, parent.getNodeRef()));
    }

    @Override
    public void enable(User user, Space space) throws AlfrescoServiceException {
        //TODO specific implementation
        throw new NotImplementedException();
    }

    @Override
    public void disable(User user, Space space) throws AlfrescoServiceException {
        throw new NotImplementedException();
    }

    @Override
    public List<Space> list(User user, Company company, Integer... depth) throws AlfrescoServiceException {
        if (depth.length > 0) {
            return fromJSON(new TypeReference<List<Space>>() {
            }, user.getRestTemplate().postForObject(getAlfrescoServerUrl()
                    + REST_POST_LISTSPACE_DEPTH_OPTION, company, String.class, depth[0]));
        } else {
            return fromJSON(new TypeReference<List<Space>>() {
            }, user.getRestTemplate().postForObject(getAlfrescoServerUrl()
                    + REST_POST_LISTSPACE, company, String.class));
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

    private Space movePrivate(User user, Space toMove, NodeRef newParentNodeRef) throws AlfrescoServiceException {
        return fromJSON(new TypeReference<Space>() {
        }, user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_MOVESPACE,
                toMove, String.class, newParentNodeRef.toString()));
    }

}
