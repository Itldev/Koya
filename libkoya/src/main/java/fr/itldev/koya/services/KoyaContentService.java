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

import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.Resource;

public interface KoyaContentService extends AlfrescoService {

    Content create(User user, Content toCreate, Directory parent) throws AlfrescoServiceException;

    Content create(User user, Content toCreate, Dossier parent) throws AlfrescoServiceException;

    Document upload(User user, Resource r, Directory repertoire) throws AlfrescoServiceException;

    Document upload(User user, Resource r, Dossier dossier) throws AlfrescoServiceException;

    Content move(User user, Content aDeplacer, Directory desination) throws AlfrescoServiceException;

    Content move(User user, Content aDeplacer, Dossier desination) throws AlfrescoServiceException;

    List<Content> list(User user, Dossier dossier, Boolean onlyFolders, Integer... depth) throws AlfrescoServiceException;

    List<Content> list(User user, Directory dir, Boolean onlyFolders, Integer... depth) throws AlfrescoServiceException;

    Long getDiskSize(User user, SecuredItem securedItem) throws AlfrescoServiceException;

    InputStream getZipInputStream(User user, List<SecuredItem> securedItems) throws AlfrescoServiceException;
}
