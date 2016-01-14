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
import fr.itldev.koya.model.Permissions;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.impl.UserConnection;
import fr.itldev.koya.model.impl.UserRole;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public interface SecuService {

	/**
	 * List available userRoles for a company.
	 * 
	 * @param userLogged
	 * @param c
	 * @return
	 * @throws AlfrescoServiceException
	 */
	List<UserRole> listAvailableRoles(User userLogged, Company c) throws AlfrescoServiceException;

	/**
	 * Get current Role in Company context for specified User.
	 * 
	 * @param userLogged
	 * @param c
	 * @param userToGetRole
	 * @return
	 * @throws AlfrescoServiceException
	 */
	UserRole getUserRole(User userLogged, Company c, User userToGetRole)
			throws AlfrescoServiceException;

	/**
	 * Set userRole in Company context for specified User.
	 * 
	 * @param userLogged
	 * @param c
	 * @param userNameSetRole
	 * @param roleName
	 * @throws AlfrescoServiceException
	 */
	void setUserRole(User userLogged, Company c, String userNameSetRole, String roleName)
			throws AlfrescoServiceException;

	/**
	 * 
	 * @param userLogged
	 * @param userToGetConnections
	 * @param companyFilter
	 * @param maxResults
	 * @return
	 * @throws AlfrescoServiceException
	 */
	List<UserConnection> listUserConnections(User userLogged, User userToGetConnections,
			List<Company> companyFilter, Integer maxResults) throws AlfrescoServiceException;

	/**
	 * revoke all user Acces on specified company.
	 * 
	 * @param userLogged
	 * @param c
	 * @param u
	 * @throws AlfrescoServiceException
	 */
	void revokeAccess(User userLogged, Company c, User u) throws AlfrescoServiceException;

	/**
	 * Checks if user logged is company manager.
	 * 
	 * @param userLogged
	 * @param c
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Boolean isCompanyManager(User userLogged, Company c) throws AlfrescoServiceException;

	/**
	 * Get permissions on defined secured Item
	 * 
	 * @param user
	 * @param s
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Permissions getPermissions(User user, KoyaNode s) throws AlfrescoServiceException;

	/**
	 * List all users member of KoyaResponsibles Group on Space.
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	List<User> listResponsibles(User user, Space dossier) throws AlfrescoServiceException;

	/**
	 * List all users member of KoyaMember Group on Space.
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	List<User> listMembers(User user, Space dossier) throws AlfrescoServiceException;

	/**
	 * List all spaces a user can access with given permission in company
	 * context
	 * 
	 * @param user
	 * @param checked
	 * @param company
	 * @param permission
	 * @return
	 * @throws AlfrescoServiceException
	 */
	List<Space> listSpacesAccess(User user, User checked, Company company,
			KoyaPermission permission) throws AlfrescoServiceException;

}
