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

import java.util.List;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public interface FavouriteService {

	public List<KoyaNode> getFavourites(User user)
			throws AlfrescoServiceException;

	public Boolean setFavouriteValue(User user, KoyaNode item,
			Boolean favouriteValue) throws AlfrescoServiceException;

	public Boolean isFavourite(User user, KoyaNode item)
			throws AlfrescoServiceException;
}
