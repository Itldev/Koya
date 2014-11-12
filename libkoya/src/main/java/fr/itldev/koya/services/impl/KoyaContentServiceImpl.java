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

import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.interfaces.Content;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.AlfrescoUploadReturn;
import fr.itldev.koya.model.json.DiskSizeWrapper;
import fr.itldev.koya.services.KoyaContentService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import static fr.itldev.koya.services.impl.AlfrescoRestService.fromJSON;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.codehaus.jackson.type.TypeReference;
import org.json.simple.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class KoyaContentServiceImpl extends AlfrescoRestService implements KoyaContentService {

    private static final String REST_GET_CREATEDIR = "/s/fr/itldev/koya/content/createdir/{parentNodeRef}?title={title}";
    private static final String REST_GET_MOVECONTENT = "/s/fr/itldev/koya/content/move/{nodeRef}?destNodeRef={destNodeRef}";
    private static final String REST_GET_COPYCONTENT = "/s/fr/itldev/koya/content/copy/{nodeRef}?destNodeRef={destNodeRef}";

    //
    private static final String REST_GET_LISTCONTENT = "/s/fr/itldev/koya/content/list/recursive/{nodeRef}?onlyFolders={onlyFolders}&maxdepth={maxdepth}";
    private static final String REST_GET_LISTCONTENT_PAGINATED = "/s/fr/itldev/koya/content/list/paginated/{nodeRef}?skipCount={skipCount}&maxItems={maxItems}&onlyFolders={onlyFolders}";

    private static final String REST_GET_DISKSIZE = "/s/fr/itldev/koya/global/disksize/{nodeRef}";
    private static final String REST_GET_IMPORTZIP = "/s/fr/itldev/koya/content/importzip/{zipnoderef}";
    private static final String DOWNLOAD_ZIP_WS_URI = "/s/fr/itldev/koya/content/zip?alf_ticket=";

    @Override
    public Directory createDir(User user, NodeRef parent, String title) throws AlfrescoServiceException {
        if (parent == null) {
            throw new AlfrescoServiceException("parent noderef must be set", 0);
        }
        return fromJSON(new TypeReference<Directory>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_CREATEDIR,
                String.class, parent.toString(), title));
    }

    @Override
    public Document upload(User user, NodeRef parent, Resource r) throws AlfrescoServiceException {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("filedata", r);
        parts.add("destination", parent.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(parts, headers);
        AlfrescoUploadReturn upReturn = fromJSON(new TypeReference<AlfrescoUploadReturn>() {
        }, user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_UPLOAD, request, String.class));

        return (Document) getSecuredItem(user, upReturn.getNodeRef());

    }

    @Override
    public Content move(User user, NodeRef contentToMove, NodeRef destination) throws AlfrescoServiceException {
        return (Content) fromJSON(new TypeReference<SecuredItem>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_MOVECONTENT, String.class, contentToMove, destination));
    }

    @Override
    public Content copy(User user, NodeRef contentToCopy, NodeRef destination) throws AlfrescoServiceException {
        return (Content) fromJSON(new TypeReference<SecuredItem>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_COPYCONTENT, String.class, contentToCopy, destination));
    }

    private static final Transformer TRANSFORM_TO_CONTENTS = new Transformer() {
        @Override
        public Object transform(Object input) {
            return (Content) input;
        }
    };

    @Override
    public List<Content> list(User user, NodeRef containerToList, Boolean onlyFolders, Integer depth) throws AlfrescoServiceException {

        List contents = fromJSON(new TypeReference<List<SecuredItem>>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_LISTCONTENT,
                String.class, containerToList, onlyFolders, depth));

        //tranform SecuredItems to contents
        CollectionUtils.transform(contents, TRANSFORM_TO_CONTENTS);

        return contents;
    }

    /**
     *
     * @param user
     * @param containerToList
     * @param skipCount
     * @param maxItems
     * @param onlyFolders
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public List<Content> listPaginatedDirectChild(User user, NodeRef containerToList,
            Integer skipCount, Integer maxItems, Boolean onlyFolders) throws AlfrescoServiceException {

        List contents = fromJSON(new TypeReference<List<SecuredItem>>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_LISTCONTENT_PAGINATED,
                String.class, containerToList, skipCount, maxItems, onlyFolders));

        //tranform SecuredItems to contents
        CollectionUtils.transform(contents, TRANSFORM_TO_CONTENTS);
        return contents;

    }

    @Override
    public Integer countChildren(User user, SecuredItem parent, Boolean onlyFolders) throws AlfrescoServiceException {

        Set<QName> typeFilter = new HashSet<>();

        if (onlyFolders) {
            typeFilter.add(ContentModel.TYPE_FOLDER);
        }

        return countChildren(user, parent, typeFilter);
    }

    @Override
    public Long getDiskSize(User user, SecuredItem securedItem) throws AlfrescoServiceException {
        DiskSizeWrapper ret = fromJSON(new TypeReference<DiskSizeWrapper>() {
        }, user.getRestTemplate().getForObject(getAlfrescoServerUrl()
                + REST_GET_DISKSIZE, String.class, securedItem.getNodeRef()));
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

        } catch (IOException e) {
            throw new AlfrescoServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void importZipedContent(User user, Document zipFile) throws AlfrescoServiceException {
        user.getRestTemplate()
                .getForObject(getAlfrescoServerUrl() + REST_GET_IMPORTZIP,
                        String.class, zipFile.getNodeRef());
    }

}
