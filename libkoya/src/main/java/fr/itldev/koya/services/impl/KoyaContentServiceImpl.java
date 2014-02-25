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

import fr.itldev.koya.model.Container;
import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Case;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.AlfrescoUploadReturn;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.KoyaContentService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class KoyaContentServiceImpl extends AlfrescoRestService implements KoyaContentService {

    private static final String REST_POST_ADDCONTENT = "/s/fr/itldev/koya/content/add";//+/{typeClass}
    private static final String REST_POST_LISTCONTENT = "/s/fr/itldev/koya/content/list";
    private static final String REST_POST_MOVECONTENT = "/s/fr/itldev/koya/content/move";
    private static final String REST_POST_UPLOAD = "/s/api/upload";

    /*TODO mettre en place un proxy pour l'upload/download de contenus comme ce qui est
     * fait pour Share.
     */
    @Override
    public Content create(User user, Content aCreer) throws AlfrescoServiceException {
        //TODO template de remplacement de params
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_ADDCONTENT + "/" + aCreer.getClass().getSimpleName(), aCreer, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK) && ret.getNbitems() == 1) {
            return (Content) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    @Override
    public Document upload(User user, Resource r, Directory repertoire) throws AlfrescoServiceException {
        return uploadPrivate(user, r, repertoire);
    }

    @Override
    public Document upload(User user, Resource r, Case dossier) throws AlfrescoServiceException {
        return uploadPrivate(user, r, dossier);
    }

    @Override
    public Content move(User user, Content aDeplacer, Directory desination) throws AlfrescoServiceException {
        return movePrivate(user, aDeplacer, desination);
    }

    @Override
    public Content move(User user, Content aDeplacer, Case desination) throws AlfrescoServiceException {
        return movePrivate(user, aDeplacer, desination);
    }

    @Override
    public List<Content> list(User user, Case dossier) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_LISTCONTENT, dossier, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    /**
     *
     * @param user
     * @param in
     * @param parent
     * @return
     * @throws AlfrescoServiceException
     */
    private Document uploadPrivate(User user, Resource resource, Container parent) throws AlfrescoServiceException {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("filedata", resource);
        parts.add("destination", parent.getNodeRef());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);
        AlfrescoUploadReturn upReturn = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_UPLOAD, request, AlfrescoUploadReturn.class);

        Document docUpload;
        if (Case.class.isAssignableFrom(parent.getClass())) {
            docUpload = new Document(upReturn.getFileName(), (Case) parent);
        } else if (Directory.class.isAssignableFrom(parent.getClass())) {
            docUpload = new Document(upReturn.getFileName(), (Directory) parent);
        } else {
            throw new AlfrescoServiceException("Type de conteneur incorrect pour le parent du document a uploader");
        }

        docUpload.setNodeRef(upReturn.getNodeRef());

        return docUpload;
    }

    private Content movePrivate(User user, Content contenu, Container parent) throws AlfrescoServiceException {
        contenu.setParent(parent);
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_MOVECONTENT, contenu, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return (Content) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

}
