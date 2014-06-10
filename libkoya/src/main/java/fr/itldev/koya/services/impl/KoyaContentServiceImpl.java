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

import fr.itldev.koya.model.Container;
import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.AlfrescoUploadReturn;
import fr.itldev.koya.model.json.DiskSizeWrapper;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.KoyaContentService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class KoyaContentServiceImpl extends AlfrescoRestService implements KoyaContentService {

    private static final String REST_POST_ADDCONTENT = "/s/fr/itldev/koya/content/add/{parentNodeRef}";
    private static final String REST_POST_LISTCONTENT_DEPTH_OPTION = "/s/fr/itldev/koya/content/list/{onlyFolders}?maxdepth={maxdepth}";
    private static final String REST_POST_LISTCONTENT = "/s/fr/itldev/koya/content/list/{onlyFolders}";
    private static final String REST_POST_MOVECONTENT = "/s/fr/itldev/koya/content/move/{parentNodeRef}";
    private static final String REST_GET_SECUREDITEM = "/s/fr/itldev/koya/global/getsecureditem/{nodeRef}";
    private static final String REST_GET_DISKSIZE = "/s/fr/itldev/koya/global/disksize/{nodeRef}";

    private static final String REST_POST_UPLOAD = "/s/api/upload";

    private static final String DOWNLOAD_ZIP_WS_URI = "/s/fr/itldev/koya/content/zip?alf_ticket=";

    @Override
    public Content create(User user, Content toCreate, Directory parent) throws AlfrescoServiceException {
        return createImpl(user, toCreate, parent);
    }

    @Override
    public Content create(User user, Content toCreate, Dossier parent) throws AlfrescoServiceException {
        return createImpl(user, toCreate, parent);
    }

    @Override
    public Document upload(User user, Resource r, Directory repertoire) throws AlfrescoServiceException {
        return uploadPrivate(user, r, repertoire);
    }

    @Override
    public Document upload(User user, Resource r, Dossier dossier) throws AlfrescoServiceException {
        return uploadPrivate(user, r, dossier);
    }

    @Override
    public Content move(User user, Content aDeplacer, Directory desination) throws AlfrescoServiceException {
        return moveImpl(user, aDeplacer, desination);
    }

    @Override
    public Content move(User user, Content aDeplacer, Dossier desination) throws AlfrescoServiceException {
        return moveImpl(user, aDeplacer, desination);
    }

    @Override
    public List<Content> list(User user, Dossier dossier, Boolean onlyFolders, Integer... depth) throws AlfrescoServiceException {
        return listContent(user, dossier, onlyFolders, depth);
    }

    @Override
    public List<Content> list(User user, Directory dir, Boolean onlyFolders, Integer... depth) throws AlfrescoServiceException {
        return listContent(user, dir, onlyFolders, depth);
    }

    private Content createImpl(User user, Content content, Container parent) throws AlfrescoServiceException {

        if (parent.getNodeRef() == null) {
            throw new AlfrescoServiceException("parent noderef must be set", 0);
        }

        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_ADDCONTENT, content,
                ItlAlfrescoServiceWrapper.class, parent.getNodeRef());
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK) && ret.getNbitems() == 1) {
            return (Content) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }

    private List<Content> listContent(User user, SecuredItem container, Boolean onlyFolders, Integer... depth) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret;
        if (depth.length > 0) {
            ret = user.getRestTemplate().postForObject(
                    getAlfrescoServerUrl() + REST_POST_LISTCONTENT_DEPTH_OPTION, container, ItlAlfrescoServiceWrapper.class, onlyFolders, depth[0]);
        } else {
            ret = user.getRestTemplate().postForObject(
                    getAlfrescoServerUrl() + REST_POST_LISTCONTENT, container, ItlAlfrescoServiceWrapper.class, onlyFolders);
        }

        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
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
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("filedata", resource);
        parts.add("destination", parent.getNodeRef());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(parts, headers);
        AlfrescoUploadReturn upReturn = user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_UPLOAD, request, AlfrescoUploadReturn.class);

        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_SECUREDITEM, ItlAlfrescoServiceWrapper.class, upReturn.getNodeRef());
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK) && ret.getNbitems() == 1) {
            return (Document) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }

    private Content moveImpl(User user, Content contenu, Container parent) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_MOVECONTENT, contenu, ItlAlfrescoServiceWrapper.class, parent.getNodeRef());
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return (Content) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }

    @Override
    public Long getDiskSize(User user, SecuredItem securedItem) throws AlfrescoServiceException {
        DiskSizeWrapper ret = user.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_DISKSIZE, DiskSizeWrapper.class, securedItem.getNodeRef());
        return ret.getSize();
    }

    @Override
    public InputStream getZipInputStream(User user, List<SecuredItem> securedItems) throws AlfrescoServiceException {
        HttpURLConnection con;

        try {
            String urlDownload = getAlfrescoServerUrl() + DOWNLOAD_ZIP_WS_URI + user.getTicketAlfresco();

            Map<String, Serializable> params = new HashMap<>();
            ArrayList<String> selected = new ArrayList<>();
            params.put("nodeRefs", selected);
            for (SecuredItem item : securedItems) {
                selected.add(item.getNodeRef());
            }

            JSONObject postParams = new JSONObject(params);

            con = (HttpURLConnection) new URL(urlDownload).openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json");

            con.getOutputStream().write(postParams.toString().getBytes());

            return con.getInputStream();

        } catch (Exception e) {
            throw new AlfrescoServiceException(e.getMessage(), e);
        }
    }

}
