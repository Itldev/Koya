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
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.springframework.web.client.RestTemplate;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.FavouriteService;
import fr.itldev.koya.services.KoyaNodeService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public class KoyaNodeServiceImpl extends AlfrescoRestService implements
		KoyaNodeService, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String REST_GET_DELITEM = "/s/fr/itldev/koya/global/delete/{nodeRef}?alf_ticket={alf_ticket}";
	protected static final String REST_GET_RENAMEITEM = "/s/fr/itldev/koya/global/rename/{nodeRef}?alf_ticket={alf_ticket}";
	private static final String REST_GET_PARENTS = "/s/fr/itldev/koya/global/parents/{nodeRef}?nbAncestor={nbAncestor}&failSafe={failSafe}&alf_ticket={alf_ticket}";
	private static final String REST_GET_PARENTS_INFINITE = "/s/fr/itldev/koya/global/parents/{nodeRef}?alf_ticket={alf_ticket}";
	private static final String REST_GET_SIZE = "/s/fr/itldev/koya/content/size/{nodeRef}?alf_ticket={alf_ticket}";

	private String alfrescoServerUrl;

	private RestTemplate template;

	private FavouriteService favouriteService;

	// <editor-fold defaultstate="collapsed" desc="Getters/Setters">
	@Override
	public String getAlfrescoServerUrl() {
		return alfrescoServerUrl;
	}

	@Override
	public void setAlfrescoServerUrl(String alfrescoServerUrl) {
		this.alfrescoServerUrl = alfrescoServerUrl;
	}

	@Override
	public RestTemplate getTemplate() {
		return template;
	}

	@Override
	public void setTemplate(RestTemplate template) {
		this.template = template;
	}

	public void setFavouriteService(FavouriteService favouriteService) {
		this.favouriteService = favouriteService;
	}

	// </editor-fold>
	/**
	 * deletes item.
	 * 
	 * @param user
	 * @param KoyaNode
	 * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
	 */
	@Override
	public void delete(User user, KoyaNode KoyaNode)
			throws AlfrescoServiceException {
		/**
		 * remove from favourites before delete if necessary
		 */

		if (favouriteService.isFavourite(user, KoyaNode)) {
			/**
			 * this action is realized in order to invalidate user favourites
			 * client cache.
			 * 
			 * Unset favourite is also done on server side before delete node
			 * 
			 */
			favouriteService.setFavouriteValue(user, KoyaNode, Boolean.FALSE);
		}

		getTemplate().getForObject(alfrescoServerUrl + REST_GET_DELITEM,
				String.class, KoyaNode.getNodeRef(), user.getTicketAlfresco());
	}

	/**
	 * Renames item.
	 * 
	 * @param user
	 * @param KoyaNode
	 * @param newName
	 */
	@Override
	public void rename(User user, KoyaNode KoyaNode, String newName)
			throws AlfrescoServiceException {
		Map<String, String> postParams = new HashMap<>();

		postParams.put("newName", newName);

		getTemplate().postForObject(alfrescoServerUrl + REST_GET_RENAMEITEM,
				postParams, String.class, KoyaNode.getNodeRef(),
				user.getTicketAlfresco());
	}

	/**
	 * Get Secured Item Parent if exists.
	 * 
	 * @param user
	 * @param KoyaNode
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public KoyaNode getParent(User user, KoyaNode koyaNode)
			throws AlfrescoServiceException {
		return getParent(user, koyaNode,false);
	}
	
	
	/**
	 * 
	 * Returns KoyaNode Parent if exists.
	 * 
	 * Do not throw exception if Failsafe parameter true; 
	 * 
	 * @param user
	 * @param koyaNode
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public KoyaNode getParent(User user, KoyaNode koyaNode,Boolean failSafe)
			throws AlfrescoServiceException {

		List<KoyaNode> parents = fromJSON(
				new TypeReference<List<KoyaNode>>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_PARENTS,
						String.class, koyaNode.getNodeRef(), 1,failSafe,
						user.getTicketAlfresco()));

		if (parents== null ||parents.isEmpty()) {
			return null;
		} else {
			return parents.get(0);
		}

	}
	
	/**
	 * Get Secured Item Parent if exists.
	 * 
	 * @param user
	 * @param KoyaNode
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public List<KoyaNode> getParents(User user, KoyaNode KoyaNode)
			throws AlfrescoServiceException {

		return fromJSON(
				new TypeReference<List<KoyaNode>>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_PARENTS_INFINITE,
						String.class, KoyaNode.getNodeRef(),
						user.getTicketAlfresco()));
	}

	@Override
	public Long getSize(User user, KoyaNode koyaNode)
			throws AlfrescoServiceException {

		return fromJSON(
				new TypeReference<Long>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_SIZE, String.class,
						koyaNode.getNodeRef(),user.getTicketAlfresco()));
	}
}
