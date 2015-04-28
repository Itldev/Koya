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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.MetaInfos;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.AlfrescoUploadReturn;
import fr.itldev.koya.model.json.MailWrapper;
import fr.itldev.koya.services.AlfrescoService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import fr.itldev.koya.services.impl.util.KoyaUtil;

public class AlfrescoRestService implements AlfrescoService {

	private static final Logger logger = Logger
			.getLogger(AlfrescoRestService.class);

	private static final String REST_GET_SERVERINFOS = "/s/fr/itldev/koya/meta/infos";
	private static final String REST_POST_MAIL = "/s/fr/itldev/koya/global/mail";
	private static final String REST_GET_KOYANODE = "/s/fr/itldev/koya/global/node/{nodeRef}";
	private static final String REST_GET_LIBVERSION = "/s/fr/itldev/koya/meta/libversion";
	protected static final String REST_POST_UPLOAD = "/s/api/upload";
	private static final String REST_GET_XPATH2NODEREF = "/s/fr/itldev/koya/global/xpath2noderef/{xPath}";

	protected static final String REST_GET_LISTCHILD_PAGINATED = "/s/fr/itldev/koya/content/list/{nodeRef}"
			+ "?skipCount={skipCount}"
			+ "&maxItems={maxItems}"
			+ "&onlyFolders={onlyFolders}"
			+ "&filterExpr={filterExpr}"
			+ "&sortExpr={sortExpr}" + "&typeFilter={typeFilter}";

	private static final String REST_POST_COUNTCHILDREN = "/s/fr/itldev/koya/global/countchildren/{parentNodeRef}";

	private String alfrescoServerUrl;

	private RestTemplate template;

	// <editor-fold defaultstate="collapsed" desc="Getters/Setters">
	public String getAlfrescoServerUrl() {
		return alfrescoServerUrl;
	}

	public void setAlfrescoServerUrl(String alfrescoServerUrl) {
		this.alfrescoServerUrl = alfrescoServerUrl;
	}

	public RestTemplate getTemplate() {
		return template;
	}

	public void setTemplate(RestTemplate template) {
		this.template = template;
	}

	// </editor-fold>
	/**
	 * Check if library version match with server one.
	 */
	@Override
	public Boolean checkLibVersionMatch() {
		String serverVersion = getTemplate().getForObject(
				getAlfrescoServerUrl() + REST_GET_LIBVERSION, String.class);
		String localVersion = KoyaUtil.getLibKoyaVersion();

		Boolean match = localVersion.equals(serverVersion);
		if (!match) {
			logger.warn("LibKoya server version doesn't match local version"
					+ " (server=" + serverVersion + ";local=" + localVersion
					+ " )");
		}

		return localVersion.equals(serverVersion);
	}

	/**
	 * Get Informations about server and modules.
	 * 
	 * @param user
	 * @return
	 */
	@Override
	public MetaInfos getServerInfos(User user) throws AlfrescoServiceException {
		return fromJSON(
				new TypeReference<MetaInfos>() {
				},
				user.getRestTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_SERVERINFOS,
						String.class));
	}

	/**
	 * 
	 * @param user
	 * @param wrapper
	 * @throws AlfrescoServiceException
	 */
	@Override
	public void sendMail(User user, MailWrapper wrapper)
			throws AlfrescoServiceException {
		if (user == null) {
			getTemplate().postForObject(
					alfrescoServerUrl + REST_POST_MAIL + "?guest=true",
					wrapper, String.class);
		} else {
			user.getRestTemplate().postForObject(
					alfrescoServerUrl + REST_POST_MAIL, wrapper, String.class);
		}

	}

	/**
	 * Get KoyaNode from noderef reference.
	 * 
	 * @param user
	 * @param nodeRef
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public KoyaNode getKoyaNode(User user, NodeRef nodeRef)
			throws AlfrescoServiceException {
		return fromJSON(
				new TypeReference<KoyaNode>() {
				},
				user.getRestTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_KOYANODE,
						String.class, nodeRef.toString()));
	}

	/**
	 * Get NodeRef reference from xpath expression.
	 * 
	 * @param user
	 * @param nodeRef
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public String xPathToNodeRef(User user, String nodeRef)
			throws AlfrescoServiceException {
		return user.getRestTemplate().getForObject(
				getAlfrescoServerUrl() + REST_GET_XPATH2NODEREF, String.class,
				nodeRef);

	}

	@Override
	public AlfrescoUploadReturn uploadToXpathNode(User user, Resource resource,
			String parentXPath) throws AlfrescoServiceException {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

		parts.add("filedata", resource);
		parts.add("destination", xPathToNodeRef(user, parentXPath));
		parts.add("overwrite", "true");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(
				parts, headers);

		return fromJSON(
				new TypeReference<AlfrescoUploadReturn>() {
				},
				user.getRestTemplate().postForObject(
						getAlfrescoServerUrl() + REST_POST_UPLOAD, request,
						String.class));
	}

	@Override
	public Integer countChildren(User user, KoyaNode parent,
			Set<QName> qNameFilter) throws AlfrescoServiceException {
		return user.getRestTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_COUNTCHILDREN, qNameFilter,
				Integer.class, parent.getNodeRef());
	}

	/*
	 * ================ Utils methods ==================
	 */
	/**
	 * Extracts noderef parts.
	 * 
	 * @param nodeRef
	 * @return
	 */
	protected Map<String, String> explodeNodeRef(String nodeRef) {

		Map<String, String> exploded = new HashMap<>();

		Pattern p = Pattern.compile("(\\w+):\\/\\/(\\w+)\\/(.*)");
		Matcher m = p.matcher(nodeRef);

		if (m.find()) {
			exploded.put("store_type", m.group(1));
			exploded.put("store_id", m.group(2));
			exploded.put("id", m.group(3));

		}

		return exploded;
	}

	/**
	 * Converts byte datasize to human readable
	 * 
	 * @param bytes
	 * @param si
	 * @return
	 */
	protected String getHumanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) {
			return bytes + " b";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
				+ (si ? "" : "i");
		return String.format("%.1f %so", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Return typed object from json string.
	 * 
	 * 
	 * @param <T>
	 * @param type
	 * @param jsonPacket
	 * @return
	 */
	public static <T> T fromJSON(final TypeReference<T> type,
			final String jsonPacket) {
		T data = null;
		if (jsonPacket == null) {
			return null;
		}

		try {
			data = new ObjectMapper().readValue(jsonPacket, type);			
		} catch (Exception e) {
			// Handle the problem
			System.out.println("================");
			System.out.println(" json="+jsonPacket);			
			System.out.println(" err fromJson : "+type.getType());
			e.printStackTrace();
			System.out.println("================");
			logger.error(e.getMessage(), e);
		}
		return data;
	}

}
