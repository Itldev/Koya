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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.codehaus.jackson.type.TypeReference;

import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.PaginatedContentList;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.services.DossierService;
import fr.itldev.koya.services.cache.CacheManager;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public class DossierServiceImpl extends AlfrescoRestService implements DossierService, Serializable {
    
	public static final String REST_GET_LISTMEMBERSHIP = "/s/fr/itldev/koya/security/membership/{rolename}/{noderef}?alf_ticket={alf_ticket}";
	private static final String REST_POST_MODIFYMEMBERSHIP = "/s/fr/itldev/koya/security/membership/{method}/{rolename}/{noderef}?alf_ticket={alf_ticket}";
	private static final String REST_GET_CLIENT_DOC_LIST = "/s/fr/itldev/koya/dossier/clientdocuments/{noderef}?alf_ticket={alf_ticket}";

	private static final String REST_CONFIDENTIAL = "/s/fr/itldev/koya/dossier/confidential/{nodeRef}?alf_ticket={alf_ticket}";

	private static final String REST_SUMMARY = "/s/fr/itldev/koya/dossier/summary/{nodeId}?documentName={documentName}?alf_ticket={alf_ticket}";


	private CacheManager cacheManager;

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public Dossier create(User user, Space parentSpace, String title)
			throws AlfrescoServiceException {
		return (Dossier) super.create(user, parentSpace,
				Dossier.newInstance(title));
	}

	/**
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public Dossier edit(User user, Dossier dossier)
			throws AlfrescoServiceException {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	/**
	 * List all Space Dossiers
	 * 
	 * @param user
	 * @param space
	 * @throws AlfrescoServiceException
	 */
	@Override
	public PaginatedContentList list(User user, Space space, int skipCount,
			int maxItems) throws AlfrescoServiceException {
		return list(user, space, skipCount, maxItems, "", "");
	}

	/**
	 * List all Space Dossiers TODO sort parameter not process in this version
	 * 
	 * 
	 * @param user
	 * @param space
	 * @throws AlfrescoServiceException
	 */
	@Override
	public PaginatedContentList list(User user, Space space, int skipCount,
			int maxItems, String filter, String sort)
			throws AlfrescoServiceException {

		PaginatedContentList pcl = getTemplate().getForObject(
				getAlfrescoServerUrl()
						+ AlfrescoRestService.REST_GET_LISTCHILD_PAGINATED,
				PaginatedContentList.class, space.getNodeRef(), skipCount,
				maxItems, true, filter, sort, "",user.getTicketAlfresco());
		return pcl;
	}

	/**
	 * Count all Space Dossiers
	 * 
	 * @param user
	 * @param space
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@SuppressWarnings("serial")
	@Override
	public Integer countChildren(User user, Space space)
			throws AlfrescoServiceException {
		return countChildren(user, space, new HashSet<QName>() {
			{
				add(KoyaModel.TYPE_DOSSIER);
			}
		});
	}

	/**
	 * List all users in charge of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public List<User> listResponsibles(User user, Dossier dossier)
			throws AlfrescoServiceException {
		return fromJSON(
				new TypeReference<List<User>>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_LISTMEMBERSHIP,
						String.class, KoyaPermissionCollaborator.RESPONSIBLE,
						dossier.getNodeRef(),user.getTicketAlfresco()));
	}

	/**
	 * List all users in charge of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public List<User> listMembers(User user, Dossier dossier)
			throws AlfrescoServiceException {
		return fromJSON(
				new TypeReference<List<User>>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_LISTMEMBERSHIP,
						String.class, KoyaPermissionCollaborator.MEMBER,
						dossier.getNodeRef(),user.getTicketAlfresco()));
	}

	/**
	 * Adds a user in charge of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @param responsible
	 * @throws AlfrescoServiceException
	 */
	@Override
	public void addResponsible(User user, Dossier dossier, User responsible)
			throws AlfrescoServiceException {
		getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_MODIFYMEMBERSHIP,
				responsible, String.class, "add",
				KoyaPermissionCollaborator.RESPONSIBLE, dossier.getNodeRef(),user.getTicketAlfresco());

		// invalidate user cache
		cacheManager.revokePermission(responsible, dossier.getNodeRef());

	}

	@Override
	public void addMember(User user, Dossier dossier, User member)
			throws AlfrescoServiceException {

		getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_MODIFYMEMBERSHIP, member,
				String.class, "add", KoyaPermissionCollaborator.MEMBER,
				dossier.getNodeRef(),user.getTicketAlfresco());

		// invalidate user cache
		cacheManager.revokePermission(member, dossier.getNodeRef());
	}

	/**
	 * Add a list of users in charge of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @param responsibles
	 * @throws AlfrescoServiceException
	 */
	@Override
	public void addResponsible(User user, Dossier dossier,
			List<User> responsibles) throws AlfrescoServiceException {
		for (User u : responsibles) {
			addResponsible(user, dossier, u);
		}
	}

	/**
	 * Remove user member or responsible of specified Dossier.
	 * 
	 * @param user
	 * @param dossier
	 * @param memberOrResp
	 * @throws AlfrescoServiceException
	 */
	@Override
	public void removeMembership(User user, Dossier dossier, User memberOrResp)
			throws AlfrescoServiceException {
		String rolename = "any";
		getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_MODIFYMEMBERSHIP,
				memberOrResp, String.class, "del", rolename,
				dossier.getNodeRef(),user.getTicketAlfresco());
		cacheManager.revokePermission(memberOrResp, dossier.getNodeRef());

	}

	/**
	 * checks if dossier is confidential
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public Boolean isConfidential(User user, Dossier dossier)
			throws AlfrescoServiceException {
		return Boolean.valueOf(getTemplate().getForObject(
				getAlfrescoServerUrl() + REST_CONFIDENTIAL, String.class,
				dossier.getNodeRef(),user.getTicketAlfresco()));
	}

	/**
	 * change dossier confidentiality status
	 * 
	 * @param user
	 * @param dossier
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public Boolean setConfidentiality(User user, Dossier dossier,
			Boolean confidential) throws AlfrescoServiceException {
		Map<String, String> params = new HashMap<>();
		params.put("confidential", confidential.toString());
		return Boolean.valueOf(getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_CONFIDENTIAL, params,
				String.class, dossier.getNodeRef(),user.getTicketAlfresco()));
	}

	@Override
	public Map<String, NodeRef> createSummary(User user, Dossier dossier,
			String summaryFileName) throws AlfrescoServiceException {
		// extract map
		Map<String, String> returnValues = getTemplate().getForObject(
				getAlfrescoServerUrl() + REST_SUMMARY, Map.class,
				dossier.getNodeRef().getId(), summaryFileName,user.getTicketAlfresco());

		Map<String, NodeRef> nodes = new HashMap<String, NodeRef>();
		for (String k : returnValues.keySet()) {
			nodes.put(k, new NodeRef(returnValues.get(k)));
		}
		return nodes;
	}

	@Override
	public List<Document> listClientUploadedDocuments(User user, Dossier dossier) {

		return fromJSON(
				new TypeReference<List<Document>>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_CLIENT_DOC_LIST,
						String.class, dossier.getNodeRef(),user.getTicketAlfresco()));

	}

	/**
	 * Workflow methods
	 * 
	 */

	private static final String REST_POST_START_WORKFLOW = "/s/fr/itldev/koya/workflow/start/{workflowId}/{nodeRef}?alf_ticket={alf_ticket}";
	private static final String REST_POST_VALIDATE_STEP = "/s/api/task/{workflowInstanceId}/formprocessor?alf_ticket={alf_ticket}";
	private static final String REST_GET_WORKFLOW_STATUS = "/s/fr/itldev/koya/workflow/workflow-instance/{workflowInstanceId}?includeTasks=true&alf_ticket={alf_ticket}";
	private static final String REST_GET_TASK_STATUS = "/s/fr/itldev/koya/workflow/task-isassignee/{taskInstanceId}?alf_ticket={alf_ticket}";

	@Override
	public Dossier startWorkflow(User user, Dossier d, String workflowId,
			Map<String, String> properties) throws AlfrescoServiceException {
		return fromJSON(
				new TypeReference<Dossier>() {
				},
				getTemplate().postForObject(
						getAlfrescoServerUrl() + REST_POST_START_WORKFLOW,
						properties, String.class, workflowId, d.getNodeRef(),user.getTicketAlfresco()));
	}

	@Override
	public void endTask(User user, String taskId, Map<String, String> properties)
			throws AlfrescoServiceException {
		getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_VALIDATE_STEP, properties,
				String.class, taskId,user.getTicketAlfresco());

	}

	@Override
	public Map<String, Serializable> getWorkflowStatus(User user,
			String workflowInstanceId) throws AlfrescoServiceException {

		Map<String, Serializable> returnValues = getTemplate()
				.getForObject(
						getAlfrescoServerUrl() + REST_GET_WORKFLOW_STATUS,
						Map.class, workflowInstanceId,user.getTicketAlfresco());
		return returnValues;
	}

	@Override
	public Boolean taskIsAssignee(User user, String taskInstanceId)
			throws AlfrescoServiceException {
		Map<String, Serializable> map = getTemplate().getForObject(
				getAlfrescoServerUrl() + REST_GET_TASK_STATUS, Map.class,
				taskInstanceId,user.getTicketAlfresco());		
		return Boolean.valueOf(map.get("isassignee").toString());
	}

}
