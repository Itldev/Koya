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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.FavouriteService;
import fr.itldev.koya.services.UserService;
import fr.itldev.koya.services.cache.CacheManager;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public class FavouriteServiceImpl extends AlfrescoRestService implements
		FavouriteService {

	private static final String REST_GET_LISTFAVOURITES = "/s/fr/itldev/koya/favourites/list?alf_ticket={alf_ticket}";
	private static final String REST_POST_FAVOURITE_STATUS = "/s/fr/itldev/koya/favourites/set?alf_ticket={alf_ticket}";

	@Autowired
	UserService userService;

	private CacheManager cacheManager;

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public List<KoyaNode> getFavourites(User user)
			throws AlfrescoServiceException {

		if (user == null) {
			return new ArrayList<>();
		}
		List<KoyaNode> userFavourites = cacheManager.getUserFavourites(user);

		if (userFavourites != null) {
			return userFavourites;
		}

		userFavourites = fromJSON(
				new TypeReference<List<KoyaNode>>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_LISTFAVOURITES,
						String.class, user.getTicketAlfresco()));
		cacheManager.setUserFavourites(user, userFavourites);
		return userFavourites;
	}

	@Override
	public Boolean setFavouriteValue(User user, KoyaNode item,
			Boolean favouriteValue) throws AlfrescoServiceException {
		cacheManager.revokeUserFavourites(user);

		Map<String, Object> postParams = new HashMap<>();
		// TODO write directly nodeRef Object instead of toString() value
		postParams.put("nodeRef", item.getNodeRef().toString());
		postParams.put("status", favouriteValue);

		Boolean status = fromJSON(
				new TypeReference<Boolean>() {
				},
				getTemplate().postForObject(
						getAlfrescoServerUrl() + REST_POST_FAVOURITE_STATUS,
						postParams, String.class, user.getTicketAlfresco()));
		// Automaticly reload user's preferences
		userService.loadPreferences(user);
		return status;
	}

	@Override
	public Boolean isFavourite(User user, KoyaNode item)
			throws AlfrescoServiceException {
		return getFavourites(user).contains(item);
	}

}
