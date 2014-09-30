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
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.Content;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.io.InputStream;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.core.io.Resource;

public interface KoyaContentService extends AlfrescoService {

    Directory createDir(User user, NodeRef parent, String title) throws AlfrescoServiceException;

    Document upload(User user, NodeRef parent, Resource r) throws AlfrescoServiceException;

    Content move(User user, NodeRef contentToMove, NodeRef destination) throws AlfrescoServiceException;

    Content copy(User user, NodeRef contentToCopy, NodeRef destination) throws AlfrescoServiceException;

    List<Content> list(User user, NodeRef containerToList, Boolean onlyFolders, Integer... depth) throws AlfrescoServiceException;

    Long getDiskSize(User user, SecuredItem securedItem) throws AlfrescoServiceException;

    InputStream getZipInputStream(User user, List<SecuredItem> securedItems) throws AlfrescoServiceException;

    void importZipedContent(User user, Document zipFile) throws AlfrescoServiceException;

}
