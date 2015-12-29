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

package fr.itldev.koya.services;

import java.util.List;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

/**
 * KoyaNodes Generic methods
 * 
 */
public interface KoyaNodeService extends AlfrescoService {

	/**
	 * Deletes item.
	 * 
	 * @param user
	 * @param KoyaNode
	 * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
	 */
	void delete(User user, KoyaNode koyaNode) throws AlfrescoServiceException;

	/**
	 * Renames item.
	 * 
	 * @param user
	 * @param koyaNode
	 * @param newName
	 * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
	 */
	void rename(User user, KoyaNode koyaNode, String newName)
			throws AlfrescoServiceException;

	/**
	 * 
	 * Returns KoyaNode Parent if exists.
	 * 
	 * @param user
	 * @param koyaNode
	 * @return
	 * @throws AlfrescoServiceException
	 */
	KoyaNode getParent(User user, KoyaNode koyaNode)
			throws AlfrescoServiceException;

	/**
	 * Returns koyaNodes ancestors list.
	 * 
	 * @param user
	 * @param koyaNode
	 * @return
	 * @throws AlfrescoServiceException
	 */
	List<KoyaNode> getParents(User user, KoyaNode koyaNode)
			throws AlfrescoServiceException;

	/**
	 * Returns size in bytes of node recursively 
	 * 
	 * @param user
	 * @param koyaNode
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Long getSize(User user, KoyaNode koyaNode) throws AlfrescoServiceException;

}
