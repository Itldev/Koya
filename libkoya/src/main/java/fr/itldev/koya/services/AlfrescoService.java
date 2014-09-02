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
package fr.itldev.koya.services;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.MetaInfos;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.MailWrapper;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public interface AlfrescoService {

    /**
     * Checks if library version match with server one.
     * @return 
     */
    Boolean checkLibVersionMatch();

    /**
     * Get Informations about server and modules.
     *
     * @param user
     * @return
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    MetaInfos getServerInfos(User user) throws AlfrescoServiceException;

    /**
     *
     * @param user
     * @param wrapper
     * @throws AlfrescoServiceException
     */
    void sendMail(User user, MailWrapper wrapper) throws AlfrescoServiceException;

    /**
     * Get SecuredItem from noderef reference.
     *
     * @param user
     * @param nodeRef
     * @return
     * @throws AlfrescoServiceException
     */
    SecuredItem getSecuredItem(User user, String nodeRef) throws AlfrescoServiceException;

}
