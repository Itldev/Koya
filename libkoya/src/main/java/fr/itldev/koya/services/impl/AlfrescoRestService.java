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

import java.io.Serializable;
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
import org.springframework.web.client.RestTemplate;

import fr.itldev.koya.model.KoyaModelVersion;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.MetaInfos;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.MailWrapper;
import fr.itldev.koya.services.AlfrescoService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;


public class AlfrescoRestService implements AlfrescoService, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(AlfrescoRestService.class);

	private static final String REST_POST_CREATE = "/s/fr/itldev/koya/global/create/{parentNodeRef}?alf_ticket={alf_ticket}";

	private static final String REST_GET_SERVERINFOS = "/s/fr/itldev/koya/meta/infos?alf_ticket={alf_ticket}";
	private static final String REST_POST_MAIL = "/s/fr/itldev/koya/global/mail?alf_ticket={alf_ticket}";
	private static final String REST_GET_KOYANODE = "/s/fr/itldev/koya/global/node/{nodeRef}?alf_ticket={alf_ticket}";
	private static final String REST_GET_LIBVERSION = "/s/fr/itldev/koya/meta/libversion";
	protected static final String REST_POST_UPLOAD = "/s/api/upload?alf_ticket={alf_ticket}";

	protected static final String REST_GET_LISTCHILD_PAGINATED = "/s/fr/itldev/koya/content/list/{nodeRef}"
			+ "?skipCount={skipCount}"
			+ "&maxItems={maxItems}"
			+ "&onlyFolders={onlyFolders}"
			+ "&filterExpr={filterExpr}"
			+ "&typeFilter={typeFilter}"
			+ "&sortField={sortField}"
			+ "&ascending={ascending}&alf_ticket={alf_ticket}";
	
	private static final String REST_POST_COUNTCHILDREN = "/s/fr/itldev/koya/global/countchildren/{parentNodeRef}?alf_ticket={alf_ticket}";

	private String alfrescoServerUrl;
	private String shareWebappUrl;

	private RestTemplate template;

	// <editor-fold defaultstate="collapsed" desc="Getters/Setters">
	public String getAlfrescoServerUrl() {
		return alfrescoServerUrl;
	}

	public void setAlfrescoServerUrl(String alfrescoServerUrl) {
		this.alfrescoServerUrl = alfrescoServerUrl;
	}

	public String getShareWebappUrl() {
		return shareWebappUrl;
	}

	public void setShareWebappUrl(String shareWebappUrl) {
		this.shareWebappUrl = shareWebappUrl;
	}

	public RestTemplate getTemplate() {
		return template;
	}

	public void setTemplate(RestTemplate template) {
		this.template = template;
	}

	// </editor-fold>

	/**
	 * Creates new Koya Node
	 * 
	 */
	protected KoyaNode create(User user, KoyaNode parent, KoyaNode toCreate)
			throws AlfrescoServiceException {
		if (parent == null) {
			throw new AlfrescoServiceException("parent noderef must be set", 0);
		}
		return fromJSON(
				new TypeReference<KoyaNode>() {
				},
				getTemplate().postForObject(
						getAlfrescoServerUrl() + REST_POST_CREATE, toCreate,
						String.class, parent.getNodeRef(),user.getTicketAlfresco()));
	}

	/**
	 * Check if library version match with server one.
	 */
	@Override
	public Boolean checkLibVersionMatch() {
		String serverVersion = getTemplate().getForObject(
				getAlfrescoServerUrl() + REST_GET_LIBVERSION, String.class);
		String localVersion = KoyaModelVersion.getVersion();

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
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_SERVERINFOS,
						String.class,user.getTicketAlfresco()));
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
			getTemplate().postForObject(
					alfrescoServerUrl + REST_POST_MAIL, wrapper, String.class,user.getTicketAlfresco());
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
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_KOYANODE,
						String.class, nodeRef.toString(),user.getTicketAlfresco()));
	}

	@Override
	public Integer countChildren(User user, KoyaNode parent,
			Set<QName> qNameFilter) throws AlfrescoServiceException {
		return getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_COUNTCHILDREN, qNameFilter,
				Integer.class, parent.getNodeRef(),user.getTicketAlfresco());
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
			System.out.println(" json=" + jsonPacket);
			System.out.println(" err fromJson : " + type.getType());
			e.printStackTrace();
			System.out.println("================");
			logger.error(e.getMessage(), e);
		}
		return data;
	}

}
