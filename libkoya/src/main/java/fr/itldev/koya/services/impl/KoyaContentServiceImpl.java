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

import java.io.File;
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
import org.codehaus.jackson.type.TypeReference;
import org.json.simple.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.KoyaContent;
import fr.itldev.koya.model.json.AlfrescoUploadReturn;
import fr.itldev.koya.model.json.DiskSizeWrapper;
import fr.itldev.koya.model.json.PaginatedContentList;
import fr.itldev.koya.services.KoyaContentService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public class KoyaContentServiceImpl extends AlfrescoRestService implements
		KoyaContentService {

	private static final String REST_GET_CREATEDIR = "/s/fr/itldev/koya/content/createdir/{parentNodeRef}?title={title}";
	private static final String REST_GET_MOVECONTENT = "/s/fr/itldev/koya/content/move/{nodeRef}?destNodeRef={destNodeRef}";
	private static final String REST_GET_COPYCONTENT = "/s/fr/itldev/koya/content/copy/{nodeRef}?destNodeRef={destNodeRef}";

	//
	private static final String REST_GET_LISTCONTENTTREE = "/s/fr/itldev/koya/content/tree/{nodeRef}?onlyFolders={onlyFolders}&maxdepth={maxdepth}";

	private static final String REST_GET_DISKSIZE = "/s/fr/itldev/koya/global/disksize/{nodeRef}";
	private static final String REST_GET_IMPORTZIP = "/s/fr/itldev/koya/content/importzip/{zipnoderef}";
	private static final String DOWNLOAD_ZIP_WS_URI = "/s/fr/itldev/koya/content/zip?alf_ticket=";

	@Override
	public Directory createDir(User user, NodeRef parent, String title)
			throws AlfrescoServiceException {
		if (parent == null) {
			throw new AlfrescoServiceException("parent noderef must be set", 0);
		}
		return fromJSON(
				new TypeReference<Directory>() {
				},
				user.getRestTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_CREATEDIR,
						String.class, parent.toString(), title));
	}

	@Override
	public Document upload(User user, NodeRef parent, File f)
			throws AlfrescoServiceException {
		return upload(user, parent, (Object) f);
	}

	@Override
	public Document upload(User user, NodeRef parent, Resource r)
			throws AlfrescoServiceException {
		return upload(user, parent, (Object) r);
	}

	private Document upload(User user, NodeRef parent, Object o)
			throws AlfrescoServiceException {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		parts.add("filedata", o);
		parts.add("destination", parent.toString());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(
				parts, headers);
		AlfrescoUploadReturn upReturn = fromJSON(
				new TypeReference<AlfrescoUploadReturn>() {
				},
				user.getRestTemplate().postForObject(
						getAlfrescoServerUrl() + REST_POST_UPLOAD, request,
						String.class));

		return (Document) getKoyaNode(user, upReturn.getNodeRef());

	}

	@Override
	public KoyaContent move(User user, NodeRef contentToMove, NodeRef destination)
			throws AlfrescoServiceException {
		return (KoyaContent) fromJSON(
				new TypeReference<KoyaNode>() {
				},
				user.getRestTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_MOVECONTENT,
						String.class, contentToMove, destination));
	}

	@Override
	public KoyaContent copy(User user, NodeRef contentToCopy, NodeRef destination)
			throws AlfrescoServiceException {
		return (KoyaContent) fromJSON(
				new TypeReference<KoyaNode>() {
				},
				user.getRestTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_COPYCONTENT,
						String.class, contentToCopy, destination));
	}

	// TODO merge with a generic listing service
	@SuppressWarnings("unchecked")
	@Override
	public List<KoyaContent> list(User user, NodeRef containerToList,
			Boolean onlyFolders, Integer depth) throws AlfrescoServiceException {

		@SuppressWarnings("rawtypes")
		List contents = fromJSON(
				new TypeReference<List<KoyaNode>>() {
				},
				user.getRestTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_LISTCONTENTTREE,
						String.class, containerToList, onlyFolders, depth));

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
	public PaginatedContentList listPaginatedDirectChild(User user,
			NodeRef containerToList, Integer skipCount, Integer maxItems,
			Boolean onlyFolders) throws AlfrescoServiceException {

		PaginatedContentList pcl = user.getRestTemplate().getForObject(
				getAlfrescoServerUrl()
						+ AlfrescoRestService.REST_GET_LISTCHILD_PAGINATED,
				PaginatedContentList.class, containerToList, skipCount,
				maxItems, onlyFolders, "", "", "");
		return pcl;
	}

	@Override
	public Integer countChildren(User user, KoyaNode parent, Boolean onlyFolders)
			throws AlfrescoServiceException {

		Set<QName> typeFilter = new HashSet<>();

		if (onlyFolders) {
			typeFilter.add(ContentModel.TYPE_FOLDER);
		}

		return countChildren(user, parent, typeFilter);
	}

	@Override
	public Long getDiskSize(User user, KoyaNode KoyaNode)
			throws AlfrescoServiceException {
		DiskSizeWrapper ret = fromJSON(
				new TypeReference<DiskSizeWrapper>() {
				},
				user.getRestTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_DISKSIZE,
						String.class, KoyaNode.getNodeRef()));
		return ret.getSize();
	}

	@Override
	public InputStream getZipInputStream(User user, List<KoyaNode> KoyaNodes)
			throws AlfrescoServiceException {
		HttpURLConnection con;

		try {
			String urlDownload = getAlfrescoServerUrl() + DOWNLOAD_ZIP_WS_URI
					+ user.getTicketAlfresco();

			Map<String, Serializable> params = new HashMap<>();
			ArrayList<String> selected = new ArrayList<>();
			params.put("nodeRefs", selected);
			for (KoyaNode item : KoyaNodes) {
				selected.add(item.getNodeRef().toString());
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
	public void importZipedContent(User user, Document zipFile)
			throws AlfrescoServiceException {
		user.getRestTemplate().getForObject(
				getAlfrescoServerUrl() + REST_GET_IMPORTZIP, String.class,
				zipFile.getNodeRef());
	}

}
