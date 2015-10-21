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

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.codehaus.jackson.type.TypeReference;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.SpaceService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public class SpaceServiceImpl extends AlfrescoRestService implements
		SpaceService {

	private static final String REST_POST_LISTSPACE = "/s/fr/itldev/koya/space/list?alf_ticket={alf_ticket}";
	private static final String REST_POST_LISTSPACE_DEPTH_OPTION = "/s/fr/itldev/koya/space/list?maxdepth={maxdepth}&alf_ticket={alf_ticket}";

	/**
	 * Create a new space
	 * 
	 * @param user
	 * @param space
	 * @param parent
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public Space create(User user, KoyaNode parent, String title)
			throws AlfrescoServiceException {
		return (Space) super.create(user, parent, Space.newInstance(title));
	}

	@Override
	public void enable(User user, Space space) throws AlfrescoServiceException {
		// TODO specific implementation
		throw new NotImplementedException();
	}

	@Override
	public void disable(User user, Space space) throws AlfrescoServiceException {
		throw new NotImplementedException();
	}

	@Override
	public List<Space> list(User user, Company company, Integer... depth)
			throws AlfrescoServiceException {
		if (depth.length > 0) {
			return fromJSON(
					new TypeReference<List<Space>>() {
					},
					getTemplate().postForObject(
							getAlfrescoServerUrl()
									+ REST_POST_LISTSPACE_DEPTH_OPTION,
							company, String.class, depth[0],
							user.getTicketAlfresco()));
		} else {
			return fromJSON(
					new TypeReference<List<Space>>() {
					},
					getTemplate().postForObject(
							getAlfrescoServerUrl() + REST_POST_LISTSPACE,
							company, String.class, user.getTicketAlfresco()));
		}
	}

}
