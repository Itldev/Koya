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

import fr.itldev.koya.model.Conteneur;
import fr.itldev.koya.model.Contenu;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Repertoire;
import fr.itldev.koya.model.impl.Utilisateur;
import fr.itldev.koya.model.json.AlfrescoUploadReturn;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.ContenuService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class ContenuServiceImpl extends AlfrescoRestService implements ContenuService {

    private static final String REST_POST_ADDCONTENU = "/s/fr/itldev/koya/contenu/add";//+/{typeClass}
    private static final String REST_POST_LISTCONTENU = "/s/fr/itldev/koya/contenu/list";
    private static final String REST_POST_MOVECONTENU = "/s/fr/itldev/koya/contenu/move";
    private static final String REST_POST_UPLOAD = "/s/api/upload";

    /*TODO mettre en place un proxy pour l'upload/download de contenus comme ce qui est
     * fait pour Share.
     */
    @Override
    public Contenu creerContenu(Utilisateur user, Contenu aCreer) throws AlfrescoServiceException {
        //TODO template de remplacement de params
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_ADDCONTENU + "/" + aCreer.getClass().getSimpleName(), aCreer, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK) && ret.getNbitems() == 1) {
            return (Contenu) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    @Override
    public Document envoyerDocument(Utilisateur user, Resource r, Repertoire repertoire) throws AlfrescoServiceException {
        return upload(user, r, repertoire);
    }

    @Override
    public Document envoyerDocument(Utilisateur user, Resource r, Dossier dossier) throws AlfrescoServiceException {
        return upload(user, r, dossier);
    }

    @Override
    public Contenu deplacer(Utilisateur user, Contenu aDeplacer, Repertoire desination) throws AlfrescoServiceException {
        return move(user, aDeplacer, desination);
    }

    @Override
    public Contenu deplacer(Utilisateur user, Contenu aDeplacer, Dossier desination) throws AlfrescoServiceException {
        return move(user, aDeplacer, desination);
    }

    @Override
    public List<Contenu> lister(Utilisateur user, Dossier dossier) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_LISTCONTENU, dossier, ItlAlfrescoServiceWrapper.class);
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
    private Document upload(Utilisateur user, Resource resource, Conteneur parent) throws AlfrescoServiceException {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("filedata", resource);
        parts.add("destination", parent.getNodeRef());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);
        AlfrescoUploadReturn upReturn = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_UPLOAD, request, AlfrescoUploadReturn.class);

        Document docUpload;
        if (Dossier.class.isAssignableFrom(parent.getClass())) {
            docUpload = new Document(upReturn.getFileName(), (Dossier) parent);
        } else if (Repertoire.class.isAssignableFrom(parent.getClass())) {
            docUpload = new Document(upReturn.getFileName(), (Repertoire) parent);
        } else {
            throw new AlfrescoServiceException("Type de conteneur incorrect pour le parent du document a uploader");
        }

        docUpload.setNodeRef(upReturn.getNodeRef());

        return docUpload;
    }

    private Contenu move(Utilisateur user, Contenu contenu, Conteneur parent) throws AlfrescoServiceException {
        contenu.setParent(parent);
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_MOVECONTENU, contenu, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return (Contenu) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

}
