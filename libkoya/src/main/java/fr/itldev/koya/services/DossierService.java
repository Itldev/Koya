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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.PaginatedContentList;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public interface DossierService extends AlfrescoService {

	/**
	 * Creates a new Dossier
	 * 
	 * @param user
	 * @param title
	 * @param parentSpace
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Dossier create(User user, Space parentSpace, String title)
			throws AlfrescoServiceException;

	/**
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Dossier edit(User user, Dossier dossier) throws AlfrescoServiceException;

	/**
	 * List all Space Dossiers
	 * 
	 * @param user
	 * @param space
	 * @param skipCount
	 * @param maxItems
	 * @return
	 * @throws AlfrescoServiceException
	 */
	PaginatedContentList list(User user, Space space, int skipCount,
			int maxItems) throws AlfrescoServiceException;

	/**
	 * List all Space Dossiers with filter and sort options
	 * 
	 * @param user
	 * @param space
	 * @param skipCount
	 * @param maxItems
	 * @param filter
	 * @return
	 * @throws AlfrescoServiceException
	 */
	PaginatedContentList list(User user, Space space, int skipCount,
			int maxItems, String filter, String sortField, Boolean ascending)
			throws AlfrescoServiceException;

	/**
	 * Count all Space Dossiers
	 * 
	 * @param user
	 * @param space
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Integer countChildren(User user, Space space)
			throws AlfrescoServiceException;

	/**
	 * List all users in charge of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	List<User> listResponsibles(User user, Dossier dossier)
			throws AlfrescoServiceException;

	/**
	 * List all users members of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	List<User> listMembers(User user, Dossier dossier)
			throws AlfrescoServiceException;

	/**
	 * Adds a user in charge of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @param responsible
	 * @throws AlfrescoServiceException
	 */
	void addResponsible(User user, Dossier dossier, User responsible)
			throws AlfrescoServiceException;

	/**
	 * Adds a user member of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @param responsible
	 * @throws AlfrescoServiceException
	 */
	void addMember(User user, Dossier dossier, User member)
			throws AlfrescoServiceException;

	/**
	 * Add a list of users in charge of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @param responsibles
	 * @throws AlfrescoServiceException
	 */
	void addResponsible(User user, Dossier dossier, List<User> responsibles)
			throws AlfrescoServiceException;

	/**
	 * Remove user member or responsible of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @param memberOrResp
	 * @throws AlfrescoServiceException
	 */
	void removeMembership(User user, Dossier dossier, User memberOrResp)
			throws AlfrescoServiceException;

	/**
	 * checks if dossier is confidential
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Boolean isConfidential(User user, Dossier dossier)
			throws AlfrescoServiceException;

	/**
	 * change dossier confidentiality status
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Boolean setConfidentiality(User user, Dossier dossier, Boolean confidential)
			throws AlfrescoServiceException;

	/**
	 * Creates dossier summary descriptors files.
	 * 
	 * prints dossier tree in files returns generated files nodeRefs in a map
	 * 
	 * @param user
	 * @param dossier
	 * @param summaryFileName
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Map<String, NodeRef> createSummary(User user, Dossier dossier,
			String summaryFileName) throws AlfrescoServiceException;

	/**
	 * Start a workflow
	 * 
	 */
	Dossier startWorkflow(User user, Dossier d, String workflowId,
			Map<String, String> properties) throws AlfrescoServiceException;

	/**
	 * Validate workflow step
	 * 
	 * @param user
	 * @param taskId
	 * @param properties
	 * @throws AlfrescoServiceException
	 */
	void endTask(User user, String taskId, Map<String, String> properties)
			throws AlfrescoServiceException;

	/**
	 * return the client document uploaded list
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	List<Document> listClientUploadedDocuments(User user, Dossier dossier)
			throws AlfrescoServiceException;

	/**
	 * 
	 * @param user
	 * @param workflowInstanceId
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Map<String, Serializable> getWorkflowStatus(User user,
			String workflowInstanceId) throws AlfrescoServiceException;

	/**
	 * 
	 * @param user
	 * @param taskInstanceId
	 * @return
	 * @throws AlfrescoServiceException
	 */
	Boolean taskIsAssignee(User user, String taskInstanceId)
			throws AlfrescoServiceException;

}
